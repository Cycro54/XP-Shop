package invoker54.xpshop.data.recipe;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RecipeData {
    public static ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public static final List<RecipeData> allRecipeData = Collections.synchronizedList(new ArrayList<>());
    public final Recipe<?> recipe;
    public final PriceGroup myGroup;
    //This is how much of an item the recipe will make. (Example: 1 raw iron block will make 9 raw iron)
    public final int resultCount;
    public boolean isBadRecipe = false;
    private static final RecipeData fakeRecipeData = new RecipeData();
    //Count is the amount of similar ingredients
    public final Map<IngredientData, AtomicInteger> myIngredientData = new HashMap<>();

    private RecipeData(Pair<Recipe<?>, List<Ingredient>> recipePair) {
        this.recipe = recipePair.getKey();
        this.resultCount = recipePair.getKey().getResultItem().getCount();
        allRecipeData.add(this);
        String itemName = recipePair.getKey().getResultItem().getDisplayName().getString();

        PriceGroup.GroupOperation recipeOP = ((list) -> {
            PriceList.PriceInfo resultInfo = PriceList.emptyPrice;
            for (PriceList.PriceInfo listInfo : list) {
                //If one of the ingredients end up being 0, it's a bad recipe.
                if (listInfo.highPrice() == 0) return PriceList.emptyPrice;
                resultInfo = resultInfo.add(listInfo);
            }
            return resultInfo.calculate((x) -> x / this.resultCount);
        });
        this.myGroup = new PriceGroup(null, new HashSet<>(), recipeOP);
        List<PriceGroup> groupList = new ArrayList<>();
        for (Ingredient ingredient : recipePair.getRight()) {
            //LOGGER.warn(itemName+" items to look for: " + ItemData.getItemNames(List.of(ingredient.getItems())));
            IngredientData matchingData = IngredientData.getMatchingData(ingredient);
            if (matchingData.isBadIngredient) {
                LOGGER.warn(itemName + " Recipe had a bad ingredient, don't finish this recipe.");
                LOGGER.warn("Ingredients in question: " + ItemData.getItemDataNames(matchingData.myItemData.keySet()));
                this.isBadRecipe = true;
                this.myIngredientData.clear();
                return;
            }
            this.myIngredientData.putIfAbsent(matchingData, new AtomicInteger(0));
            this.myIngredientData.get(matchingData).addAndGet(1);
            //LOGGER.info(itemName+" Recipe ingredient count is now: " + this.myIngredientData.size());
            //LOGGER.info(itemName+" Count for that ingredient is: " + this.myIngredientData.get(matchingData));
        }
        for (var entry : this.myIngredientData.entrySet()) {
            PriceGroup.GroupOperation op = ((list) -> {
                //Finally compile into one priceInfo, then multiply it by the count in the recipe
                return PriceList.compilePriceListData("Ingredient", list)
                        .calculate(x -> x * entry.getValue().get());
            });
            groupList.add(new PriceGroup(null, new HashSet<>(List.of(entry.getKey().myGroup)), op));
        }
        this.myGroup.setGroups(groupList);
        LOGGER.debug(itemName + " Created new recipe");
    }

    private RecipeData(){
        this.myGroup = new PriceGroup(null, new HashSet<>(), null);
        this.recipe = null;
        this.resultCount = 1;
        LOGGER.info("This is a fake recipe.");
    }

//    public List<PriceGroup> getPriceList(Map<IngredientData, List<PriceGroup>> cacheMap, List<ItemData> unmodifiedCopy,
//                                         List<ItemData> mainCheckList, HashSet<ItemData> invalidList, ItemData mainItem) {
//        List<PriceGroup> recipeGroupList = new ArrayList<>();
//
//        for (var ingEntry : this.myIngredientData.entrySet()) {
//            List<PriceGroup> ingredientGroupList =
//                    cacheMap.getOrDefault(ingEntry.getKey(), new ArrayList<>());
//
//            if (ingredientGroupList.isEmpty()) {
//                ArrayList<ItemData> newGroupList = new ArrayList<>(ingEntry.getKey().myItemData.keySet());
//                newGroupList.removeIf((item) -> {
//                    if (!unmodifiedCopy.contains(item)) return false;
//                    invalidList.add(item);
//                    return true;
//                });
////                LOGGER.info("What are the items in unmodifiedCopy BEFORE: " + ItemData.getItemDataNames(unmodifiedCopy));
//                List<ItemData> recipeCopy = new ArrayList<>(unmodifiedCopy);
////                LOGGER.info("What are the items in recipeCopy AFTER: " + ItemData.getItemDataNames(recipeCopy));
//
//                try {
//                    ingredientGroupList = ingEntry.getKey().getPriceList
//                            (recipeCopy, newGroupList, invalidList, mainItem);
//                }
//                catch (Exception e){
//                    LOGGER.error("What are the items in unmodifiedCopy: " + ItemData.getItemDataNames(unmodifiedCopy));
//                    LOGGER.error("What are the items in recipeCopy: " + ItemData.getItemDataNames(recipeCopy));
//                    throw e;
//                }
//
//
//                //Add all the recorded ingredients from the recipeCopy set to the main hashSet
//                recipeCopy.removeAll(mainCheckList);
//                mainCheckList.addAll(new HashSet<>(recipeCopy));
//                //Add it to the cached list for later
//                cacheMap.put(ingEntry.getKey(), ingredientGroupList);
//            } else {
//                LOGGER.debug("Found a cached ingredient, using it!");
//            }
//
//            //This is for each ingredient in the recipe
//            //This will just be made up of each item in the ingredient
//            PriceGroup.GroupOperation op = ((list) -> {
//                //First remove any ingredient that's 0
//                list.removeIf(info -> info.highPrice() == 0);
//                //Next remove outliers
//                PriceList.sortAndRemoveOutliers(list);
//                //Finally compile into one priceInfo, then multiply it by the count in the recipe
//                return PriceList.compilePriceListData("Ingredient", list)
//                        .calculate(x -> x * ingEntry.getValue().get());
//            });
//            PriceGroup ingredientGroup = new PriceGroup(null, ingredientGroupList, op, null);
//            recipeGroupList.add(ingredientGroup);
//        }
//        return recipeGroupList;
//    }

    public static Set<PriceGroup> createFakeGroup(List<Ingredient> ingredientList){
        Set<PriceGroup> groupList = new HashSet<>();
        for (Ingredient ingredient : ingredientList) {
            groupList.add(PriceGroup.duplicate(IngredientData.getMatchingData(ingredient).myGroup,
                    new HashSet<>(), new HashSet<>()));
        }
        return groupList;
    }

    public static RecipeData getMatchingData(Pair<Recipe<?>, List<Ingredient>> recipePair) {
        synchronized (allRecipeData) {
            for (RecipeData data : allRecipeData) {
                if (data.recipe.getId().equals(recipePair.getLeft().getId())) {
                    //LOGGER.warn(recipePair.getKey().getResultItem().getDisplayName().getString()
//                        +" Found matching recipe");
                    return data;
                }
            }

            //LOGGER.debug(recipePair.getKey().getResultItem().getDisplayName().getString()
//                +" Found no matching recipes, making new recipe data...");
            return new RecipeData(recipePair);
        }
    }
}
