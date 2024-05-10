package invoker54.xpshop.event.generation.recipe;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.CategoryEntry;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.data.recipe.ItemData;
import invoker54.xpshop.data.recipe.PriceGroup;
import invoker54.xpshop.data.recipe.RecipeData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static invoker54.xpshop.event.generation.ShopGenerationEvent.assignCategory;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class CraftRecipeEvents {

    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    @SubscribeEvent
    public static void priceRecipe(PriceRecipeEvent event) {
        ItemData itemData = event.getItemData();
        LOGGER.warn("Recipe Item: " + ItemData.getItemName(itemData) + (itemData.priceList.getFullPrice(false) != 0 ? " (has stats)" : ""));

        Set<ItemData> checkList = new HashSet<>(List.of(itemData));
        PriceGroup resultGroup = PriceGroup.duplicate(itemData.myGroup, checkList, new HashSet<>());

        for (PriceGroup group : resultGroup.getGroups()){
            if (group.getResult().highPrice() == 0) continue;
            itemData.priceList.addRecipe(group.getResult());
        }
    }

    @SubscribeEvent
    public static void onBrew(PriceRecipeEvent event) {
        ItemStack currentItem = event.getCurrentItem();

//        LOGGER.info("Item class: " + currentItem.getItem().getClass());
        if (!(currentItem.getItem() instanceof PotionItem)) return;
        LOGGER.info("Potion I am pricing: " + currentItem.getDisplayName().getString());
        assignCategory(event.getCurrentItem(), CategoryEntry.POTIONS);

        //This will price the potion type (regular, splashing, and lingering)
        List<Ingredient> ingredientList = new ArrayList<>(List.of(Ingredient.of(new ItemStack(Items.GLASS_BOTTLE))));
        if (!currentItem.getItem().getClass().equals(PotionItem.class)) {
            Item potionTypeItem = currentItem.getItem();
            while (!(potionTypeItem.getClass().equals(PotionItem.class))) {
                boolean foundMatch = false;

                for (PotionBrewing.Mix<Item> mix : PotionBrewing.CONTAINER_MIXES) {
                    if (potionTypeItem.equals(mix.to.get())) {
                        foundMatch = true;
                        ingredientList.add(mix.ingredient);
                        potionTypeItem = mix.from.get();
                    }
                }

                if (!foundMatch) {
                    LOGGER.error("Couldn't find a matching potion container: " + potionTypeItem.getClass());
                    break;
                }
            }
        }

        //This prices the actual potion
        Potion potion = PotionUtils.getPotion(event.getCurrentItem());
        while (potion != Potions.WATER) {
            boolean foundMatch = false;
            for (PotionBrewing.Mix<Potion> mix : PotionBrewing.POTION_MIXES) {
                if (potion.equals(mix.to.get())) {
                    foundMatch = true;
                    ingredientList.add(mix.ingredient);
                    potion = mix.from.get();
                }
            }
            if (!foundMatch) break;
        }

        AtomicBoolean hasIngredients = new AtomicBoolean(false);
        Set<PriceGroup> groupList = RecipeData.createFakeGroup(ingredientList);
        PriceGroup.GroupOperation op = ((list) ->{
            PriceList.PriceInfo priceInfo = new PriceList.PriceInfo("Potion Value", 0,0);
            for (PriceList.PriceInfo info : list){
                if (priceInfo.highPrice() != 0){
                    hasIngredients.set(true);
                    priceInfo = priceInfo.add(info);
                }
                else priceInfo = priceInfo.calculate(x -> x + XPShopConfig.xpPerOddity);
            }
            return priceInfo.calculate(x -> x/3);
        });
        PriceList.PriceInfo finalResult = new PriceGroup(null, groupList, op).getResult();

        //If the potion isn't craftable, skip it.
        if (!hasIngredients.get() || finalResult.highPrice() == 0) return;

        event.getPriceList().addRecipe(finalResult);
    }

//    public static priceResult getIngredientCost(Ingredient ingredient, Map<ItemStack, priceResult> cachedItems,
//                                                PriceRecipeCopyEvent currEvent) {
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
//            PriceRecipeCopyEvent newEvent = calculateRecipePrice(ingredientStack, fullCheckList);
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
}
