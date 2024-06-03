package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

import static invoker54.xpshop.data.recipe.ItemData.getItemDataNames;
import static invoker54.xpshop.event.generation.ShopGenerationEvent.priceBranchData;

public class IngredientData{
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<IngredientData> allIngredientDataList = Collections.synchronizedList(new ArrayList<>());
    public static List<IngredientData> unfinishedList = new ArrayList<>();
    public final Ingredient mainIngredient;
    public boolean isBadIngredient = false;
    public final Map<Integer, List<PriceBranch>> branchMap = new HashMap<>();
    public static final PriceBranch emptyBranch = new PriceBranch(new HashSet<>(), PriceList.emptyPrice);
    public final List<Set<ItemData>> badList = new ArrayList<>();
    public String ingredientName;
    public int branchCount = 0;
   public List<DataGroup> chosenList;
   public final List<List<DataGroup>> bunchedGroups = new ArrayList<>();
   public final List<ItemData> myItems = new ArrayList<>();
   public record DataGroup(ItemData itemData, int count){};
    private IngredientData(Ingredient mainIngredient){
        allIngredientDataList.add(this);
        this.mainIngredient = mainIngredient;
        List<DataGroup> baseList = new ArrayList<>();

        for (ItemStack ingredientStack : mainIngredient.getItems()){
            int count = ingredientStack.getCount();
            ingredientStack.setCount(1);

            ItemData matchingData = ItemData.getMatchingData(ingredientStack);
            myItems.add(matchingData);
            baseList.add(new DataGroup(matchingData, count));
        }
        if (baseList.isEmpty()) this.isBadIngredient = true;
        else{
            unfinishedList.add(this);
            this.bunchedGroups.add(new ArrayList<>(baseList));
        }
    }

    public static void initialize() {
        for (var ingData : unfinishedList) {
            if (ingData.bunchedGroups.isEmpty()){
                LOGGER.warn("This is empty??? bad ingredient? " + ingData.isBadIngredient);
                continue;
            }
            List<DataGroup> baseList = ingData.bunchedGroups.get(0);
            ingData.bunchedGroups.clear();

            baseList.sort(Comparator.comparingInt(A -> -A.itemData.userSet.size()));
            List<DataGroup> list = new ArrayList<>();
            int count = -1;
            for (var dataGroup : baseList) {
                LOGGER.warn(ItemData.getItemName(dataGroup.itemData)+" user size: " + dataGroup.itemData.userSet.size());
                if (dataGroup.itemData.userSet.size() != count) {
                    if (list.size() > 3){
                        int squareRoot = (int) Math.sqrt(list.size());
                        int splitCount = 1;
                        LOGGER.info("List is too long, length is: "+list.size()+", sqrt is:"+squareRoot);
                        while (list.size() > squareRoot){
                            List<DataGroup> subGroup = new ArrayList<>(list.subList(0, squareRoot));
                            ingData.bunchedGroups.add(ingData.bunchedGroups.indexOf(list), subGroup);
                            list.removeAll(subGroup);
                            splitCount++;
                        }
                        LOGGER.info("Split list into " + splitCount + " pieces.");
                    }

                    list = new ArrayList<>();
                    ingData.bunchedGroups.add(list);
                    count = dataGroup.itemData.userSet.size();
                }
                list.add(dataGroup);
            }

            ingData.ingredientName = getItemDataNames(baseList.stream().map(DataGroup::itemData).toList());
            LOGGER.warn(ingData.ingredientName+" How many sections? " +
                    ingData.bunchedGroups.size() + ", Total items? " + ingData.myItems.size());
        }
        unfinishedList = new ArrayList<>();
    }

    public PriceList.PriceInfo getResult(ItemData mainData, Set<ItemData> invalidSet, Set<ItemData> prevFrozenSet) {
        if (this.bunchedGroups.isEmpty()) return PriceList.emptyPrice;
        if (chosenList == null){
            int tries = Math.max(this.bunchedGroups.size()/2, 1);
            for (var list : this.bunchedGroups){
                chosenList = list;
                PriceList.PriceInfo info = this.getResult(mainData, invalidSet, prevFrozenSet);
                if (info.highPrice() != 0){
                    chosenList = null;
                    return info;
                }
                tries--;
                if (tries == 0) break;
            }
            return PriceList.emptyPrice;
        }

        List<DataGroup> addedItemList = new ArrayList<>();
        Set<ItemData> newFrozenSet = new HashSet<>(prevFrozenSet);

        this.chosenList.forEach((group) -> {
            if (!prevFrozenSet.contains(group.itemData) && ShopGenerationEvent.isSkipped(group.itemData.mainItem) != 0) {
                addedItemList.add(group);
                newFrozenSet.add(group.itemData);
            }
        });
        PriceList.PriceInfo finalInfo = PriceList.emptyPrice;

        if (!addedItemList.isEmpty()){
            List<PriceList.PriceInfo> infoList = new ArrayList<>();
            for (var group : addedItemList){
                if (group.itemData.isBadItem){
                    LOGGER.warn(ingredientName+" Item was bad: " + ItemData.getItemName(group.itemData));
                    continue;
                }
                infoList.add(group.itemData.getResult(newFrozenSet).calculate(x -> x*group.count()));
            }

            //First remove any ingredient item that's 0
            infoList.removeIf(info -> info.highPrice() == 0);
            //Next remove outliers
            PriceList.sortAndRemoveOutliers(infoList);
            //Finally compile into one priceInfo, and give it to the recipe group
            finalInfo = PriceList.compilePriceListData("Ingredient", infoList);
        }

        this.addBranch(mainData, invalidSet, finalInfo);
        return finalInfo;
    }

    public void addBranch(ItemData itemData, Set<ItemData> invalidSet, PriceList.PriceInfo finalInfo){
        if (finalInfo.highPrice() == 0){
            LOGGER.warn(ingredientName+" This was a bad batch: " + getItemDataNames(invalidSet));
            badList.add(new HashSet<>(invalidSet));
            return;
        }

        this.branchMap.putIfAbsent(invalidSet.size(), new ArrayList<>());
        List<PriceBranch> branchList = branchMap.get(invalidSet.size());

        for (PriceBranch branch : branchList){
            if (branch.finalInfo.equals(finalInfo)){
                List<ItemData> itemsToAdd = invalidSet.stream().
                        filter(item -> !branch.invalidSet.contains(item)).toList();
                LOGGER.warn(ingredientName+branch.finalInfo+" Found a match, adding these items...("+invalidSet.size()+
                        ") ("+itemsToAdd.size()+")"+ getItemDataNames(itemsToAdd));
                branch.invalidSet.addAll(invalidSet);
                return;
            }
        }
        LOGGER.error(ingredientName+"Cause of new branch: " + ItemData.getItemName(itemData));

        PriceBranch newBranch = new PriceBranch(invalidSet, finalInfo);

        branchCount += 1;
        priceBranchData.calculateNewInfo(newBranch, itemData, this);
        branchMap.get(invalidSet.size()).add(newBranch);
    }

    public PriceBranch getBranch(Set<ItemData> invalidSet) {
        for (var set : this.badList){
            if (set.size() > invalidSet.size()) continue;
            if (!invalidSet.containsAll(set)) continue;
            LOGGER.error(ingredientName+"Bad Set("+set.size()+
                    "), Invalid Set("+invalidSet.size()+") " + getItemDataNames(invalidSet));
            return emptyBranch;
        }

        List<PriceBranch> branchList = branchMap.getOrDefault(invalidSet.size(), Collections.emptyList());
        for (PriceBranch branch : branchList){
            if (!branch.invalidSet.containsAll(invalidSet)) continue;
            LOGGER.debug(ingredientName+" found branch("+branch.invalidSet.size()+"): "+ getItemDataNames(invalidSet));
            return branch;
        }
        return null;
    }

    public void fillOutInvalidSet(Set<ItemData> frozenSet, Set<ItemData> invalidSet){
        invalidSet.clear();
        //This is all the items we already went through.
        Set<ItemData> oldData = new HashSet<>();
        //This is the current batch of items
        Set<ItemData> newData = new HashSet<>(this.myItems);
//        LOGGER.info(this.ingredientName+" The beginning items to check: " + getItemDataNames(this.myItems));
//        LOGGER.info(this.ingredientName+" Frozen set: " + getItemDataNames(frozenSet));
        do {
            //This is the next batch of items
            Set<ItemData> nextData = new HashSet<>();
            for (ItemData item : newData){
                if (oldData.contains(item)) continue;
                oldData.add(item);
                if (frozenSet.contains(item)){
//                    LOGGER.warn(this.ingredientName+" item was in frozen set: "+ItemData.getItemName(item));
                    invalidSet.add(item);
                }
                else{
//                    LOGGER.warn(this.ingredientName+" frozen set: "+ItemData.getItemDataNames(frozenSet));
//                    LOGGER.warn(this.ingredientName+" item in question: "+ItemData.getItemName(item));
                    nextData.addAll(item.validIngredients);
                }
            }
            newData = new HashSet<>(nextData);
//            LOGGER.warn(this.ingredientName+" new batch: "+ItemData.getItemDataNames(nextData));
        }
        while (!newData.isEmpty());
//        LOGGER.error(this.ingredientName+" This is the new invalidSet: " + getItemDataNames(invalidSet));
    }

    public static IngredientData getMatchingData(Ingredient ingredient){
        synchronized (allIngredientDataList) {
            for (IngredientData ingredientData : allIngredientDataList) {
                if (!sameIngredients(ingredient, ingredientData.mainIngredient)) continue;
                return ingredientData;
            }

            return new IngredientData(ingredient);
        }
    }

    public static boolean sameIngredients(Ingredient ingredientA, Ingredient ingredientB){
        if (ingredientA.getItems().length != ingredientB.getItems().length) return false;
        List<ItemStack> listB = List.of(ingredientB.getItems());
        for (ItemStack stack : ingredientA.getItems()){
            ItemStack matchingStack = ShopGenerationEvent.getMatchingItemStack(stack, listB);
            if (matchingStack == null) return false;
            if (matchingStack.getCount() != stack.getCount()) return false;
        }
        return true;
    }

    public record PriceBranch(Set<ItemData> invalidSet, PriceList.PriceInfo finalInfo) {
    }
}
