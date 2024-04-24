package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationCopyEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

public class IngredientData {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<IngredientData> allIngredientDataList = Collections.synchronizedList(new ArrayList<>());
    public final Ingredient mainIngredient;
    public boolean isBadIngredient = false;

    //The item and its count
    public final Map<ItemData, Integer> myItemData = new HashMap<>();
    private IngredientData(Ingredient mainIngredient){
        allIngredientDataList.add(this);
        this.mainIngredient = mainIngredient;

        for (ItemStack ingredientStack : mainIngredient.getItems()){
            int count = ingredientStack.getCount();
            ingredientStack.setCount(1);

            ItemData matchingData = ItemData.getMatchingData(ingredientStack);
            if (matchingData.baseInfo.highPrice() == 0 && matchingData.hasRecipes){
                LOGGER.warn(ItemData.getItemName(matchingData) + " is a bad item, skipping...");
                continue;
            }

            this.myItemData.putIfAbsent(matchingData, count);
        }
        LOGGER.info("My item list is " + ItemData.getItemDataNames(this.myItemData.keySet()));
        if (this.myItemData.isEmpty()) this.isBadIngredient = true;
    }
    public List<PriceGroup> getPriceList(List<ItemData> recipeCopy,
                                         List<ItemData> newGroup, List<ItemData> invalidList, ItemData mainItem){
        List<PriceGroup> ingredientGroupList = new ArrayList<>();
        List<ItemData> unmodifiedCopy = new ArrayList<>(recipeCopy);

        for (var itemEntry : this.myItemData.entrySet()) {
            if (!newGroup.contains(itemEntry.getKey())) continue;
            List<ItemData> ingredientCopy = new ArrayList<>(unmodifiedCopy);
            //This will make it so the item we are getting the price group from will be first on the list.
            try {
                Collections.swap(newGroup, newGroup.indexOf(itemEntry.getKey()), 0);
//                Collections.swap(modifiedCopy, modifiedCopy.indexOf(itemEntry.getKey()), unmodifiedCopy.size()-1);
                ingredientCopy.addAll(newGroup);
            }
            catch (Exception e){
                LOGGER.warn("Main Item: " + ItemData.getItemName(mainItem) + " \n" +
                        "First Item: " + ItemData.getItemName(ingredientCopy.get(0)) + " \n" +
                        "Unmodified: " + ItemData.getItemDataNames(recipeCopy) + " \n" +
                        "Modified: " + ItemData.getItemDataNames(ingredientCopy));
                throw e;
            }


            List<PriceGroup> itemGroup = itemEntry.getKey().getPriceList(mainItem, ingredientCopy, invalidList);
            ingredientCopy.removeAll(recipeCopy);
            recipeCopy.addAll(ingredientCopy);
            //This is for each item in the ingredient
            //The list is made up of the item's base info, and recipes result info
            PriceGroup.GroupOperation op = ((list) -> {
                list.removeIf((info) -> info.highPrice() == 0);
                return PriceList.compilePriceListData(ItemData.getItemName(itemEntry.getKey()), list)
                        .calculate(x -> x * itemEntry.getValue());
            });
            ingredientGroupList.add(new PriceGroup(itemEntry.getKey(), itemGroup, op, null));
        }
        return ingredientGroupList;
    }


    public static IngredientData getMatchingData(Ingredient ingredient){
        synchronized (allIngredientDataList) {
            for (IngredientData ingredientData : allIngredientDataList) {
                if (!sameIngredients(ingredient, ingredientData.mainIngredient)) continue;

                LOGGER.error("Found matching ingredients!" + ItemData.getItemDataNames(ingredientData.myItemData.keySet()));
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
            ItemStack matchingStack = ShopGenerationCopyEvent.getMatchingItemStack(stack, listB);
            if (matchingStack == null) return false;
            if (matchingStack.getCount() != stack.getCount()) return false;
        }
        return true;
    }
}
