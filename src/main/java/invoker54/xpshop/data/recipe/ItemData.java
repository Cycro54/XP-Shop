package invoker54.xpshop.data.recipe;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import invoker54.xpshop.util.ModStopWatch;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static invoker54.xpshop.event.generation.ShopGenerationEvent.*;

public class ItemData{
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<ItemData> allItemDataList = Collections.synchronizedList(new ArrayList<>());
    public final ItemStack mainItem;
    public final PriceList priceList;
    public PriceList.PriceInfo baseInfo;
    public SortedSet<Set<ItemData>> badSets;
    private final String name;
    public static final ModStopWatch STOP_WATCH = ModStopWatch.getTimer(XPShop.MOD_ID, "Item price info", ModStopWatch.Time.LONGEST);
    public Set<ItemData> validIngredients = new HashSet<>();
    public final List<RecipeData> recipeList;
    //All the itemData's that use this
    public final Set<ItemData> userSet = new HashSet<>();
    private boolean hasAllIngredients = false;
    public boolean hasBadSet = false;
    public boolean hasPrice = false;
    public boolean isBadItem = false;
    public static final Map<Integer, Integer> depthMap = new HashMap<>();
    public static int currentDepth = 0;
    private ItemData(ItemStack mainItem) {
        badSets = new TreeSet<>(Comparator.comparingInt(Set::size));
        name = mainItem.getDisplayName().getString();
        LOGGER.debug("New itemData: " + name);
        allItemDataList.add(this);
        this.mainItem = mainItem;
        this.priceList = ShopGenerationEvent.getPriceList(this.mainItem);
        this.baseInfo = this.priceList.getFinalPriceInfo();

        //Remove all prices that are 0
        //remove all prices that are WAY too high
        //Return a compiled price info object
        List<RecipeData> groupList = new ArrayList<>();
        ItemStack matchingStack = getMatchingItemStack(mainItem, craftResultMap.keySet());
        List<RecipeInfo> recipeInfoList = matchingStack == null ? null : craftResultMap.get(matchingStack);
        if (recipeInfoList != null){
            //go through each recipePair and create a new recipeData class instance
            for (RecipeInfo recipeInfo : recipeInfoList) {
                RecipeData matchingData = new RecipeData(recipeInfo, this);
                groupList.add(matchingData);
            }
            this.recipeList = groupList;
        }
        else this.recipeList = Collections.emptyList();
        if (this.recipeList.isEmpty()) hasAllIngredients = true;
    }

    public List<PriceList.PriceInfo> getResultList(Set<ItemData> frozenSet){
        currentDepth++;
        if (!depthMap.containsKey(currentDepth)){
            LOGGER.warn("New depth achieved: " + currentDepth);
            depthMap.put(currentDepth, 1);
        }
        else depthMap.put(currentDepth, depthMap.get(currentDepth) + 1);

        List<PriceList.PriceInfo> infoList = new ArrayList<>();
        for (RecipeData group : this.recipeList){
            PriceList.PriceInfo info = group.getResult(frozenSet);
            if (info.highPrice() != 0) {
                hasPrice = true;
                infoList.add(info);
            }
        }

        if (!hasBadSet && !hasPrice){
            LOGGER.warn("This is a bad item: " + getItemName(this));
            isBadItem = true;
        }
        else {
            hasBadSet = false;
            hasPrice = false;
        }

        currentDepth--;
        return infoList;
    }

    public PriceList.PriceInfo getResult(Set<ItemData> frozenSet) {
        return this.calculate(this.getResultList(frozenSet));
    }

    private PriceList.PriceInfo calculate(List<PriceList.PriceInfo> infoList){
        infoList = new ArrayList<>(infoList);
        infoList.add(this.baseInfo);

        //Remove all prices that are 0
        infoList.removeIf((info) -> info.highPrice() == 0);
        //remove all prices that are WAY too high
        PriceList.sortAndRemoveOutliers(infoList);
        //Return a compiled price info object
        return PriceList.compilePriceListData(getItemName(getMatchingData(mainItem)), infoList);
    }

    public void gatherBaseIngredients(){
        if (hasAllIngredients) return;

        if (this.validIngredients.isEmpty()){
            for (RecipeData rData : this.recipeList){
                for (RecipeData.IngredientBunch bunch : rData.ingredientList){
                    for (ItemData itemdata : bunch.data().myItems){
                        this.validIngredients.add(itemdata);
                        itemdata.userSet.add(this);
                    }
                }
            }

            for (ItemData data : this.validIngredients){
                data.gatherBaseIngredients();
            }
        }
        hasAllIngredients = true;
    }

    public static ItemData getMatchingData(ItemStack stack) {
        synchronized (allItemDataList) {
            for (ItemData itemData : allItemDataList) {
                if (getMatchingItemStack(stack, List.of(itemData.mainItem)) != null)
                    return itemData;
            }
            return new ItemData(stack);
        }
    }

    public static String getItemName(ItemData data) {
        if (data == null) return "[Empty Item]";
        return data.name;
    }

    public static String getItemDataNames(Collection<ItemData> dataList) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (ItemData data : dataList){
            if (count == 3){
                builder.append("...");
                break;
            }
            builder.append(getItemName(data));
            count++;
        }
        return builder.toString();
    }

//    public static String getIngredientItemNames(Collection<IngredientData> ingredientDataList) {
//        int count = 1;
//        StringBuilder builder = new StringBuilder();
//        for (IngredientData data : ingredientDataList) {
//            builder.append("Ingredient ").append(count).append(":").append(getItemDataNames(data.myItemData.keySet()));
//            count++;
//        }
//        return builder.toString();
//    }
}
