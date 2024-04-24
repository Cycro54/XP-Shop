package invoker54.xpshop.data.recipe;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationCopyEvent;
import invoker54.xpshop.util.MiniTicker;
import invoker54.xpshop.util.ModStopWatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static invoker54.xpshop.event.generation.ShopGenerationCopyEvent.*;

public class ItemData {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<ItemData> allItemDataList = Collections.synchronizedList(new ArrayList<>());
    public final ItemStack mainItem;
    public final PriceList priceList;
    public PriceList.PriceInfo baseInfo;
    private final List<RecipeData> myRecipeData = new ArrayList<>();
    public final List<PriceBranch> branches = new ArrayList<>();
//    //These are all the items needed to price this item.
    public final HashSet<ItemData> ingredientList = new HashSet<>();
    public static final ModStopWatch STOP_WATCH = ModStopWatch.getTimer(XPShop.MOD_ID, "Item price info", ModStopWatch.Time.LONGEST);
    public CompletableFuture<Boolean> isInitialized;
    public List<PriceGroup> mainGroup;
    public boolean hasRecipes = false;
    private ItemData(ItemStack mainItem) {
        allItemDataList.add(this);
        this.mainItem = mainItem;
        this.priceList = ShopGenerationCopyEvent.getPriceList(this.mainItem);
        this.baseInfo = this.priceList.getFinalPriceInfo();
        //LOGGER.warn("Is there initial recipe data? " + (initialRecipeData != null));
        ItemStack matchingStack = getMatchingItemStack(mainItem, craftResultMap.keySet());
        InitialRecipeData initialRecipeData = matchingStack == null ? null : craftResultMap.get(matchingStack);
        if (initialRecipeData == null){
            this.mainGroup = new ArrayList<>();
            this.isInitialized = CompletableFuture.completedFuture(true);
            return;
        }
        else {
            this.isInitialized = CompletableFuture.completedFuture(false);
        }
        //go through each recipePair and create a new recipeData class instance
        for (Pair<Recipe<?>, List<Ingredient>> recipePair : initialRecipeData.recipes()) {
            RecipeData matchingData = RecipeData.getMatchingData(recipePair);
            if (matchingData.isBadRecipe){
                LOGGER.warn(getItemName(this) + " has a bad recipe, skipping...");
                continue;
            }
            this.myRecipeData.add(matchingData);
            //LOGGER.warn(getItemName(this.mainItem)+" Added recipe " + matchingData.recipe.getId());
        }
        LOGGER.warn("Created a new item Data: " + getItemName(this));
    }

    public boolean hasRecipes(){
        return this.hasRecipes;
    }

    public synchronized List<PriceGroup> getPriceList(ItemData mainItem, List<ItemData> mainCheckList, List<ItemData> invalidList) {
        if (mainItem == this){
            if (!this.isInitialized.join()){
                this.isInitialized = new CompletableFuture<>();
            }
            mainCheckList.add(this);
        }
        else if (!this.isInitialized.isDone()) {
            LOGGER.info(getItemName(mainItem) + "I have to wait till they come back." + getItemName(this));
            this.isInitialized.join();
        }

        if (!this.branches.isEmpty()) {
            LOGGER.info(getItemName(mainItem) + " The item " + getItemName(this) + " has finished, grabbing branch info...");
            List<PriceGroup> groupList = this.getBranchInfo(mainCheckList);
            if (groupList != null) {
                return groupList;
            }
        }

        MiniTicker<?> ticker = STOP_WATCH.ticker();
        //This will be used for getting all possible items from the ingredients.
        List<ItemData> unmodifiedCopy = new ArrayList<>(mainCheckList);
//        int mainItemIndex = unmodifiedCopy.indexOf(this);

        //This is the main priceGroup List
        List<PriceGroup> mainGroupList = new ArrayList<>();
        //This will be for ingredients that repeat in other recipes.
        Map<IngredientData, List<PriceGroup>> cacheMap = new HashMap<>();

        for (RecipeData recipeData : this.myRecipeData){
            List<PriceGroup> recipeGroupLis = recipeData.getPriceList
                    (cacheMap, unmodifiedCopy, mainCheckList, invalidList, mainItem);

            //This is for each recipe for the item
            PriceGroup.GroupOperation op = ((list) ->{
                PriceList.PriceInfo resultInfo = PriceList.emptyPrice;
                for (PriceList.PriceInfo listInfo : list){
                    //If one of the ingredients end up being 0, it's a bad recipe.
                    if (listInfo.highPrice() == 0) return PriceList.emptyPrice;
                    resultInfo = resultInfo.add(listInfo);
                }
                return resultInfo.calculate((x)->x/recipeData.resultCount);
            });
            PriceGroup recipeGroup = new PriceGroup(null, recipeGroupLis, op, null);
            mainGroupList.add(recipeGroup);
        }

        //Remove all items from invalidList that are not behind the main item in the main checklist
        //Removed items are considered valid ingredients.
        invalidList.removeIf((item) -> item == this || !unmodifiedCopy.contains(item));
        //Make a new price branch
        synchronized (this.branches) {
            LOGGER.info("Creating a new branch");
            List<ItemData> finalIngredientList = mainCheckList.subList(mainCheckList.indexOf(this), mainCheckList.size());
            finalIngredientList.addAll(invalidList);
            this.ingredientList.addAll(finalIngredientList);
            this.ingredientList.remove(this);
            addBranch(new HashSet<>(invalidList), mainGroupList);
        }

        //If invalidList has 0 items, finalize the price, this was a success.
        if (invalidList.isEmpty()) {
            this.mainGroup = new ArrayList<>(mainGroupList);

            this.finalizePrice(mainGroupList);

            LOGGER.debug(getItemName(this) + " New ingredient list: " + getItemDataNames(this.ingredientList));

            String resultString = ticker.record("To fully price " + getItemName(this) + ": " +
                    this.priceList.getFinalPriceInfo());
            if (!resultString.isEmpty()) LOGGER.info(resultString);

            this.isInitialized = CompletableFuture.completedFuture(true);
        }
        else {
            LOGGER.debug(getItemName(this) + "had bad ingredients: " + getItemDataNames(invalidList));
            LOGGER.debug(getItemName(this) + " was it the main caller? " + (mainItem == this));
        }

        return mainGroupList;
    }

    public void finalizePrice(List<PriceGroup> priceGroupList){
        this.priceList.clearRecipes();

        List<PriceList.PriceInfo> infoList = new ArrayList<>();
        priceGroupList.forEach((group) -> {
            PriceList.PriceInfo priceInfo = group.getResult();
            if (priceInfo.highPrice() == 0) return;
            infoList.add(priceInfo);
        });
        PriceList.sortAndRemoveOutliers(infoList);
        infoList.forEach(this.priceList::addRecipe);
    }

    public List<PriceGroup> getBranchInfo(List<ItemData> checkList) {
//        if (this.myRecipeData.isEmpty() || this.ingredientList.isEmpty()){
//            LOGGER.warn(getItemName(this) +" Branch info: recipes? " + !this.myRecipeData.isEmpty() +
//                    "Empty ingredient list? " + this.ingredientList.isEmpty());
//            return new ArrayList<>();
//        }


        Set<ItemData> invalidList = new HashSet<>(checkList.subList(0, checkList.indexOf(this)));
//        List<ItemData> validList = new ArrayList<>();
//        for (ItemData item : checkList) if (this.ingredientList.contains(item)) validList.add(item);
        //Removes all items that are not in the main ingredient list.
        invalidList.removeIf((item) -> !this.ingredientList.contains(item));

        List<PriceGroup> groupList = new ArrayList<>();
        for (PriceBranch branch : this.branches) {
            Set<ItemData> branchInvalidList = branch.invalidList();

            //Make sure the size of both lists are equal.
            if (branchInvalidList.size() != invalidList.size()) continue;

            boolean canSkip = invalidList.containsAll(branchInvalidList);
            if (canSkip) {
                LOGGER.warn(getItemName(this) + " found a suitable branch ");
                groupList = branch.priceGroupList;
                return groupList;
            }
        }

        if (this.isInitialized.isDone() && this.isInitialized.join()) {
            LOGGER.info(getItemName(this)+" Creating a new branch");
            groupList = new ArrayList<>();
            for (PriceGroup group : this.mainGroup) groupList.add(group.duplicate(new ArrayList<>(invalidList), null));
            synchronized (this.branches) {
                addBranch(invalidList, groupList);
            }
        }
        else {
            LOGGER.error(getItemName(checkList.get(0)) + " The item "
                    + getItemName(this) + " is missing its group data!?!");
        }

        //This will add all the ingredients from the priceGroups to the checklist
        groupList.forEach((group) -> group.getIngredients().forEach((item) ->{
            if (!checkList.contains(item)) checkList.add(item);
        }));

        return groupList.isEmpty() ? null : groupList;
    }

    public static ItemData getMatchingData(ItemStack stack) {
        synchronized (allItemDataList) {
            for (ItemData itemData : allItemDataList) {
                if (getMatchingItemStack(stack, List.of(itemData.mainItem)) != null)
                    return itemData;
            }
            //LOGGER.error(getItemName(stack) + " has no item data, creating new one...");
            return new ItemData(stack);
        }
    }

    public void addBranch(Set<ItemData> invalidList, List<PriceGroup> groupList) {
//        if (getBranchInfo(invalidList) != null){
//            LOGGER.error(getItemName(this)+" This item already has this branch info, skipping...");
//            return;
//        }
        Set<ItemData> validList = new HashSet<>();
        for (PriceGroup group : groupList){
            validList.addAll(group.getIngredients());
        }

        PriceBranch newBranch = new PriceBranch(invalidList, validList, groupList);
        priceBranchData.calculateNewInfo(newBranch, this);
        this.branches.add(newBranch);
    }

    public record PriceBranch(Set<ItemData> invalidList, Set<ItemData> validList, List<PriceGroup> priceGroupList) {
    }

    public static String getItemName(ItemData data) {
        if (data == null) return "[Empty Item]";
        return data.mainItem.getDisplayName().getString();
    }

    public static String getItemDataNames(Collection<ItemData> dataList) {
        StringBuilder builder = new StringBuilder();
        new ArrayList<>(dataList).forEach(itemData -> builder.append(getItemName(itemData)));
        return builder.toString();
    }

    public static String getIngredientItemNames(Collection<IngredientData> ingredientDataList) {
        int count = 1;
        StringBuilder builder = new StringBuilder();
        for (IngredientData data : ingredientDataList) {
            builder.append("Ingredient ").append(count).append(":").append(getItemDataNames(data.myItemData.keySet()));
            count++;
        }
        return builder.toString();
    }
}
