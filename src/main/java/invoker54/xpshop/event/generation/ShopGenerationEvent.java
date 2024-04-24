//package invoker54.xpshop.event.generation;
//
//import com.google.common.util.concurrent.AtomicDouble;
//import com.mojang.authlib.GameProfile;
//import invoker54.xpshop.XPShop;
//import invoker54.xpshop.capability.WorldShopCapability;
//import invoker54.xpshop.config.XPShopConfig;
//import invoker54.xpshop.data.CategoryEntry;
//import invoker54.xpshop.data.ItemEntry;
//import invoker54.xpshop.data.ModLogger;
//import invoker54.xpshop.data.PriceList;
//import invoker54.xpshop.event.generation.recipe.CraftRecipeEvents;
//import invoker54.xpshop.event.generation.recipe.GatherIngredientsEvent;
//import invoker54.xpshop.event.generation.recipe.PriceRecipeEvent;
//import invoker54.xpshop.event.generation.stat.PriceEvent;
//import invoker54.xpshop.event.generation.stat.StatPriceEvents;
//import net.minecraft.core.Vec3i;
//import net.minecraft.data.BuiltinRegistries;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.damagesource.CombatRules;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraft.world.entity.item.ItemEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.EnchantedBookItem;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.alchemy.Potion;
//import net.minecraft.world.item.alchemy.PotionUtils;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.Recipe;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraft.world.item.enchantment.EnchantmentInstance;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
//import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
//import net.minecraft.world.level.storage.loot.LootContext;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.common.Tags;
//import net.minecraftforge.common.util.FakePlayer;
//import net.minecraftforge.event.level.LevelEvent;
//import net.minecraftforge.eventbus.api.EventPriority;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.registries.ForgeRegistries;
//import org.apache.commons.lang3.tuple.Pair;
//
//import javax.annotation.Nullable;
//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.util.*;
//
//@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
//public class ShopGenerationEvent {
//    public static final DecimalFormat df = new DecimalFormat("#.#");
//    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
//
//    //ItemEntry: Shop item entry, Boolean: If it's a valid item
//    public static Map<ItemStack, PriceList> priceListMap;
//    public static Map<ItemEntry, Boolean> allItemEntries;
//    public static ArrayList<ItemStack> allItems;
//    public record InitialRecipeData(List<Pair<Recipe<?>, List<Ingredient>>> recipes, List<ItemStack> allIngredients){}
//    public static Map<ItemStack, InitialRecipeData> craftResultMap;
//    public static Map<ItemStack, ArrayList<PriceBranch>> priceBranchMap;
//    public static Map<String, Double> basicResourceMap;
//    public static Map<ItemStack, CategoryEntry> categoryMap;
//    public static ArrayList<ItemStack> newlyPricedItems;
//    //black listed stuff
//    public static ArrayList<ItemStack> blackListedItems;
//    public static ArrayList<TagKey<?>> blackListedTags;
//    public static double ingredientCheckTime = 0;
//    public static PriceList.PriceInfo info = new PriceList.PriceInfo("Null", 0,0);
//    public static PriceBranchData priceBranchData = new PriceBranchData(0,0,0,0,0,0,0);
//
//
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void initializeGenerator(LevelEvent.Load event) {
//        if (event.getLevel().isClientSide()) return;
//        if (!(event.getLevel() instanceof Level)) return;
//        if (((Level) event.getLevel()).dimension() != Level.OVERWORLD) return;
//        df.setRoundingMode(RoundingMode.HALF_UP);
//
//        long startTime = System.nanoTime();
//
//        LOGGER.error("Now beginning the auto-generation");
//        StatPriceEvents.setDummyEntity(new FakePlayer((ServerLevel) event.getLevel(), new GameProfile(null, "dummy")));
//        initLists();
//        grabRecipes((Level) event.getLevel());
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to grab recipes");
//        startTime = System.nanoTime();
//        grabEnchants();
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to grab enchants");
//        startTime = System.nanoTime();
//        grabPotions();
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to grab potions");
//        startTime = System.nanoTime();
//        grabOresAndDrops((ServerLevel) event.getLevel());
//        grabMobDrops((ServerLevel) event.getLevel());
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to grab mob and ore drops");
//        startTime = System.nanoTime();
//        grabResources();
//        initBlacklist();
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to grab resource and black list");
//        startTime = System.nanoTime();
//
//        //First price items by stats
//        ArrayList<ItemStack> statRecipeItems = new ArrayList<>();
//        ArrayList<ItemStack> unpricedItems = new ArrayList<>();
//        int totalRecipeItems = 0;
//        for (ItemStack shopStack : allItems) {
//            ItemStack recipeStack = getMatchingItemStack(shopStack, craftResultMap.keySet());
//            double statPrice = calculateStatPrice(shopStack);
////            LOGGER.warn("Stat Item: " + shopStack.getDisplayName().getString());
//            if (recipeStack != null || statPrice == 0) {
//                if (statPrice != 0) statRecipeItems.add(shopStack);
//                else unpricedItems.add(shopStack);
//                totalRecipeItems++;
////                LOGGER.error(shopStack.getDisplayName().getString() + " has no stats! ");
//            }
////            LOGGER.error("Stat progress: " + df.format(100 * (count / (double) (allItems.size()))) + "%");
//        }
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to do all of the stat items");
//        startTime = System.nanoTime();
//
//        //Then price items by recipe starting with stat items
//        int count = 0;
//        int batchCount = 1;
//        for (ItemStack shopStack : statRecipeItems) {
//            LOGGER.warn("Recipe Item: " + shopStack.getDisplayName().getString() + (getPriceList(shopStack).getFullPrice(false) != 0 ? " (has stats)" : ""));
//            calculateRecipePrice(shopStack, new ArrayList<>());
//            if (!isValidEntry(shopStack) && isSkipped(shopStack) == -1){
//                LOGGER.warn(shopStack.getDisplayName().getString() + " can't be priced.");
////                unpricedItems.add(shopStack);
//                CraftRecipeEvents.clearRecipePrices(shopStack);
//            }
//            count++;
//            LOGGER.error("Recipe progress: " + df.format(100 * (count / (double) totalRecipeItems)) + "%");
//        }
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to do all of the stat recipe items");
//        startTime = System.nanoTime();
//
////        ArrayList<ItemStack> soonToBePricedItems = new ArrayList<>();
////        ArrayList<ItemStack> fullyPricedItems = new ArrayList<>();
////
////        //Fill the fullyPricedItems list with items that are valid entries or have been skipped.
////        for (ItemEntry entry : allItemEntries.keySet()) {if (allItemEntries.get(entry))fullyPricedItems.add(entry.getShopItem());}
////        fullyPricedItems.addAll(WorldShopCapability.skippedStackMap.keySet());
//
//
////        boolean foundBatch;
////        do {
////            foundBatch = false;
////            //Grab the next batch of items that should be priced by going through the otherRecipe
////            for (ItemStack unpricedStack : new ArrayList<>(unpricedItems)){
//////                ModPriceList priceList = getPriceList(unpricedStack);
////
//////                if (priceList.getRecipePrice() != 0){
//////                    soonToBePricedItems.add(unpricedStack);
//////                    unpricedItems.remove(unpricedStack);
//////                    continue;
//////                }
////                for (ItemStack pricedStack : new ArrayList<>(fullyPricedItems)){
////                    ItemStack ingredientStack = getMatchingItemStack(pricedStack, ingredientItemMap.keySet());
////                    if (ingredientStack == null){
////                        fullyPricedItems.remove(pricedStack);
////                        continue;
////                    }
////
////                    if (getMatchingItemStack(unpricedStack, ingredientItemMap.get(ingredientStack)) != null){
////                        LOGGER.warn(unpricedStack.getDisplayName().getString() +  " has " + ingredientStack.getDisplayName().getString() + " as an ingredient");
////                        soonToBePricedItems.add(unpricedStack);
////                        unpricedItems.remove(unpricedStack);
////                        foundBatch = true;
////                        break;
////                    }
////                }
////            }
////
////            LOGGER.warn("Batch " + batchCount);
////
////            //Then I begin to calculate their recipe
////            for (ItemStack shopStack : new ArrayList<>(soonToBePricedItems)) {
////                LOGGER.warn("Recipe Item: " + shopStack.getDisplayName().getString());
////                calculateRecipePrice(shopStack, new ArrayList<>());
////                if (!isValidEntry(shopStack) && isSkipped(shopStack) == -1){
////                    LOGGER.warn(shopStack.getDisplayName().getString() + " can't be priced.");
//////                    unpricedItems.add(shopStack);
////                    CraftRecipeEvents.clearRecipePrices(shopStack);
////                }
////                count++;
////                LOGGER.error("Recipe progress: " + df.format(100 * (count / (double) totalRecipeItems)) + "%");
////            }
////
////            //after calculating all the item costs I need to get all the items I already checked
////            batchCount++;
////
////            //Finally I grab those items that I priced
////            fullyPricedItems = new ArrayList<>(newlyPricedItems);
////            LOGGER.warn("How many new items have been priced? " + newlyPricedItems.size());
////            soonToBePricedItems.clear();
////            newlyPricedItems.clear();
////        }
////        while (foundBatch);
//
//        //This will finish off the rest of the items that couldn't find a price.
//        LOGGER.warn("Last batch of items");
//        if (!unpricedItems.isEmpty()){
////            lastWave = true;
//            for (ItemStack shopStack : unpricedItems) {
//                LOGGER.warn("Recipe Item: " + shopStack.getDisplayName().getString());
//                calculateRecipePrice(shopStack, new ArrayList<>());
//                count++;
//                LOGGER.error("Recipe progress: " + df.format(100 * (count / (double) totalRecipeItems)) + "%");
//            }
////            lastWave = false;
//        }
//
//        LOGGER.error("It took " + (ingredientCheckTime/1000000000F) + " seconds to check for already priced items");
//        ingredientCheckTime = 0;
//        LOGGER.error("It took " + ((System.nanoTime() - startTime)/1000000000F) + " seconds to do the rest of the recipe items");
//        LOGGER.error("It took " + info.name() + " " + (info.counter()/1000000000F) + " to get fully priced: " + info.price());
//
//        LOGGER.error("Branch data");
//        LOGGER.warn("Highest Valid Count: " + priceBranchData.highestValidCount());
//        LOGGER.warn("Average Valid Count: " + priceBranchData.getAverageValidCount());
//        LOGGER.warn("Highest Bad Count: " + priceBranchData.highestBadCount());
//        LOGGER.warn("Average Bad Count: " + priceBranchData.getAverageBadCount());
//        LOGGER.warn("Highest branch Count: " + priceBranchData.highestBranch());
//        LOGGER.warn("Average branch Count: " + priceBranchData.getAverageBranchCount());
//        LOGGER.warn("Total items: " + priceBranchData.totalItemCount());
//        LOGGER.warn("Total skipped items: " + WorldShopCapability.skippedStackMap.size());
//        LOGGER.warn("Total entry items: " + allItemEntries.size());
//        priceBranchData = new PriceBranchData(0,0,0,0,0,0,0);
//
//        LOGGER.debug("What are the categories?");
//        for (CategoryEntry categoryEntry : WorldShopCapability.categoryEntryMap.values()) {
//            LOGGER.warn("ID: " + categoryEntry.getCategoryId() + ", Name: " + categoryEntry.getCategoryName() + ", Item: " + categoryEntry.getDisplayItem().getDisplayName().getString());
//        }
//        LOGGER.debug("What are the items? ");
//        for (ItemEntry itemEntry : WorldShopCapability.itemEntryMap.values()) {
//            LOGGER.warn("ID: " + itemEntry.getItemId() + ", Price: " + itemEntry.getPrice(true) + ", Name: " + itemEntry.getShopItem().getDisplayName().getString());
//        }
//        LOGGER.debug("What items didn't get an entry?");
//        for (ItemStack itemStack : WorldShopCapability.skippedStackMap.keySet()) {
//            LOGGER.debug(itemStack.getDisplayName().getString());
//        }
//        LOGGER.error("Blacklisted stuff");
//        for (ItemStack itemStack : blackListedItems) {
//            LOGGER.warn("Blacklisted item: " + itemStack.getDisplayName().getString());
//        }
//
//        ModLogger.getAllTimePassed();
//
//
////        clearData();
//    }
//
//    public static ItemEntry createItemEntry(ItemStack shopItem, PriceList priceList) {
//        boolean isCraftable = getMatchingStackCount(shopItem, craftResultMap.keySet()) >= 1 || priceList.hasRecipes();
//        boolean isModdedItem = !containsStringInList(ForgeRegistries.ITEMS.getKey(shopItem.getItem()), List.of("minecraft"));
//        ItemEntry matchingEntry = getItemEntry(shopItem);
//        if (matchingEntry != null) return matchingEntry;
//
//        int itemID;
//        if (WorldShopCapability.itemEntryMap.isEmpty()) itemID = 0;
////        else if (matchingEntry != null) itemID = matchingEntry.getItemId();
//        else itemID = Collections.max(WorldShopCapability.itemEntryMap.keySet()) + 1;
//
//        ItemStack lockItem = ItemStack.EMPTY;
//        if (!shopItem.isEdible() && (XPShopConfig.lockModItems && isModdedItem || XPShopConfig.lockUncraftableItems && !isCraftable)) {
//            lockItem = shopItem;
//        }
//        boolean hideIfLocked = XPShopConfig.hideLockedItems;
//        int stockAmount = 1;
//
//        return new ItemEntry(itemID, getCurrentCategoryID(shopItem), shopItem, lockItem, hideIfLocked, stockAmount, priceList);
//    }
//
//    public static double calculateStatPrice(ItemStack shopItem) {
//        //If their is already a matching ItemEntry, that means the item already has a price
//        //If the item is in the skipped list, that means the item has no redeeming qualities
//        //Go through the possible stats then calculate the price
//        //If a price is established make sure
//        LOGGER.warn("Stat Item: " + shopItem.getDisplayName().getString() + ", Valid: " + (getMatchingItemStack(shopItem, craftResultMap.keySet()) == null));
//        ItemEntry matchingEntry = getItemEntry(shopItem);
//        if (matchingEntry != null) return matchingEntry.getPrice(false);
//        double skippedPrice = isSkipped(shopItem);
//        if (skippedPrice != -1) {
//            LOGGER.warn("Skipped: " + shopItem.getDisplayName().getString());
//            return skippedPrice;
//        }
//
//        PriceList priceList = getPriceList(shopItem);
//
//        PriceEvent priceEvent = new PriceEvent(shopItem, priceList);
//        MinecraftForge.EVENT_BUS.post(priceEvent);
//
//        if (!priceList.isEmpty()) {
//            boolean valid = priceList.hasStats() && getMatchingItemStack(shopItem, craftResultMap.keySet()) == null;
//            ItemEntry entry = createItemEntry(shopItem, priceList);
//            addItemEntry(entry, valid);
//            if (valid) addPriceBranch(shopItem);
//            return entry.getPrice(false);
//        } else return 0F;
//    }
//
//    public static PriceRecipeEvent calculateRecipePrice(ItemStack shopItem, List<ItemStack> itemsBeingChecked) {
//        PriceRecipeEvent recipeEvent = new PriceRecipeEvent(shopItem, new AtomicDouble(0), itemsBeingChecked);
//        PriceList priceList = recipeEvent.getPriceList();
//
//        double timer = System.nanoTime();
//        PriceBranch matchingBranch = getPriceBranch(shopItem, recipeEvent);
//        ingredientCheckTime += (System.nanoTime() - timer);
//        if (matchingBranch != null){
//            CraftRecipeEvents.priceResult priceResult = matchingBranch.priceInfo;
//
//            ItemStack baseStack = itemsBeingChecked.isEmpty() ? shopItem : itemsBeingChecked.get(0);
//            LOGGER.warn(baseStack.getDisplayName().getString() + " You can skip this item: " + shopItem +
//                    " (Price:" + priceResult.sum() + ",Counter:"+priceResult.counter()+")");
//
//            recipeEvent.recordIngredientsFromList(matchingBranch.invalidIngredients);
//            recipeEvent.addToSum(priceResult.sum());
//            recipeEvent.getPriceCounter().addAndGet(priceResult.counter());
//            if (priceResult.badIngredient()) recipeEvent.setHasBadRecipes();
//            return recipeEvent;
//        }
//        else if (!isValidEntry(shopItem) && isSkipped(shopItem) == -1) priceList.clearRecipes();
//
//        timer = System.nanoTime();
//        MinecraftForge.EVENT_BUS.post(recipeEvent);
//        if (recipeEvent.getRecipeData() == null && priceList.getFullPrice(false) == 0){
//            LOGGER.error(shopItem.getDisplayName().getString() + " has bad recipes");
//            recipeEvent.setHasBadRecipes();
//        }
//        addPriceBranch(recipeEvent);
//        if (System.nanoTime() - timer > info.counter()) {
//            info = new PriceList.PriceInfo(shopItem.getDisplayName().getString(),
//                            recipeEvent.getSum() / recipeEvent.getPriceCounter().get(),
//                    System.nanoTime() - timer);
//            LOGGER.error(shopItem.getDisplayName().getString() + " took longer: " + ((System.nanoTime() - timer)/1000000000F));
//        }
//
//        if (priceList.hasRecipes()) assignCategory(shopItem, CategoryEntry.CRAFTABLES);
//
//        boolean skipItem = recipeEvent.wasFullyPriced() && (priceList.getFullPrice(false) == 0 ||
//                getMatchingItemStack(shopItem, allItems) == null);
//
//        if (skipItem) {
//            LOGGER.warn("Skip this item: " + shopItem.getDisplayName().getString() + ", Price: " + priceList.getFullPrice(false));
////            if (priceList.isEmpty()) priceList.addRecipe("Oddity", XPShopConfig.xpPerOddity, 1);
//            skipItem(shopItem, priceList.getFullPrice(false));
//            newlyPricedItems.add(shopItem);
//            return recipeEvent;
//        }
//
//
//
//        //Only make it a valid entry if the item is fully priced.
//        if (recipeEvent.wasFullyPriced()) {
//            if (isValidEntry(shopItem) || isSkipped(shopItem) != -1) LOGGER.error("THIS WAS ALREADY PRICED!! " + shopItem.getDisplayName().getString());
//            else LOGGER.warn("Item being fully priced: " + shopItem.getDisplayName().getString());
//
//            ItemEntry entry = createItemEntry(shopItem, priceList);
//            //Only add the item if its recipe price is higher than 0, or if it's the base item being checked
//            addItemEntry(entry, !recipeEvent.hasBadRecipes());
//            newlyPricedItems.add(shopItem);
//        }
//
//        return recipeEvent;
//    }
//
//    public static void initBlacklist() {
//        ArrayList<TagKey<?>> allTagKeys = new ArrayList<>();
//        blackListedTags.clear();
//
//        //Let's make a list of all tags
//        for (ItemStack itemStack : allItems) {
//            itemStack.getTags().toList().forEach((tagKey) -> {
//                if (!allTagKeys.contains(tagKey)) allTagKeys.add(tagKey);
//            });
//            Block.byItem(itemStack.getItem()).defaultBlockState().getTags().toList().forEach((tagKey) -> {
//                if (!allTagKeys.contains(tagKey)) allTagKeys.add(tagKey);
//            });
//        }
//        //Then make a list of blackListed Tags
//        for (TagKey<?> tagKey : allTagKeys) {
//            if (containsStringInList(tagKey.location(), XPShopConfig.tagBlacklist)) {
//                blackListedTags.add(tagKey);
//            }
//        }
//
//        //Now to go through each item and see which ones are blacklisted
//        for (ItemStack itemStack : new ArrayList<>(allItems)) {
//            Item item = itemStack.getItem();
//
//            //Check if the mod is banned
//            if (containsStringInList(ForgeRegistries.ITEMS.getKey(item), XPShopConfig.modBlacklist)) {
//                allItems.remove(itemStack);
//                blackListedItems.add(itemStack);
//                continue;
//            }
//
//            //Now check if the item is banned
//            if (containsStringInList(ForgeRegistries.ITEMS.getKey(item), XPShopConfig.itemBlacklist)) {
//                allItems.remove(itemStack);
//                blackListedItems.add(itemStack);
//                continue;
//            }
//
//            //Check if the items tags are banned
//            if (containsTagKeyInList(itemStack, blackListedTags)) {
//                allItems.remove(itemStack);
//                blackListedItems.add(itemStack);
//                continue;
//            }
//
//            if (getItemEntry(itemStack) != null) {
//                allItems.remove(itemStack);
//                continue;
//            }
//
//            //Check if the item was previously skipped
//            if (isSkipped(itemStack) != -1) {
//                allItems.remove(itemStack);
//                blackListedItems.add(itemStack);
//                continue;
//            }
//        }
//    }
//
//    public static void grabRecipes(Level level) {
//        Collection<Recipe<?>> recipes = level.getRecipeManager().getRecipes();
//        LOGGER.warn("How many recipes are there? " + recipes.size());
//
//        double timer = System.nanoTime();
//        for (Recipe<?> recipe : recipes) {
//            ItemStack resultItem = recipe.getResultItem();
////            LOGGER.debug("Craft result item: " + resultItem.getDisplayName().getString());
//            ItemStack mapStack = getMatchingItemStack(resultItem, craftResultMap.keySet());
//            InitialRecipeData initialRecipeData;
//
//            if (mapStack == null) {
//                initialRecipeData = new InitialRecipeData(new ArrayList<>(), new ArrayList<>());
//                craftResultMap.put(resultItem, initialRecipeData);
//            }
//            else initialRecipeData = craftResultMap.get(mapStack);
//
//            GatherIngredientsEvent ingredientsEvent = new GatherIngredientsEvent(recipe);
//            MinecraftForge.EVENT_BUS.post(ingredientsEvent);
//
//            //This will skip bad recipes
//            if (ingredientsEvent.isCanceled()) {
//                LOGGER.warn(resultItem.getDisplayName().getString() + "Ingredient issue: Invalid recipe, skip...");
//                continue;
//            }
//            //This will skip empty recipes
//            else if (ingredientsEvent.getIngredients().isEmpty()) {
//                LOGGER.warn(resultItem.getDisplayName().getString() + "Ingredient issue: Couldn't find any ingredients, skip...");
//                continue;
//            }
//
//            //Add the recipe
//            List<Ingredient> ingredients = new ArrayList<>();
//            List<ItemStack> allIngredients = initialRecipeData.allIngredients;
//
//            //Grab the ingredient items from the recipe.
//            for (Ingredient ingredient : ingredientsEvent.getIngredients()) {
//                if (ingredient == null) continue;
//                if (ingredient.isEmpty()) continue;
//                ingredients.add(ingredient);
//
//                for (ItemStack itemStack : ingredient.getItems()) {
//                    if (getMatchingItemStack(itemStack, allIngredients) == null) {
//                        allIngredients.add(itemStack);
//                    }
//                }
//            }
//
//            //Add the recipe and its ingredients to recipe data
//            initialRecipeData.recipes.add(Pair.of(recipe, ingredients));
//        }
//        LOGGER.error("It took " + ((System.nanoTime() - timer)/1000000000F) + " seconds to go through all the recipes.");
//        timer = System.nanoTime();
//
//        for (InitialRecipeData initialRecipeData : craftResultMap.values()){
//            //Sort the items in the allIngredient list
//            initialRecipeData.allIngredients.sort(Comparator.comparing(stack -> stack.getDisplayName().getString()));
//        }
//        LOGGER.error("It took " + ((System.nanoTime() - timer)/1000000000F) + " seconds to sort every ingredient list.");
//    }
//
//    public static void grabEnchants() {
//        //I have to grab all the enchantment books
//        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
//            if (containsStringInList(ForgeRegistries.ENCHANTMENTS.getKey(enchantment), XPShopConfig.modBlacklist)) {
//                continue;
//            }
//            for (int a = enchantment.getMinLevel(); a < enchantment.getMaxLevel() + 1; a++) {
//                allItems.add(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, a)));
//            }
//        }
//    }
//
//    public static void grabPotions() {
//        //I have to grab all the potions
//        for (Potion potion : ForgeRegistries.POTIONS) {
//            if (containsStringInList(ForgeRegistries.POTIONS.getKey(potion), XPShopConfig.modBlacklist)) {
//                continue;
//            }
//
//            allItems.add(PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
//            allItems.add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), potion));
//            allItems.add(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
//        }
//
//    }
//
//    public static void grabOresAndDrops(ServerLevel level) {
//        Map<Block, Double> oreMap = new HashMap<>();
//
//        for (Map.Entry<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> mapEntry : BuiltinRegistries.CONFIGURED_FEATURE.entrySet()) {
//            ConfiguredFeature<?, ?> feature = mapEntry.getValue();
//            if (!(feature.config() instanceof OreConfiguration oreConfig)) continue;
//
//            for (OreConfiguration.TargetBlockState target : oreConfig.targetStates) {
//                Block block = target.state.getBlock();
//                double currSize = oreMap.get(block) == null ? 0 : oreMap.get(block);
//
//                oreMap.put(block, currSize + Math.max(1, (oreConfig.size * (1F - oreConfig.discardChanceOnAirExposure))));
//                ItemStack oreStack = new ItemStack(block.asItem());
//                assignCategory(oreStack, CategoryEntry.ORE_DROPS);
//            }
//        }
//        List<Block> blocks = new ArrayList<>(oreMap.keySet());
//        blocks.sort((a, b) -> {
//            double sizeA = oreMap.get(a);
//            double sizeB = oreMap.get(b);
//            return Double.compare(sizeA, sizeB);
//        });
//        Collections.reverse(blocks);
//
//        //region Grab all ores, sort them, and give them the proper price
//        LOGGER.info("Pricing the Ores");
//        double count = 1;
//        double prevSize = 0;
//        for (Block block : blocks) {
//            if (oreMap.get(block) < prevSize) count += 1;
//            prevSize = oreMap.get(block);
////            oreMap.put(block, (double) (count * XPShopConfig.xpOreStep));
//            getPriceList(new ItemStack(block.asItem())).addStat("Ore drop", (double) (count * XPShopConfig.xpOreStep));
////            LOGGER.warn("Ore: " + block.getName().getString() + ", Size: " + prevSize + ", Price: " + oreMap.get(block));
//        }
//        //endregion
//
//        //region Grab all the ore drops, then price them.
//        LOGGER.info("Pricing the Drops");
//        for (Block block : oreMap.keySet()) {
//            BlockState defaultState = block.defaultBlockState();
//            String priceTypeString = block.getName().getString() + " Drop";
//            double totalPrice = getPriceList(new ItemStack(block.asItem())).getFullPrice(false);
//            //Make a loot context builder(take from loot command)
//            LootContext lootContext = (new LootContext.Builder(level))
//                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(Vec3i.ZERO))
//                    .withParameter(LootContextParams.BLOCK_STATE, defaultState)
//                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
//                    .create(LootContextParamSets.BLOCK);
//            LootTable loottable = level.getServer().getLootTables().get(block.getLootTable());
//            calculateDropCosts(loottable, lootContext, 100, CategoryEntry.ORE_DROPS, priceTypeString, totalPrice);
//        }
//        //endregion
//    }
//
//    public static void grabMobDrops(ServerLevel level) {
//        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
//            var entity = entityType.create(level);
//            if (!(entity instanceof LivingEntity livingEntity)) continue;
//            String priceTypeString = livingEntity.getName().getString() + " Mob Drop:";
//
//            LOGGER.debug("Mob: " + livingEntity.getName().getString());
//            double totalHealth = livingEntity.getMaxHealth();
//            if (totalHealth <= 0) continue;
//            double damage = CombatRules.getDamageAfterAbsorb((float) totalHealth, (float) livingEntity.getArmorValue(), (float) livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
//            //If the mob has armor, this will give the true amount of health the mob should have.
//            totalHealth += (totalHealth - damage);
//            LOGGER.debug("Mob health: " + totalHealth);
//            double totalPrice = (double) (totalHealth * XPShopConfig.xpPerMobHealth);
//            if (livingEntity.getType().is(Tags.EntityTypes.BOSSES)) {
//                LOGGER.debug("Was a boss, double the price");
//                totalPrice *= 2;
//            }
//            LOGGER.debug("Mob price: " + totalPrice);
//
//            ResourceLocation mobLocation = livingEntity.getLootTable();
//            LootTable lootTable = level.getServer().getLootTables().get(mobLocation);
//            Player player = (Player) StatPriceEvents.getDummyEntity();
//            DamageSource dmgSource = DamageSource.playerAttack(player);
//
//            LootContext.Builder lootcontext$builder = (new LootContext.Builder(level)).withRandom(livingEntity.getRandom())
//                    .withParameter(LootContextParams.THIS_ENTITY, entity)
//                    .withParameter(LootContextParams.ORIGIN, livingEntity.position())
//                    .withParameter(LootContextParams.DAMAGE_SOURCE, dmgSource)
//                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, dmgSource.getEntity())
//                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, dmgSource.getDirectEntity());
//            lootcontext$builder = lootcontext$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(0);
//            LootContext ctx = lootcontext$builder.create(LootContextParamSets.ENTITY);
//
//            calculateDropCosts(lootTable, ctx, 100, CategoryEntry.MOB_DROPS, priceTypeString, totalPrice);
//
//            entity.captureDrops(new ArrayList<>());
//            ((LivingEntity) entity).dropCustomDeathLoot(dmgSource, 0, true);
//            Collection<ItemEntity> customDrops = entity.captureDrops(new ArrayList<>());
//            if (customDrops.isEmpty()) continue;
//            LOGGER.debug(entity.getName().getString() + " has " + customDrops.size() + " custom drop(s)");
//
//            double customLootPrice = totalPrice / (double) customDrops.size();
//            for (ItemEntity itemEntity : customDrops) {
//                ItemStack customStack = itemEntity.getItem();
//                if (isSkipped(customStack) != -1 || getItemEntry(customStack) != null) continue;
//                assignCategory(customStack, CategoryEntry.MOB_DROPS);
//                double individualPrice = customLootPrice / customStack.getCount();
//                getPriceList(customStack).addStat(priceTypeString, individualPrice);
//
////                LOGGER.debug(customStack.getDisplayName().getString() + " will cost: " + dropsMap.get(getMatchingItemStack(customStack, dropsMap.keySet())));
//                itemEntity.setItem(ItemStack.EMPTY);
//            }
//        }
//    }
//
//    public static void grabResources() {
//        LOGGER.info("Grabbing Basic Resource tags");
//        for (String tag : XPShopConfig.basicResourceList) {
//            try {
//                List<String> parts = List.of(tag.split(":"));
//                if (parts.size() != 2 && parts.size() != 3) {
//                    LOGGER.error("THIS RESOURCE STRING WAS MADE INCORRECTLY: " + tag);
//                    continue;
//                }
//
//                String tagToLookFor;
//                double price;
//                if (parts.size() == 2) {
//                    tagToLookFor = parts.get(0);
//                    price = Double.parseDouble(parts.get(1));
//                } else {
//                    tagToLookFor = parts.get(0) + ":" + parts.get(1);
//                    price = Double.parseDouble(parts.get(2));
//                }
//
//                basicResourceMap.put(tagToLookFor, price);
//                LOGGER.warn("Resource: " + tagToLookFor + ", Price: " + price);
//            } catch (Error error) {
//                error.printStackTrace();
//            }
//        }
//    }
//
//    public static PriceList getPriceList(ItemStack priceStack) {
//        ItemStack matchingStack = getMatchingItemStack(priceStack, priceListMap.keySet());
//        if (matchingStack == null) {
//            priceListMap.put(priceStack, new PriceList(priceStack));
//            matchingStack = priceStack;
//        }
//        return priceListMap.get(matchingStack);
//    }
//
//    public static ItemEntry getItemEntry(ItemStack stackToFind) {
//        Collection<ItemEntry> entries = allItemEntries.keySet();
//
//        for (ItemEntry itemEntry : entries) {
//            boolean sameItem = stackToFind.sameItem(itemEntry.getShopItem());
//            boolean matchingTags = ItemStack.tagMatches(stackToFind, itemEntry.getShopItem());
//            if (sameItem && matchingTags) return itemEntry;
//        }
//
//        return null;
//    }
//
//    public static void addItemEntry(ItemEntry entry, boolean valid) {
//        ItemEntry matchingEntry = null;
//        for (ItemEntry itemEntry : allItemEntries.keySet()) {
//            boolean sameItem = entry.getShopItem().sameItem(itemEntry.getShopItem());
//            boolean matchingTags = ItemStack.tagMatches(entry.getShopItem(), itemEntry.getShopItem());
//            if (sameItem && matchingTags) {
////                itemEntry.deserializeNBT(entry.serializeNBT());
////                LOGGER.error("THERE WAS ALREADY A SHOP ENTRY FOR THIS ITEM: " + entry.getShopItem().getDisplayName().getString());
//                matchingEntry = itemEntry;
//                break;
////                return;
//            }
//        }
//        if (matchingEntry != null) allItemEntries.remove(matchingEntry);
////        boolean hasRecipe = getMatchingStackCount(entry.getShopItem(), craftResultMap.keySet()) >= 1;
////        boolean isIngredient = getMatchingItemStack(entry.getShopItem(), craftIngredients.keySet()) != null;
////        boolean isFinished = (hasRecipe || isIngredient || valid);
//
//        allItemEntries.put(entry, valid);
//        if (valid) {
//            PriceList priceList = getPriceList(entry.getShopItem());
//            PriceList.PriceInfo uneditedInfo = priceList.getUneditedPriceInfo();
//
//            WorldShopCapability.itemEntryMap.put(entry.getItemId(), entry);
//            LOGGER.warn(entry.getShopItem().getDisplayName().getString() + " is fully priced! " +
//                    "(Full Price:"+priceList.getFullPrice(false) +
//                    ", Price:"+uneditedInfo.price() +", Counter:"+uneditedInfo.counter());
//        }
//    }
//
//    public static ItemStack getMatchingItemStack(ItemStack stackToFind, Collection<ItemStack> stackList) {
//        for (ItemStack stack : stackList) {
//            boolean sameItem = stackToFind.sameItem(stack);
//            boolean matchingTags = ItemStack.tagMatches(stackToFind, stack);
//            if (sameItem && matchingTags) return stack;
//        }
//        return null;
//    }
//
//    public static int getMatchingStackCount(ItemStack stackToFind, Collection<ItemStack> stackList) {
//        if (stackToFind == null) return 0;
//        int count = 0;
//
//        for (ItemStack stack : stackList) {
//            boolean sameItem = stackToFind.sameItem(stack);
//            boolean matchingTags = ItemStack.tagMatches(stackToFind, stack);
//            if (sameItem && matchingTags) count += 1;
//        }
//        return count;
//    }
//
//    public static boolean containsStringInList(@Nullable ResourceLocation location, Collection<? extends String> stringList) {
//        if (location == null) return false;
//        String locationString = location.toString().toLowerCase(Locale.ROOT);
//
//        for (String string : stringList) {
//            if (locationString.contains(string)) return true;
//        }
//
//        return false;
//    }
//
//    public static boolean containsTagKeyInList(ItemStack itemStack, List<TagKey<?>> tagList) {
//        for (TagKey<?> itemTag : itemStack.getTags().toList()) {
//            if (tagList.contains(itemTag)) return true;
//        }
//        for (TagKey<?> itemTag : Block.byItem(itemStack.getItem()).defaultBlockState().getTags().toList()) {
//            if (tagList.contains(itemTag)) return true;
//        }
//
//        return false;
//    }
//
//    public static void assignCategory(ItemStack itemStack, CategoryEntry categoryEntry) {
//        ItemStack matchingStack = getMatchingItemStack(itemStack, categoryMap.keySet());
//        if (matchingStack == null) {
//            categoryMap.put(itemStack, categoryEntry);
//        } else if (categoryMap.get(matchingStack).getPriority() < categoryEntry.getPriority()) {
//            categoryMap.put(matchingStack, categoryEntry);
//        }
//    }
//
//    public static int getCurrentCategoryID(ItemStack itemStack) {
//        ItemStack matchingStack = getMatchingItemStack(itemStack, categoryMap.keySet());
//        return categoryMap.getOrDefault(matchingStack, CategoryEntry.ODDITY).getCategoryId();
//    }
//
//    public static CategoryEntry getOrCreateCategory(Integer priority, String categoryName, ItemStack displayItem) {
//        for (CategoryEntry categoryEntry : WorldShopCapability.categoryEntryMap.values()) {
//            if (categoryEntry.getCategoryName().contains(categoryName)) {
//                return categoryEntry;
//            }
//        }
//
//        int highestNumber;
//        if (WorldShopCapability.categoryEntryMap.isEmpty()) highestNumber = 0;
//        else highestNumber = Collections.max(WorldShopCapability.categoryEntryMap.keySet());
//        CategoryEntry categoryEntry = new CategoryEntry(highestNumber + 1, priority, categoryName, displayItem);
//        WorldShopCapability.categoryEntryMap.put(categoryEntry.getCategoryId(), categoryEntry);
//
//        return categoryEntry;
//    }
//
//    public static Map<ItemStack, Double> calculateDropCosts(LootTable lootTable, LootContext lootContext, int totalRolls, CategoryEntry category, String dropType, double totalPrice) {
//        Map<ItemStack, Double> countMap = new HashMap<>();
//        for (int a = 0; a < totalRolls; a++) {
//            for (ItemStack stack : lootTable.getRandomItems(lootContext)) {
//
//                if (stack.isEmpty()) continue;
//
//                ItemStack mapStack = getMatchingItemStack(stack, countMap.keySet());
//                if (mapStack == null) {
//                    countMap.put(stack, (double) stack.getCount());
//                } else {
//                    countMap.put(mapStack, countMap.get(mapStack) + stack.getCount());
//                }
//            }
//        }
//
//        if (countMap.isEmpty()) {
//            LOGGER.debug("This object doesn't drop anything...");
//            return countMap;
//        }
//        double itemCountAverage = 0F;
//        //This will make the countMap value equal the average amount of each item per roll
//        for (Map.Entry<ItemStack, Double> entry : countMap.entrySet()) {
//            entry.setValue((entry.getValue() / totalRolls));
//            itemCountAverage += entry.getValue();
//        }
//        totalPrice = totalPrice / itemCountAverage;
//        LOGGER.debug("Total price for each item: " + totalPrice);
//
//        for (Map.Entry<ItemStack, Double> entry : new ArrayList<>(countMap.entrySet())) {
//            if (entry.getValue() < 1)
//                LOGGER.debug("Drop: " + entry.getKey().getDisplayName().getString() + ", Average: " + entry.getValue() + ", Price(totalPrice * (1 + (1 - dropAverage))): " + (totalPrice * (1 + (1 - entry.getValue()))));
//            if (entry.getValue() > 1)
//                LOGGER.debug("Drop: " + entry.getKey().getDisplayName().getString() + ", Average: " + entry.getValue() + ", Price(totalPrice/dropAverage): " + (totalPrice / entry.getValue()));
//            if (entry.getValue() < 0.1) {
//                LOGGER.error("here's one below the threshold, skipping...");
//                countMap.remove(entry.getKey());
//                continue;
//            }
//            //This gets the true price of each itemStack in the countMap
//            if (entry.getValue() < 1) entry.setValue(totalPrice * (1 + (1 - entry.getValue())));
//            else entry.setValue(totalPrice / entry.getValue());
//
//        }
//
//        for (Map.Entry<ItemStack, Double> entry : countMap.entrySet()) {
////            //This will make it so dupe stats don't keep getting added to already existing item entries
////            if (getItemEntry(entry.getKey()) != null) continue;
//            if (isSkipped(entry.getKey()) != -1 || getItemEntry(entry.getKey()) != null) continue;
//            assignCategory(entry.getKey(), category);
//            getPriceList(entry.getKey()).addStat(dropType, entry.getValue());
//        }
//        return countMap;
//    }
//
//    public static void skipItem(ItemStack stack, double skipPrice) {
//        WorldShopCapability.skippedStackMap.put(stack, skipPrice);
//    }
//
//    public static double isSkipped(ItemStack stack) {
//        ItemStack skippedStack = getMatchingItemStack(stack, WorldShopCapability.skippedStackMap.keySet());
//        return WorldShopCapability.skippedStackMap.getOrDefault(skippedStack, -1D);
//    }
//
//    public static boolean isValidEntry(ItemStack stack){
////        LOGGER.error("Is an entry? " + ((getItemEntry(stack) != null)) +
////                ", Is valid? " + (allItemEntries.getOrDefault(getItemEntry(stack), false)));
//        return allItemEntries.getOrDefault(getItemEntry(stack), false);
//    }
//
//    private static void initLists() {
//        priceListMap = new HashMap<>();
//        allItemEntries = new HashMap<>();
//        for (ItemEntry entry : WorldShopCapability.itemEntryMap.values()) {
//            allItemEntries.put(entry, true);
//            priceListMap.put(entry.getShopItem(), entry.getPriceList());
//        }
//        allItems = new ArrayList<>();
//        for (Item item : ForgeRegistries.ITEMS.getValues()) {
//            if (item == Items.AIR) continue;
//            allItems.add(new ItemStack(item));
//        }
//        LOGGER.error("How many items? " + allItems.size());
//        craftResultMap = new HashMap<>();
//        priceBranchMap = new HashMap<>();
//        basicResourceMap = new HashMap<>();
//        categoryMap = new HashMap<>();
//        newlyPricedItems = new ArrayList<>();
//        blackListedItems = new ArrayList<>();
//        blackListedTags = new ArrayList<>();
//    }
//
////    public static void addPriceBranch(PriceRecipeEvent event){
////        double highestValidCount = priceBranchData.highestValidCount;
////        double totalValidCount = priceBranchData.totalValidCount;
////        double highestBadCount = priceBranchData.highestBadCount;
////        double totalBadCount = priceBranchData.totalBadCount;
////        double highestBranch = priceBranchData.highestBranch;
////        double totalBranchCount = priceBranchData.totalBranchCount;
////        double totalItemCount = priceBranchData.totalItemCount;
////
////        ItemStack matchingStack = getMatchingItemStack(event.getCurrentItem(), priceBranchMap.keySet());
////        if (matchingStack == null){
////            matchingStack = event.getCurrentItem();
////            priceBranchMap.put(matchingStack, new ArrayList<>());
////            totalItemCount++;
////        }
////
////        ItemData.PriceBranch newBranch = new ItemData.PriceBranch(event.getInvalidIngredients(), event.getValidIngredients(),
////                new CraftRecipeEvents.priceResult(event.getSum(),
////                        event.getPriceCounter().get(), event.hasBadRecipes()));
////        priceBranchMap.get(matchingStack).add(newBranch);
////
////        if (newBranch.validIngredients.size() > highestValidCount) highestValidCount = newBranch.validIngredients.size();
////        totalValidCount += newBranch.validIngredients.size();
////        if (newBranch.invalidIngredients.size() > highestBadCount) highestBadCount = newBranch.invalidIngredients.size();
////        totalBadCount += newBranch.invalidIngredients.size();
////        if (priceBranchMap.get(matchingStack).size() > highestBranch) highestBranch = priceBranchMap.get(matchingStack).size();
////        totalBranchCount += 1;
////
////        priceBranchData = new PriceBranchData(highestValidCount, totalValidCount, highestBadCount,
////                totalBadCount, highestBranch, totalBranchCount, totalItemCount);
////
////        LOGGER.error("The item we are creating the price branch for: " + event.getCurrentItem().getDisplayName().getString());
////        List<ItemStack> itemsToBeChecked = event.getItemList();
////        ArrayList<String> ingredients = new ArrayList<>();
////        itemsToBeChecked.forEach((stack1) -> ingredients.add(stack1.getDisplayName().getString()));
////        Collections.sort(ingredients);
////        LOGGER.error("check list:" + "("+itemsToBeChecked.size()+") " + ingredients);
////        LOGGER.error("Price branch " + priceBranchMap.get(matchingStack).size());
////        ingredients.clear();
////        event.getValidIngredients().forEach((stack1 -> ingredients.add(stack1.getDisplayName().getString())));
////        Collections.sort(ingredients);
////        LOGGER.error("Valid ingredients:" + "("+event.getValidIngredients().size()+") " + ingredients);
////
////        ingredients.clear();
////        event.getInvalidIngredients().forEach((stack1 -> ingredients.add(stack1.getDisplayName().getString())));
////        Collections.sort(ingredients);
////        LOGGER.error("Bad ingredients:" + "("+event.getInvalidIngredients().size()+") "  + ingredients);
////    }
////    private static void addPriceBranch(ItemStack shopItem){
////        PriceRecipeEvent event = new PriceRecipeEvent(shopItem, new AtomicDouble(0), new ArrayList<>());
////        PriceList priceList = getPriceList(shopItem);
////        PriceList.PriceInfo priceInfo = priceList.getUneditedPriceInfo();
////        event.addToSum(priceInfo.price());
////        event.getPriceCounter().addAndGet(priceInfo.counter());
////        addPriceBranch(event);
////    }
//
////    private static PriceBranch getPriceBranch(ItemStack stack, PriceRecipeEvent event){
////        ItemStack matchingStack = getMatchingItemStack(stack, priceBranchMap.keySet());
////        if (matchingStack == null) return null;
////        ArrayList<PriceBranch> priceBranches = new ArrayList<>(priceBranchMap.get(matchingStack));
////
////        boolean isValid = false;
////        List<ItemStack> eventList = event.getInvalidIngredients();
////        if (event.getInvalidIngredients().size() > event.getValidIngredients().size()){
////            isValid = true;
////            eventList = event.getValidIngredients();
////        }
////
////        for (PriceBranch branch : new ArrayList<>(priceBranches)){
////            List<ItemStack> branchList = isValid ? branch.validIngredients : branch.invalidIngredients;
////            if (branchList.size() != eventList.size()) continue;
////
////            boolean skip = false;
////            for (int a = 0; a < branchList.size(); a++){
////                if (branchList.get(a) == eventList.get(a)) continue;
////
////                skip = true;
////                break;
////            }
////            if (skip) continue;
////            return branch;
////        }
////        return null;
////    }
//
//    public record PriceBranchData(double highestValidCount, double totalValidCount,
//                                  double highestBadCount, double totalBadCount, double highestBranch, double totalBranchCount, double totalItemCount){
//        public double getAverageValidCount(){return totalValidCount/totalItemCount;}
//        public double getAverageBadCount(){return totalBadCount/totalItemCount;}
//        public double getAverageBranchCount(){return totalBranchCount/totalItemCount;}
//    }
////    private static void clearData(boolean fullClean) {
////        allItemEntries.clear();
////        allItems.clear();
////        craftResultMap.clear();
////        ingredientItemMap.clear();
////        basicResourceMap.clear();
////        categoryMap.clear();
////        blackListedItems.clear();
////        blackListedTags.clear();
////    }
//}
