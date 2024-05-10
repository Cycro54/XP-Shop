package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

public class IngredientData {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<IngredientData> allIngredientDataList = Collections.synchronizedList(new ArrayList<>());
    public final Ingredient mainIngredient;
    public boolean isBadIngredient = false;
    public final PriceGroup myGroup;

    //The item and its count
    public final Map<ItemData, Integer> myItemData = new HashMap<>();
    private IngredientData(Ingredient mainIngredient){
        allIngredientDataList.add(this);
        this.mainIngredient = mainIngredient;
        PriceGroup.GroupOperation ingredientOP = ((list) -> {
            //First remove any ingredient that's 0
            list.removeIf(info -> info.highPrice() == 0);
            //Next remove outliers
            PriceList.sortAndRemoveOutliers(list);
            //Finally compile into one priceInfo, and give it to the recipe group
            return PriceList.compilePriceListData("Ingredient", list);
        });
        this.myGroup = new PriceGroup(null, new HashSet<>(), ingredientOP);
        List<PriceGroup> groupList = new ArrayList<>();

        for (ItemStack ingredientStack : mainIngredient.getItems()){
            int count = ingredientStack.getCount();
            ingredientStack.setCount(1);

            ItemData matchingData = ItemData.getMatchingData(ingredientStack);
            if (matchingData.baseInfo.highPrice() == 0 && !matchingData.hasRecipes()){
                LOGGER.warn(ItemData.getItemName(matchingData) + " is a bad item, skipping...");
                continue;
            }

            this.myItemData.putIfAbsent(matchingData, count);
        }
        for (var entry : this.myItemData.entrySet()){
            PriceGroup.GroupOperation itemOP = ((list) -> {
                list.removeIf((info) -> info.highPrice() == 0);
                return PriceList.compilePriceListData(ItemData.getItemName(entry.getKey()), list)
                        .calculate(x -> x * entry.getValue());
            });
            groupList.add(new PriceGroup(entry.getKey(), new HashSet<>(List.of(entry.getKey().myGroup)), itemOP));
        }
//        LOGGER.info("My item list is " + ItemData.getItemDataNames(this.myItemData.keySet()));
        if (this.myItemData.isEmpty()) this.isBadIngredient = true;
        this.myGroup.setGroups(groupList);
    }


    public static IngredientData getMatchingData(Ingredient ingredient){
        synchronized (allIngredientDataList) {
            for (IngredientData ingredientData : allIngredientDataList) {
                if (!sameIngredients(ingredient, ingredientData.mainIngredient)) continue;
//                LOGGER.error("Found matching ingredients!" + ItemData.getItemDataNames(ingredientData.myItemData.keySet()));
                return ingredientData;
            }

            LOGGER.debug("There isn't a matching dataIngredient, creating a new one...");

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
}
