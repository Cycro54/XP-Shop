//package invoker54.xpshop.event.generation.recipe;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import invoker54.xpshop.XPShop;
//import invoker54.xpshop.config.XPShopConfig;
//import invoker54.xpshop.data.CategoryEntry;
//import invoker54.xpshop.data.ModLogger;
//import invoker54.xpshop.data.PriceList;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.PotionItem;
//import net.minecraft.world.item.alchemy.Potion;
//import net.minecraft.world.item.alchemy.PotionBrewing;
//import net.minecraft.world.item.alchemy.PotionUtils;
//import net.minecraft.world.item.alchemy.Potions;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.Recipe;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.apache.commons.lang3.tuple.Pair;
//
//import java.util.*;
//
//import static invoker54.xpshop.event.generation.ShopGenerationEvent.*;
//
//@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
//public class CraftRecipeEvents {
//
//    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
//
//    public static void clearRecipePrices(ItemStack stack) {
//        ItemStack resultItem = getMatchingItemStack
//                (stack, craftResultMap.keySet());
//
//        if (resultItem == null) return;
//        getPriceList(resultItem).clearRecipes();
//    }
//
//    @SubscribeEvent
//    public static void priceRecipe(PriceRecipeEvent event) {
//        ItemStack currentItem = event.getCurrentItem();
//        String itemName = currentItem.getDisplayName().getString();
//
//        boolean alreadyPriced = isValidEntry(currentItem) || isSkipped(currentItem) != -1;
//        LOGGER.info("Pricing craft-able item: " + itemName);
//
//        InitialRecipeData initialRecipeData = event.getRecipeData();
//        if (initialRecipeData == null){
//            LOGGER.error(itemName + " has no recipes!");
////            if (!event.getPriceList().hasStats()) event.setHasBadRecipes();
//            return;
//        }
//        Map<ItemStack, priceResult> cachedItemCosts = new HashMap<>();
//        event.getInvalidIngredients().forEach((stack ->
//                cachedItemCosts.put(stack, new priceResult(0,0, !getPriceList(stack).hasStats()))));
//        cachedItemCosts.put(event.getCurrentItem(), new priceResult(0,0, true));
//
//        AtomicDouble counter = event.getPriceCounter();
//        PriceList priceList = event.getPriceList();
//
//        //Add the item stats if it has them.
//        PriceList.PriceInfo statInfo = priceList.getStatInfo();
//        event.addToSum(statInfo.price());
//        counter.addAndGet(statInfo.counter());
//
//        int recipeCount = 0;
//        boolean hasAllBadRecipes = true;
//
//        //Now go through all the recipes
//        for (Pair<Recipe<?>, List<Ingredient>> recipePair : new ArrayList<>(initialRecipeData.recipes())) {
//            //What makes a bad recipe?
//            //A recipe can be bad if none of its ingredients is affected by the item check list, and the cost is still 0.
//            boolean badRecipe = false;
//            recipeCount++;
//            LOGGER.error(itemName + " Recipe " + recipeCount);
//            Recipe<?> recipe = recipePair.getKey();
////            LOGGER.warn("[" + event.getItemList().size() + "]" + event.getCurrentItem().getDisplayName().getString() + " Recipe " + recipeCount);
////            LOGGER.debug("Recipe type: " + recipePair.left.getType());
//
//            double recipePriceCounterSum = 0;
//            double recipeSum = 0;
//            int ingredientCount = 0;
//            int resultCount = recipe.getResultItem().getCount();
//
//            for (Ingredient ingredient : recipePair.getRight()) {
//                ingredientCount += 1;
//
//                priceResult ingredientResult = getIngredientCost(ingredient, cachedItemCosts, event);
//
//                //This will skip the recipePair if the ingredientResult sum is 0.
//                if (ingredientResult.sum == 0) {
//                    LOGGER.error(itemName + " This recipePair can't be calculated. skip it");
//                    if (ingredientResult.badIngredient) badRecipe = true;
//                    recipeSum = 0;
//                    break;
//                } else {
//                    recipePriceCounterSum += ingredientResult.counter;
//                    recipeSum += ingredientResult.sum;
//                }
//            }
//
//            LOGGER.error(itemName + " is the recipe bad? " + badRecipe);
//
//            if (recipeSum != 0) {
//                LOGGER.error(itemName + " Recipe Price: " + (recipeSum/resultCount));
//                counter.addAndGet(recipePriceCounterSum / ingredientCount);
//                event.addToSum(recipeSum / resultCount);
//                if (!alreadyPriced)
//                    priceList.addRecipe("Recipe", recipeSum / resultCount,
//                             recipePriceCounterSum / ingredientCount);
//            }
//            if (!badRecipe) hasAllBadRecipes = false;
//        }
//        if (hasAllBadRecipes && !Objects.equals(event.getCurrentItem().getItem().getCreatorModId(event.getCurrentItem()), "minecraft")){
//            LOGGER.error(itemName + " has all bad recipes, skip it.");
//            event.setHasBadRecipes();
//        }
//    }
//
//    @SubscribeEvent
//    public static void onBrew(PriceRecipeEvent event) {
//        ItemStack currentItem = event.getCurrentItem();
//
////        LOGGER.info("Item class: " + currentItem.getItem().getClass());
//        if (!(currentItem.getItem() instanceof PotionItem)) return;
//        Map<ItemStack, priceResult> cachedItemCosts = new HashMap<>();
//        event.getInvalidIngredients().forEach((stack ->
//                cachedItemCosts.put(stack, new priceResult(0,0, !getPriceList(stack).hasStats()))));
//        cachedItemCosts.put(event.getCurrentItem(), new priceResult(0,0, true));
//
//        double potionSum = 0;
//        double priceCounterSum = 0;
//        int ingredientCount = 0;
////        LOGGER.info("Potion type: " + currentItem.getItem().getClass());
//        assignCategory(event.getCurrentItem(), CategoryEntry.POTIONS);
//
//        //This will price the potion type (regular, splashing, and lingering)
//        if (!currentItem.getItem().getClass().equals(PotionItem.class)) {
//            Item potionTypeItem = currentItem.getItem();
//            while (!(potionTypeItem.getClass().equals(PotionItem.class))) {
//                boolean foundMatch = false;
//
//                for (PotionBrewing.Mix<Item> mix : PotionBrewing.CONTAINER_MIXES) {
//                    if (potionTypeItem.equals(mix.to.get())) {
//                        foundMatch = true;
//
//                        priceResult priceResult = getIngredientCost(mix.ingredient, cachedItemCosts, event);
//                        if (priceResult.sum == 0) priceResult = new priceResult(XPShopConfig.xpPerOddity,
//                                1, priceResult.badIngredient);
//
//                        potionSum += priceResult.sum;
//                        priceCounterSum += priceResult.counter;
//                        ingredientCount += 1;
//                        potionTypeItem = mix.from.get();
//                    }
//                }
//
//                if (!foundMatch) {
//                    LOGGER.error("Couldn't find a matching potion container: " + potionTypeItem.getClass());
//                    break;
//                }
//            }
//        }
//
//        //This prices the actual potion
//        Potion potion = PotionUtils.getPotion(event.getCurrentItem());
//        while (potion != Potions.WATER) {
//            boolean foundMatch = false;
//
//            for (PotionBrewing.Mix<Potion> mix : PotionBrewing.POTION_MIXES) {
//                if (potion.equals(mix.to.get())) {
//                    foundMatch = true;
//                    priceResult priceResult = getIngredientCost(mix.ingredient, cachedItemCosts, event);
//                    if (priceResult.sum == 0) priceResult = new priceResult(XPShopConfig.xpPerOddity,
//                            1, priceResult.badIngredient);
//
//                    potionSum += priceResult.sum;
//                    priceCounterSum += priceResult.counter;
//                    ingredientCount += 1;
//                    potion = mix.from.get();
//                }
//            }
//
//            if (!foundMatch) break;
//        }
//
//        //Get the price of the bottle
//        if (potion == Potions.WATER) {
//            PriceRecipeEvent recipeEvent = calculateRecipePrice(new ItemStack(Items.GLASS_BOTTLE), new ArrayList<>());
//            PriceList.PriceInfo priceInfo = recipeEvent.getPriceList().getUneditedPriceInfo();
//
//            potionSum += priceInfo.price();
//            priceCounterSum += priceInfo.counter();
//            ingredientCount += 1;
//        }
//
//        //If the potion isn't craftable, skip it.
//        if (ingredientCount == 0) return;
//
//        //Divide potion sum by 3 since a brewing stand can make 3 potions at once with 1 ingredient
//        event.getPriceList().addRecipe("Potion value", potionSum / 3F,priceCounterSum / ingredientCount);
//    }
//
//    public static priceResult getIngredientCost(Ingredient ingredient, Map<ItemStack, priceResult> cachedItems,
//                                                PriceRecipeEvent currEvent) {
//        int workingItemCount = 0;
//        boolean badIngredient = true;
//        List<PriceList.PriceInfo> priceInfoList = new ArrayList<>();
//
//        List<ItemStack> fullCheckList = new ArrayList<>(currEvent.getItemList());
//        fullCheckList.addAll(List.of(ingredient.getItems()));
//
////        List<ItemStack> invalidIngredients = new ArrayList<>(currEvent.getInvalidIngredients());
////        invalidIngredients.add(currEvent.getCurrentItem());
//        for (ItemStack ingredientStack : ingredient.getItems()) {
//            //Check if the item was already priced, if it was just use that instead.
//            ItemStack matchingStack = getMatchingItemStack(ingredientStack, cachedItems.keySet());
//            if (matchingStack != null){
//                priceResult priceResult = cachedItems.get(matchingStack);
//                if (!priceResult.badIngredient) badIngredient = false;
//                if (priceResult.sum == 0) continue;
//                priceInfoList.add(new PriceList.PriceInfo("ingredient",
//                        priceResult.sum * ingredientStack.getCount(), priceResult.counter()));
//                workingItemCount++;
//                LOGGER.error(currEvent.getCurrentItem().getDisplayName().getString() + " Found cached item: " + ingredientStack.getDisplayName().getString());
//                continue;
//            }
//
//            PriceRecipeEvent newEvent = calculateRecipePrice(ingredientStack, fullCheckList);
//            //Record all the ingredientStacks that come from the newEvent to the currEvent
//            currEvent.recordIngredientsFromEvent(newEvent);
//            if (!newEvent.hasBadRecipes()){
////                LOGGER.error(ingredientStack.getDisplayName().getString()+ " recipes were good.");
//                badIngredient = false;
//            }
////            else LOGGER.error(ingredientStack.getDisplayName().getString()+ " recipes were bad.");
//
//            if (newEvent.getSum() != 0) {
//                priceResult priceResult = new priceResult(newEvent.getSum(),
//                        newEvent.getPriceCounter().get(),badIngredient);
//                cachedItems.put(ingredientStack, priceResult);
//                priceInfoList.add(new PriceList.PriceInfo(
//                        "ingredient",
//                        priceResult.sum * ingredientStack.getCount(),
//                        priceResult.counter()));
//                workingItemCount++;
//            }
//        }
//
//        if (priceInfoList.isEmpty()) return new priceResult(0, 0, badIngredient);
//
//        priceInfoList = PriceList.sortAndRemoveOutliers(priceInfoList);
//        double sum = 0;
//        double counter = 0;
//        for (PriceList.PriceInfo priceInfo : priceInfoList){
//            sum += priceInfo.price();
//            counter += priceInfo.counter();
//        }
//
//        return new priceResult(sum / workingItemCount, counter / workingItemCount, badIngredient);
//    }
//
//    public record priceResult(double sum, double counter, boolean badIngredient) {
//    }
//}
