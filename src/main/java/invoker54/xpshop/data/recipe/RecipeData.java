package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;

public class RecipeData{
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    //This is how much of an item the recipe will make. (Example: 1 raw iron block will make 9 raw iron)
    public boolean isBadRecipe = false;
    public final boolean isBrewingRecipe;
    public final ItemStack resultStack;
    public final ItemData mainData;
    public final String recipeType;
    //Count is the amount of similar ingredients
    public final List<IngredientBunch> ingredientList = new ArrayList<>();
    public record IngredientBunch(IngredientData data, int count){}

    public RecipeData(ShopGenerationEvent.RecipeInfo recipeInfo, ItemData mainData) {
        this.isBrewingRecipe = recipeInfo.resultStack().getItem() instanceof PotionItem;
        this.mainData = mainData;
        recipeType = recipeInfo.recipeType();
        resultStack = recipeInfo.resultStack();
        Map<IngredientData, Integer> ingredientMap = new HashMap<>();

        for (Ingredient ingredient : recipeInfo.ingredients()) {
            IngredientData matchingData = IngredientData.getMatchingData(ingredient);
            if (matchingData.isBadIngredient) {
                LOGGER.warn(ItemData.getItemName(mainData) + " Recipe had a bad ingredient, don't finish this recipe.");
                this.isBadRecipe = true;
                this.ingredientList.clear();
                return;
            }
            ingredientMap.putIfAbsent(matchingData, 0);
            ingredientMap.put(matchingData, ingredientMap.get(matchingData) + 1);
        }



        ingredientMap.forEach((key, value) -> ingredientList.add(new IngredientBunch(key, value)));
    }

    public PriceList.PriceInfo getResult(Set<ItemData> frozenSet) {
        List<MutablePair<Set<ItemData>, PriceList.PriceInfo>> branchList = new ArrayList<>();

        for (IngredientBunch bunch : this.ingredientList) {
            Set<ItemData> invalidSet = new HashSet<>();
//            boolean badItems = true;
            //Fill out the invalid set
            bunch.data.fillOutInvalidSet(frozenSet, invalidSet);
            if (!invalidSet.isEmpty())  this.mainData.hasBadSet = true;
            IngredientData.PriceBranch branch = bunch.data.getBranch(invalidSet);

            if (branch != null){
                if (branch.finalInfo().highPrice() != 0){
                    branchList.add(new MutablePair<>(invalidSet, branch.finalInfo()));
                    continue;
                }
                LOGGER.warn("This is a bad branch");
                return PriceList.emptyPrice;
            }
            branchList.add(new MutablePair<>(invalidSet, null));
//            else {
//                for (ItemData item : bunch.data.myItemData.keySet()) {
//                    if (!bunch.invalidSet.contains(item)) {
//                        branchList.add(new MutablePair<>(bunch, null));
//                        badItems = false;
//                        break;
//                    }
//                }
//            }
//
//            if (badItems) {
//                LOGGER.error(bunch.data.ingredientName+" Had bad items");
//                bunch.data.addBranch(mainData, bunch.invalidSet, PriceList.emptyPrice);
//                return PriceList.emptyPrice;
//            }
        }

        PriceList.PriceInfo resultInfo = PriceList.emptyPrice;
        int a = -1;
        for (var pair : branchList) {
            a++;
            if (pair.getRight() == null) {
                pair.setValue(this.ingredientList.get(a).data.getResult(mainData, pair.getLeft(), frozenSet));
                if (pair.getRight().highPrice() == 0) {
                    if (isBrewingRecipe) {
                        resultInfo.add(new PriceList.PriceInfo(recipeType, XPShopConfig.xpPerOddity, XPShopConfig.xpPerOddity));
                        continue;
                    } else return PriceList.emptyPrice;
                }
            }

            int finalA = a;
            resultInfo = resultInfo.add(pair.getRight().calculate(x -> x * this.ingredientList.get(finalA).count));
        }
        return resultInfo.calculate((x) -> x / this.resultStack.getCount());
    }
}