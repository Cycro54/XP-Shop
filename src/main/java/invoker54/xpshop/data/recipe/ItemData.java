package invoker54.xpshop.data.recipe;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import invoker54.xpshop.util.MiniTicker;
import invoker54.xpshop.util.ModStopWatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static invoker54.xpshop.event.generation.ShopGenerationEvent.*;

public class ItemData {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<ItemData> allItemDataList = Collections.synchronizedList(new ArrayList<>());
    public final ItemStack mainItem;
    public final PriceList priceList;
    public PriceList.PriceInfo baseInfo;
    private final String name;
    private final List<RecipeData> myRecipeData = new ArrayList<>();
    public static final ModStopWatch STOP_WATCH = ModStopWatch.getTimer(XPShop.MOD_ID, "Item price info", ModStopWatch.Time.LONGEST);
    public final PriceGroup myGroup;
    public Set<ItemData> invalidIngredients = new HashSet<>();
    public final HashMap<Integer, List<PriceBranch>> branches = new HashMap<>();
    private boolean hasRecipes = false;
    private ItemData(ItemStack mainItem) {
        name = mainItem.getDisplayName().getString();
        allItemDataList.add(this);
        this.mainItem = mainItem;
        this.priceList = ShopGenerationEvent.getPriceList(this.mainItem);
        this.baseInfo = this.priceList.getFinalPriceInfo();
        PriceGroup.GroupOperation myOP = ((list) -> {
            //Remove all prices that are 0
            list.removeIf((info) -> info.highPrice() == 0);
            //remove all prices that are WAY too high
            PriceList.sortAndRemoveOutliers(list);
            //Return a compiled price info object
            return PriceList.compilePriceListData(getItemName(this), list);
        });
        this.myGroup = new PriceGroup(null, new HashSet<>(), myOP);
        List<PriceGroup> groupList = new ArrayList<>();
        ItemStack matchingStack = getMatchingItemStack(mainItem, craftResultMap.keySet());
        InitialRecipeData initialRecipeData = matchingStack == null ? null : craftResultMap.get(matchingStack);
        if (initialRecipeData != null){
            this.hasRecipes = true;
            //go through each recipePair and create a new recipeData class instance
            for (Pair<Recipe<?>, List<Ingredient>> recipePair : initialRecipeData.recipes()) {
                RecipeData matchingData = RecipeData.getMatchingData(recipePair);
                if (matchingData.isBadRecipe){
                    continue;
                }
                groupList.add(matchingData.myGroup);
                this.myRecipeData.add(matchingData);
            }
        }
        this.myGroup.setGroups(groupList);

//        LOGGER.warn("Created a new item Data: " + getItemName(this));
    }

    private void gatherIngredients(){
        for (RecipeData recipe : this.myRecipeData){
            for (IngredientData ingredient : recipe.myIngredientData.keySet()){
                this.invalidIngredients.addAll(ingredient.myItemData.keySet());
            }
        }
//        LOGGER.info(getItemName(this) +" ingredient list: " + getItemDataNames(this.invalidIngredients));
    }

    public boolean hasRecipes(){
        return this.hasRecipes;
    }

    public void addBranch(Set<ItemData> invalidSet, PriceGroup finalGroup){
        this.branches.putIfAbsent(invalidSet.size(), new ArrayList<>());
        List<PriceBranch> branchList = this.branches.get(invalidSet.size());
        LOGGER.warn(ItemData.getItemName(this)+"Creating a new branch, invalid list: " + getItemDataNames(invalidSet));
        PriceBranch newBranch = new PriceBranch(invalidSet, finalGroup);
        priceBranchData.calculateNewInfo(newBranch, this);
        branchList.add(newBranch);
//        this.invalidIngredients.addAll(invalidSet);
    }

    public PriceBranch getBranch(Set<ItemData> checkList, PriceGroup currGroup){
        if (!hasRecipes) return null;
        if (this.invalidIngredients.isEmpty()) gatherIngredients();
        MiniTicker<?> ticker = ModStopWatch.getTimer(XPShop.MOD_ID, "CheckBranch", ModStopWatch.Time.LONGEST).ticker();

        Set<ItemData> currentInvalidSet = new HashSet<>(checkList);
        //Remove all items that are not in the invalid ingredient list.
        currentInvalidSet.removeIf(item -> !this.invalidIngredients.contains(item));
        for (int a = currentInvalidSet.size(); a >= 0; a--){
            PriceBranch savedBranch = null;
            for (PriceBranch branch : this.branches.getOrDefault(a, Collections.emptyList())){
                if (!currentInvalidSet.containsAll(branch.invalidList)){
//                    LOGGER.info(getItemName(this)+"Not compatible" + "\n M-list:" +getItemDataNames(currentInvalidSet) +
//                            "\n B-list:"+getItemDataNames(branch.invalidList));
                    continue;
                }
                savedBranch = branch;
                if (branch.group.sameOps(currGroup)){
                    break;
                }
            }

            if (savedBranch == null) continue;
            if (!savedBranch.group.sameOps(currGroup) &&
            savedBranch.invalidList.size() == currentInvalidSet.size()){
                savedBranch = new PriceBranch(savedBranch.invalidList, savedBranch.group.setOp(currGroup));
                this.branches.get(currentInvalidSet.size()).add(savedBranch);
            }

//            LOGGER.info(getItemName(this)+"Found a branch! Br size:" + savedBranch.invalidList.size() +
//                    ", My size:"+currentInvalidSet.size());
            String returnedString = ticker.record("Finding the correct branch." +
                    "\ninvalid list size: " + currentInvalidSet.size() +
                    "\nWas it the perfect size? " + (savedBranch.invalidList.size() == currentInvalidSet.size()));

            return savedBranch;
//            return savedBranch.group.duplicate(checkList, new HashSet<>(savedBranch.invalidList), savedBranch);
        }
        return null;
    }

    public static ItemData getMatchingData(ItemStack stack) {
        synchronized (allItemDataList) {
            for (ItemData itemData : allItemDataList) {
                if (getMatchingItemStack(stack, List.of(itemData.mainItem)) != null)
                    return itemData;
            }
//            LOGGER.error(stack.getDisplayName().getString() + " has no item data, creating new one...");
            return new ItemData(stack);
        }
    }

    public record PriceBranch(Set<ItemData> invalidList, PriceGroup group) {
    }

    public static String getItemName(ItemData data) {
        if (data == null) return "[Empty Item]";
        return data.name;
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
