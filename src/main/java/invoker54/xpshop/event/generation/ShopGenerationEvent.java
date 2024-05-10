package invoker54.xpshop.event.generation;

import com.mojang.authlib.GameProfile;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.capability.WorldShopCapability;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.CategoryEntry;
import invoker54.xpshop.data.ItemEntry;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.data.recipe.IngredientData;
import invoker54.xpshop.data.recipe.ItemData;
import invoker54.xpshop.data.recipe.RecipeData;
import invoker54.xpshop.event.generation.recipe.GatherIngredientsEvent;
import invoker54.xpshop.event.generation.recipe.PriceRecipeEvent;
import invoker54.xpshop.event.generation.stat.PriceEvent;
import invoker54.xpshop.event.generation.stat.StatPriceEvents;
import invoker54.xpshop.util.MiniTicker;
import invoker54.xpshop.util.ModStopWatch;
import invoker54.xpshop.util.ModTimer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
public class ShopGenerationEvent {
    public static final DecimalFormat df = new DecimalFormat("#.#");
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    //ItemEntry: Shop item entry, Boolean: If it's a valid item
    public static final Map<ItemStack, PriceList> priceListMap = new ConcurrentHashMap<>();
    public static final Map<ItemEntry, Boolean> allItemEntries = new ConcurrentHashMap<>();
    public static final NonNullList<ItemStack> allItems = NonNullList.create();
    public record InitialRecipeData(List<Pair<Recipe<?>, List<Ingredient>>> recipes) {
    }

    public static final Map<ItemStack, InitialRecipeData> craftResultMap = new ConcurrentHashMap<>();
    public static final Map<String, Double> basicResourceMap = new HashMap<>();
    public static final Map<ItemStack, CategoryEntry> categoryMap = new ConcurrentHashMap<>();
    //black listed stuff
    public static final List<ItemStack> blackListedItems = new ArrayList<>();
    public static final List<TagKey<?>> blackListedTags = new ArrayList<>();
    public static AtomicBoolean isRunning = new AtomicBoolean(false);
    public static PriceBranchData priceBranchData = new PriceBranchData( 0, 0, 0, 0, new ArrayList<>());
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static final Set<CompletableFuture<?>> futures = new HashSet<>();

    public static void initializeGenerator(Level level) {
        if (level.isClientSide()) return;
        df.setRoundingMode(RoundingMode.HALF_UP);
        MiniTicker<?> myTimer = ModTimer.getTimer(XPShop.MOD_ID, "generator").ticker();
        ModStopWatch branchStopWatch = ModStopWatch.getTimer(XPShop.MOD_ID, "BranchInfo", ModStopWatch.Time.ACCUMULATE);
        resetAll();

        Thread t = new Thread(() -> {
            isRunning.set(true);
            LOGGER.error("Now beginning the auto-generation");
            StatPriceEvents.setDummyEntity(new FakePlayer((ServerLevel) level, new GameProfile(null, "dummy")));
            myTimer.reset();
            initItemList();
            grabRecipes(level);
            myTimer.reset();
//            grabEnchants();
//            LOGGER.info(myTimer.record("grab enchants"));
//            grabPotions();
//            LOGGER.info(myTimer.record("grab potions"));
            grabOresAndDrops((ServerLevel) level);
            LOGGER.info(myTimer.record("grab ores"));
            grabMobDrops((ServerLevel) level);
            LOGGER.info(myTimer.record("grab mob drops"));
            grabResources();
            initBlacklist();
            LOGGER.info(myTimer.record("grab resources and blacklist"));

            //First price items by stats
            ArrayList<ItemStack> recipeItems = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);
//            allItems.subList(0, Math.min(1100, allItems.size()))
            for (ItemStack shopStack : allItems) {
                addTask(() -> {
                    ItemStack recipeStack = getMatchingItemStack(shopStack, craftResultMap.keySet());
                    double statPrice = calculateStatPrice(shopStack);
                    if (recipeStack != null || statPrice == 0) {
                        recipeItems.add(shopStack);
                    }
                    LOGGER.error("Stat progress: " + df.format(100 *
                            (counter.addAndGet(1) / (double) (allItems.size()))) + "%");
                    return null;
                });
            }
            LOGGER.info(myTimer.record("Queue all stat tasks"));
            waitForTasks();
            LOGGER.info(myTimer.record("Complete all stat tasks"));
            counter.set(0);
            //Then price items by recipe
            for (ItemStack stack : recipeItems) {
                    ItemData itemData = ItemData.getMatchingData(stack);
//                itemData.isInitialized = addTask(() -> {
                    calculateRecipePrice(stack);
                    LOGGER.error("Recipe progress: " + df.format(100 * (counter.addAndGet(1) / (double) recipeItems.size())) + "%");
//                    return true;
//                });
            }
            waitForTasks();
            LOGGER.info(myTimer.record("Complete all recipe tasks"));
            LOGGER.info(branchStopWatch.compileTime("Gathering branch data", false, true));
            LOGGER.debug(ItemData.STOP_WATCH.bestTime());

            LOGGER.error("Branch data");
            LOGGER.warn("Highest Bad Count: " + priceBranchData.highestBadCount());
            LOGGER.warn("Average Bad Count: " + priceBranchData.getAverageBadCount());
            LOGGER.warn("Highest branch Count: " + priceBranchData.highestBranch());
            LOGGER.warn("Average branch Count: " + priceBranchData.getAverageBranchCount());
            LOGGER.warn("Total items: " + priceBranchData.stackList.size());
            LOGGER.warn("Total skipped items: " + WorldShopCapability.skippedStackMap.size());
            LOGGER.warn("Total entry items: " + allItemEntries.size());
            priceBranchData = new PriceBranchData( 0, 0, 0, 0, new ArrayList<>());

            LOGGER.debug("What are the categories?");
            for (CategoryEntry categoryEntry : WorldShopCapability.categoryEntryMap.values()) {
                LOGGER.warn("ID: " + categoryEntry.getCategoryId() + ", Name: " + categoryEntry.getCategoryName() + ", Item: " + categoryEntry.getDisplayItem().getDisplayName().getString());
            }
            LOGGER.debug("What are the items? ");
            for (ItemEntry itemEntry : WorldShopCapability.itemEntryMap.values()) {
                LOGGER.warn("ID: " + itemEntry.getItemId() + ", Price: " + itemEntry.getPrice(true) + ", Name: " + itemEntry.getShopItem().getDisplayName().getString());
            }
            LOGGER.debug("What items didn't get an entry?");
            for (ItemStack itemStack : WorldShopCapability.skippedStackMap.keySet()) {
                LOGGER.debug(itemStack.getDisplayName().getString());
            }
            LOGGER.error("Blacklisted stuff");
            for (ItemStack itemStack : blackListedItems) {
                LOGGER.warn("Blacklisted item: " + itemStack.getDisplayName().getString());
            }

            ModLogger.getAllTimePassed();
            for (Player player : level.getServer().getPlayerList().getPlayers()) {
                WorldShopCapability.syncInitialCapToClient(player);
            }
            isRunning.set(false);
        });
        t.start();
        LOGGER.debug("the new thread is now running.");
    }

    public static void initItemList(){
        if (!allItems.isEmpty()) return;

        for (Item item : ForgeRegistries.ITEMS.getValues()){
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, allItems);
        }
    }

    public static void resetAll() {
        priceListMap.clear();
        allItemEntries.clear();

        WorldShopCapability.resetAll();
        ItemData.allItemDataList.clear();
        RecipeData.allRecipeData.clear();
        IngredientData.allIngredientDataList.clear();
    }

    public synchronized static ItemEntry createItemEntry(ItemStack shopItem, PriceList priceList) {
//        boolean isCraftable = getMatchingItemStack(shopItem, craftResultMap.keySet()) != null || priceList.hasRecipes();
        boolean isCraftable = priceList.hasRecipes();
        boolean isModdedItem = !containsStringInList(ForgeRegistries.ITEMS.getKey(shopItem.getItem()), List.of("minecraft"));
        ItemEntry matchingEntry = getItemEntry(shopItem);
        if (matchingEntry != null) return matchingEntry;

        int itemID;
        if (WorldShopCapability.itemEntryMap.isEmpty()) itemID = 0;
//        else if (matchingEntry != null) itemID = matchingEntry.getItemId();
        else itemID = Collections.max(WorldShopCapability.itemEntryMap.keySet()) + 1;

        ItemStack lockItem = ItemStack.EMPTY;
        if (!shopItem.isEdible() && (XPShopConfig.lockModItems && isModdedItem || XPShopConfig.lockUncraftableItems && !isCraftable)) {
            lockItem = shopItem;
        }
        boolean hideIfLocked = XPShopConfig.hideLockedItems;
        int stockAmount = 1;

        return new ItemEntry(itemID, getCurrentCategoryID(shopItem), shopItem, lockItem, hideIfLocked, stockAmount, priceList);
    }

    public static double calculateStatPrice(ItemStack shopItem) {
        //If their is already a matching ItemEntry, that means the item already has a price
        //If the item is in the skipped list, that means the item has no redeeming qualities
        //Go through the possible stats then calculate the price
        //If a price is established make sure
        LOGGER.warn("Stat Item: " + shopItem.getDisplayName().getString());
        ItemEntry matchingEntry = getItemEntry(shopItem);
        if (matchingEntry != null) return matchingEntry.getPrice(false);
        double skippedPrice = isSkipped(shopItem);
        if (skippedPrice != -1) {
            LOGGER.warn("Skipped: " + shopItem.getDisplayName().getString());
            return skippedPrice;
        }

        PriceList priceList = getPriceList(shopItem);

        PriceEvent priceEvent = new PriceEvent(shopItem, priceList);
        MinecraftForge.EVENT_BUS.post(priceEvent);

        if (!priceList.isEmpty()) {
//            boolean valid = priceList.hasStats() && getMatchingItemStack(shopItem, craftResultMap.keySet()) == null;
            boolean valid = priceList.hasStats();
            ItemEntry entry = createItemEntry(shopItem, priceList);
            addItemEntry(entry, valid);
            return entry.getPrice(false);
        } else return 0F;
    }

    public static void calculateRecipePrice(ItemStack shopStack) {
        PriceRecipeEvent recipeEvent = new PriceRecipeEvent(shopStack);
        MinecraftForge.EVENT_BUS.post(recipeEvent);
        PriceList priceList = getPriceList(shopStack);

        if (priceList.hasRecipes()) assignCategory(shopStack, CategoryEntry.CRAFTABLES);

        //What makes an item get skipped?
        //Its full price is 0
        //It's not in the allItems list
        //It's not a vanilla item, It's in the craft result map, and its recipe sum is 0.
        boolean skipItem = priceList.getFullPrice(false) == 0 ||
                getMatchingItemStack(shopStack, allItems) == null ||
                !Objects.equals(shopStack.getItem().getCreatorModId(shopStack), "minecraft") &&
                        craftResultMap.get(shopStack) != null &&
                        !priceList.hasRecipes();

        if (skipItem) {
            LOGGER.warn("Skip this item: " + shopStack.getDisplayName().getString() + ", Price: " + priceList.getFullPrice(false));
//            if (priceList.isEmpty()) priceList.addRecipe("Oddity", XPShopConfig.xpPerOddity, 1);
            skipItem(shopStack, priceList.getFullPrice(false));
            return;
        }
//        if (isValidEntry(shopStack) || isSkipped(shopStack) != -1)
//            LOGGER.error("THIS WAS ALREADY PRICED!! " + shopStack.getDisplayName().getString());
//        else LOGGER.warn("Item being fully priced: " + shopStack.getDisplayName().getString());

        ItemEntry entry = createItemEntry(shopStack, priceList);
        //Only add the item if its recipe price is higher than 0, or if it's the base item being checked
        addItemEntry(entry, true);
    }

    public static void initBlacklist() {
        if (!blackListedItems.isEmpty()) return;
        ArrayList<TagKey<?>> allTagKeys = new ArrayList<>();
        blackListedTags.clear();

        //Let's make a list of all tags
        for (ItemStack itemStack : allItems) {
            itemStack.getTags().toList().forEach((tagKey) -> {
                if (!allTagKeys.contains(tagKey)) allTagKeys.add(tagKey);
            });
            Block.byItem(itemStack.getItem()).defaultBlockState().getTags().toList().forEach((tagKey) -> {
                if (!allTagKeys.contains(tagKey)) allTagKeys.add(tagKey);
            });
        }
        //Then make a list of blackListed Tags
        for (TagKey<?> tagKey : allTagKeys) {
            if (containsStringInList(tagKey.location(), XPShopConfig.tagBlacklist)) {
                blackListedTags.add(tagKey);
            }
        }

        //Now to go through each item and see which ones are blacklisted
        for (ItemStack itemStack : new ArrayList<>(allItems)) {
            Item item = itemStack.getItem();

            //Check if the mod is banned
            if (containsStringInList(ForgeRegistries.ITEMS.getKey(item), XPShopConfig.modBlacklist)) {
                allItems.remove(itemStack);
                blackListedItems.add(itemStack);
                continue;
            }

            //Now check if the item is banned
            if (containsStringInList(ForgeRegistries.ITEMS.getKey(item), XPShopConfig.itemBlacklist)) {
                allItems.remove(itemStack);
                blackListedItems.add(itemStack);
                continue;
            }

            //Check if the items tags are banned
            if (containsTagKeyInList(itemStack, blackListedTags)) {
                allItems.remove(itemStack);
                blackListedItems.add(itemStack);
                continue;
            }

            if (getItemEntry(itemStack) != null) {
                allItems.remove(itemStack);
                continue;
            }

            //Check if the item was previously skipped
            if (isSkipped(itemStack) != -1) {
                allItems.remove(itemStack);
                blackListedItems.add(itemStack);
                continue;
            }
        }
    }

    public static void grabRecipes(Level level) {
        if (!craftResultMap.isEmpty()) return;
        Collection<Recipe<?>> recipes = level.getRecipeManager().getRecipes();
        LOGGER.warn("How many recipes are there? " + recipes.size());
        List<RecipeType<?>> failedRecipes = new ArrayList<>();


        MiniTicker<?> timer = ModTimer.getTimer(XPShop.MOD_ID, "RecipeTimer").ticker();
        for (Recipe<?> recipe : recipes) {
            addTask(() -> {
                List<ItemStack> craftList = new ArrayList<>(craftResultMap.keySet());

                ItemStack resultItem = recipe.getResultItem();
                if (resultItem.isEmpty()) {
                    LOGGER.error("Result item is missing, skip...");
                    if (!failedRecipes.contains(recipe.getType())) {
                        failedRecipes.add(recipe.getType());
                    }
                    return null;
                }
//            LOGGER.debug("Craft result item: " + resultItem.getDisplayName().getString());
                ItemStack mapStack = getMatchingItemStack(resultItem, craftList);
                InitialRecipeData initialRecipeData;

                if (mapStack == null) {
                    initialRecipeData = new InitialRecipeData(new ArrayList<>());
                    craftResultMap.put(resultItem, initialRecipeData);
                } else initialRecipeData = craftResultMap.get(mapStack);

                GatherIngredientsEvent ingredientsEvent = new GatherIngredientsEvent(recipe);
                MinecraftForge.EVENT_BUS.post(ingredientsEvent);

                //This will skip bad recipes
                if (ingredientsEvent.isCanceled()) {
                    LOGGER.warn(resultItem.getDisplayName().getString() + "Ingredient issue: Invalid recipe, skip...");
                    return null;
                }
                //This will skip empty recipes
                else if (ingredientsEvent.getIngredients().isEmpty()) {
                    LOGGER.warn(resultItem.getDisplayName().getString() + "Ingredient issue: Couldn't find any ingredients, skip...");
                    if (!failedRecipes.contains(recipe.getType())) {
                        failedRecipes.add(recipe.getType());
                    }
                    return null;
                }

                //Add the recipe
                List<Ingredient> ingredients = new ArrayList<>();

                //Grab the ingredient items from the recipe.
                for (Ingredient ingredient : ingredientsEvent.getIngredients()) {
                    if (ingredient == null) continue;
                    if (ingredient.isEmpty()) continue;
                    ingredients.add(ingredient);
                }

                //Add the recipe and its ingredients to recipe data
                initialRecipeData.recipes.add(Pair.of(recipe, ingredients));
                return null;
            });
        }
        LOGGER.info(timer.record("Queuing all of the recipe tasks"));
        waitForTasks();
        LOGGER.info(timer.record("Went through all of the recipes"));

        LOGGER.info("These are all the invalid recipe types: ");
        failedRecipes.forEach((type) -> LOGGER.debug(type.toString()));
    }
    public static void grabOresAndDrops(ServerLevel level) {
        Map<Block, Double> oreMap = new HashMap<>();

        for (Map.Entry<ResourceKey<ConfiguredFeature<?, ?>>, ConfiguredFeature<?, ?>> mapEntry : BuiltinRegistries.CONFIGURED_FEATURE.entrySet()) {
            ConfiguredFeature<?, ?> feature = mapEntry.getValue();

//            LOGGER.error("What's the configured feature called? " + feature.getClass());
//            LOGGER.error("What about the config? " + feature.config().getClass());
            if (!(feature.config() instanceof OreConfiguration oreConfig)) continue;

            for (OreConfiguration.TargetBlockState target : oreConfig.targetStates) {
                Block block = target.state.getBlock();
                double currSize = oreMap.get(block) == null ? 0 : oreMap.get(block);

                oreMap.put(block, currSize + Math.max(1, (oreConfig.size * (1F - oreConfig.discardChanceOnAirExposure))));
                ItemStack oreStack = new ItemStack(block.asItem());
                assignCategory(oreStack, CategoryEntry.ORE_DROPS);
            }
        }
        List<Block> blocks = new ArrayList<>(oreMap.keySet());
        blocks.sort((a, b) -> {
            double sizeA = oreMap.get(a);
            double sizeB = oreMap.get(b);
            return Double.compare(sizeA, sizeB);
        });
        Collections.reverse(blocks);

        //region Grab all ores, sort them, and give them the proper price
        LOGGER.info("Pricing the Ores");
        double count = 1;
        double prevSize = 0;
        for (Block block : blocks) {
            if (oreMap.get(block) < prevSize) count += 1;
            prevSize = oreMap.get(block);
//            oreMap.put(block, (double) (count * XPShopConfig.xpOreStep));
            getPriceList(new ItemStack(block.asItem())).addStat("Ore drop", count * XPShopConfig.xpOreStep);
//            LOGGER.warn("Ore: " + block.getName().getString() + ", Size: " + prevSize + ", Price: " + oreMap.get(block));
        }
        //endregion

        //region Grab all the ore drops, then price them.
        LOGGER.info("Pricing the Drops");
        for (Block block : oreMap.keySet()) {
            BlockState defaultState = block.defaultBlockState();
            String priceTypeString = block.getName().getString() + " Drop";
            double totalPrice = getPriceList(new ItemStack(block.asItem())).getFullPrice(false);
            //Make a loot context builder(take from loot command)
            LootContext lootContext = (new LootContext.Builder(level))
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(Vec3i.ZERO))
                    .withParameter(LootContextParams.BLOCK_STATE, defaultState)
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .create(LootContextParamSets.BLOCK);
            LootTable loottable = level.getServer().getLootTables().get(block.getLootTable());
            calculateDropCosts(loottable, lootContext, 100, CategoryEntry.ORE_DROPS, priceTypeString, totalPrice);
        }
        //endregion
    }

    public static void grabMobDrops(ServerLevel level) {
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            var entity = entityType.create(level);
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            String priceTypeString = livingEntity.getName().getString() + " Mob Drop:";

            LOGGER.debug("Mob: " + livingEntity.getName().getString());
            double totalHealth = livingEntity.getMaxHealth();
            if (totalHealth <= 0) continue;
            double damage = CombatRules.getDamageAfterAbsorb((float) totalHealth, (float) livingEntity.getArmorValue(), (float) livingEntity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
            //If the mob has armor, this will give the true amount of health the mob should have.
            totalHealth += (totalHealth - damage);
            LOGGER.debug("Mob health: " + totalHealth);
            double totalPrice = (double) (totalHealth * XPShopConfig.xpPerMobHealth);
            if (livingEntity.getType().is(Tags.EntityTypes.BOSSES)) {
                LOGGER.debug("Was a boss, double the price");
                totalPrice *= 2;
            }
            LOGGER.debug("Mob price: " + totalPrice);

            ResourceLocation mobLocation = livingEntity.getLootTable();
            LootTable lootTable = level.getServer().getLootTables().get(mobLocation);
            Player player = (Player) StatPriceEvents.getDummyEntity();
            DamageSource dmgSource = DamageSource.playerAttack(player);

            LootContext.Builder lootcontext$builder = (new LootContext.Builder(level)).withRandom(livingEntity.getRandom())
                    .withParameter(LootContextParams.THIS_ENTITY, entity)
                    .withParameter(LootContextParams.ORIGIN, livingEntity.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, dmgSource)
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, dmgSource.getEntity())
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, dmgSource.getDirectEntity());
            lootcontext$builder = lootcontext$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).withLuck(0);
            LootContext ctx = lootcontext$builder.create(LootContextParamSets.ENTITY);

            calculateDropCosts(lootTable, ctx, 100, CategoryEntry.MOB_DROPS, priceTypeString, totalPrice);

            entity.captureDrops(new ArrayList<>());
            ((LivingEntity) entity).dropCustomDeathLoot(dmgSource, 0, true);
            Collection<ItemEntity> customDrops = entity.captureDrops(new ArrayList<>());
            if (customDrops.isEmpty()) continue;
            LOGGER.debug(entity.getName().getString() + " has " + customDrops.size() + " custom drop(s)");

            double customLootPrice = totalPrice / (double) customDrops.size();
            for (ItemEntity itemEntity : customDrops) {
                ItemStack customStack = itemEntity.getItem();
                if (isSkipped(customStack) != -1 || getItemEntry(customStack) != null) continue;
                assignCategory(customStack, CategoryEntry.MOB_DROPS);
                double individualPrice = customLootPrice / customStack.getCount();
                getPriceList(customStack).addStat(priceTypeString, individualPrice);

//                LOGGER.debug(customStack.getDisplayName().getString() + " will cost: " + dropsMap.get(getMatchingItemStack(customStack, dropsMap.keySet())));
                itemEntity.setItem(ItemStack.EMPTY);
            }
        }
    }

    public static void grabResources() {
        LOGGER.info("Grabbing Basic Resource tags");
        for (String tag : XPShopConfig.basicResourceList) {
            try {
                List<String> parts = List.of(tag.split(":"));
                if (parts.size() != 2 && parts.size() != 3) {
                    LOGGER.error("THIS RESOURCE STRING WAS MADE INCORRECTLY: " + tag);
                    continue;
                }

                String tagToLookFor;
                double price;
                if (parts.size() == 2) {
                    tagToLookFor = parts.get(0);
                    price = Double.parseDouble(parts.get(1));
                } else {
                    tagToLookFor = parts.get(0) + ":" + parts.get(1);
                    price = Double.parseDouble(parts.get(2));
                }

                basicResourceMap.put(tagToLookFor, price);
                LOGGER.warn("Resource: " + tagToLookFor + ", Price: " + price);
            } catch (Error error) {
                error.printStackTrace();
            }
        }
    }

    public static PriceList getPriceList(ItemStack priceStack) {
        ItemStack matchingStack = getMatchingItemStack(priceStack, priceListMap.keySet());
        if (matchingStack == null) {
            priceListMap.put(priceStack, new PriceList(priceStack));
            matchingStack = priceStack;
        }
        return priceListMap.get(matchingStack);
    }

    public static ItemEntry getItemEntry(ItemStack stackToFind) {
        Collection<ItemEntry> entries = new ArrayList<>(allItemEntries.keySet());

        for (var itemEntry : entries) {
            boolean sameItem = stackToFind.sameItem(itemEntry.getShopItem());
            boolean matchingTags = ItemStack.tagMatches(stackToFind, itemEntry.getShopItem());
            if (sameItem && matchingTags) return itemEntry;
        }

        return null;
    }

    public synchronized static void addItemEntry(ItemEntry entry, boolean valid) {
        ItemEntry matchingEntry = null;
        for (ItemEntry itemEntry : allItemEntries.keySet()) {
            boolean sameItem = entry.getShopItem().sameItem(itemEntry.getShopItem());
            boolean matchingTags = ItemStack.tagMatches(entry.getShopItem(), itemEntry.getShopItem());
            if (sameItem && matchingTags) {
                matchingEntry = itemEntry;
                break;
            }
        }
        if (matchingEntry != null) allItemEntries.remove(matchingEntry);

        allItemEntries.put(entry, valid);
        if (valid) {
            PriceList priceList = getPriceList(entry.getShopItem());
            PriceList.PriceInfo uneditedInfo = priceList.getFinalPriceInfo();

            WorldShopCapability.itemEntryMap.put(entry.getItemId(), entry);
            LOGGER.warn(entry.getShopItem().getDisplayName().getString() + " is fully priced! " +
                    "(Full Price:" + priceList.getFullPrice(false) +
                    ",Low Price:" + uneditedInfo.lowPrice() + ", High Price:" + uneditedInfo.highPrice());
        }
    }

    public static ItemStack getMatchingItemStack(ItemStack stackToFind, Collection<ItemStack> stackList) {
        for (ItemStack stack : new ArrayList<>(stackList)) {
            if (!stackToFind.sameItem(stack)) continue;
            if (!ItemStack.tagMatches(stackToFind, stack)) continue;
            return stack;
        }
        return null;
    }

    public static boolean containsStringInList(@Nullable ResourceLocation location, Collection<? extends String> stringList) {
        if (location == null) return false;
        String locationString = location.toString().toLowerCase(Locale.ROOT);

        for (String string : stringList) {
            if (locationString.contains(string)) return true;
        }

        return false;
    }

    public static boolean containsTagKeyInList(ItemStack itemStack, List<TagKey<?>> tagList) {
        for (TagKey<?> itemTag : itemStack.getTags().toList()) {
            if (tagList.contains(itemTag)) return true;
        }
        for (TagKey<?> itemTag : Block.byItem(itemStack.getItem()).defaultBlockState().getTags().toList()) {
            if (tagList.contains(itemTag)) return true;
        }

        return false;
    }

    public static void assignCategory(ItemStack itemStack, CategoryEntry categoryEntry) {
        ItemStack matchingStack = getMatchingItemStack(itemStack, categoryMap.keySet());

        if (matchingStack == null) {
            categoryMap.put(itemStack, categoryEntry);
        } else if (categoryMap.get(matchingStack).getPriority() < categoryEntry.getPriority()) {
            categoryMap.put(matchingStack, categoryEntry);
        }
    }

    public static int getCurrentCategoryID(ItemStack itemStack) {
        ItemStack matchingStack = getMatchingItemStack(itemStack, categoryMap.keySet());
        return categoryMap.getOrDefault(matchingStack, CategoryEntry.ODDITY).getCategoryId();
    }

    public static CategoryEntry getOrCreateCategory(Integer priority, String categoryName, ItemStack displayItem) {
        for (CategoryEntry categoryEntry : WorldShopCapability.categoryEntryMap.values()) {
            if (categoryEntry.getCategoryName().contains(categoryName)) {
                return categoryEntry;
            }
        }

        int highestNumber;
        if (WorldShopCapability.categoryEntryMap.isEmpty()) highestNumber = 0;
        else highestNumber = Collections.max(WorldShopCapability.categoryEntryMap.keySet());
        CategoryEntry categoryEntry = new CategoryEntry(highestNumber + 1, priority, categoryName, displayItem);
        WorldShopCapability.categoryEntryMap.put(categoryEntry.getCategoryId(), categoryEntry);

        return categoryEntry;
    }

    public static Map<ItemStack, Double> calculateDropCosts(LootTable lootTable, LootContext lootContext, int totalRolls, CategoryEntry category, String dropType, double totalPrice) {
        Map<ItemStack, Double> countMap = new HashMap<>();
        for (int a = 0; a < totalRolls; a++) {
            for (ItemStack stack : lootTable.getRandomItems(lootContext)) {

                if (stack.isEmpty()) continue;

                ItemStack mapStack = getMatchingItemStack(stack, countMap.keySet());
                if (mapStack == null) {
                    countMap.put(stack, (double) stack.getCount());
                } else {
                    countMap.put(mapStack, countMap.get(mapStack) + stack.getCount());
                }
            }
        }

        if (countMap.isEmpty()) {
            LOGGER.debug("This object doesn't drop anything...");
            return countMap;
        }
        double itemCountAverage = 0F;
        //This will make the countMap value equal the average amount of each item per roll
        for (Map.Entry<ItemStack, Double> entry : countMap.entrySet()) {
            entry.setValue((entry.getValue() / totalRolls));
            itemCountAverage += entry.getValue();
        }
        totalPrice = totalPrice / itemCountAverage;
        LOGGER.debug("Total price for each item: " + totalPrice);

        for (Map.Entry<ItemStack, Double> entry : new ArrayList<>(countMap.entrySet())) {
            if (entry.getValue() < 1)
                LOGGER.debug("Drop: " + entry.getKey().getDisplayName().getString() + ", Average: " + entry.getValue() + ", Price(totalPrice * (1 + (1 - dropAverage))): " + (totalPrice * (1 + (1 - entry.getValue()))));
            if (entry.getValue() > 1)
                LOGGER.debug("Drop: " + entry.getKey().getDisplayName().getString() + ", Average: " + entry.getValue() + ", Price(totalPrice/dropAverage): " + (totalPrice / entry.getValue()));
            if (entry.getValue() < 0.1) {
                LOGGER.error("here's one below the threshold, skipping...");
                countMap.remove(entry.getKey());
                continue;
            }
            //This gets the true price of each itemStack in the countMap
            if (entry.getValue() < 1) entry.setValue(totalPrice * (1 + (1 - entry.getValue())));
            else entry.setValue(totalPrice / entry.getValue());

        }

        for (Map.Entry<ItemStack, Double> entry : countMap.entrySet()) {
//            //This will make it so dupe stats don't keep getting added to already existing item entries
//            if (getItemEntry(entry.getKey()) != null) continue;
            if (isSkipped(entry.getKey()) != -1 || getItemEntry(entry.getKey()) != null) continue;
            assignCategory(entry.getKey(), category);
            getPriceList(entry.getKey()).addStat(dropType, entry.getValue());
        }
        return countMap;
    }

    public static void skipItem(ItemStack stack, double skipPrice) {
        WorldShopCapability.skippedStackMap.put(stack, skipPrice);
        blackListedItems.add(stack);
    }

    public static double isSkipped(ItemStack stack) {
        ItemStack skippedStack = getMatchingItemStack(stack, WorldShopCapability.skippedStackMap.keySet());
        return WorldShopCapability.skippedStackMap.getOrDefault(skippedStack, -1D);
    }

    public static boolean isValidEntry(ItemStack stack) {
//        LOGGER.error("Is an entry? " + ((getItemEntry(stack) != null)) +
//                ", Is valid? " + (allItemEntries.getOrDefault(getItemEntry(stack), false)));
        ItemEntry entry = getItemEntry(stack);
        if (entry == null) return false;
        return allItemEntries.getOrDefault(entry, false);
    }

    public static <U> CompletableFuture<U> addTask(Supplier<U> supplier) {
        CompletableFuture<U> future = CompletableFuture.supplyAsync(supplier, threadPool)
                .exceptionally(ex -> {
                    System.err.println("Error in task: " + ex.getMessage());
                    ex.printStackTrace();
                    threadPool.shutdownNow();
                    return null;
                });
        futures.add(future);
        return future;
    }

    public static void waitForTasks() {
        futures.forEach(CompletableFuture::join);
        futures.clear();
    }

    public record PriceBranchData(double highestBadCount, double totalBadCount, double highestBranch,
                                  double totalBranchCount, List<ItemStack> stackList) {
        public void calculateNewInfo(ItemData.PriceBranch newBranch, ItemData data) {
            MiniTicker<?> stopWatch =
                    ModStopWatch.getTimer(XPShop.MOD_ID, "BranchInfo", ModStopWatch.Time.ACCUMULATE).ticker();
            stopWatch.reset();
            double highestBadCount = priceBranchData.highestBadCount();
            double totalBadCount = priceBranchData.totalBadCount();
            double highestBranch = priceBranchData.highestBranch();
            double totalBranchCount = priceBranchData.totalBranchCount();

            if (highestBadCount < (newBranch.invalidList().size())) {
                highestBadCount = newBranch.invalidList().size();
            }
            totalBadCount += newBranch.invalidList().size();

            int count = 1;
            for (var entry : data.branches.entrySet()){
                count += entry.getValue().size();
            }

            if (highestBranch < count) {
                highestBranch = count;
            }
            totalBranchCount += 1;
            if (!this.stackList.contains(data.mainItem)) this.stackList.add(data.mainItem);

            LOGGER.error("The item we are creating the price branch for: " + ItemData.getItemName(data));
            LOGGER.error("Price branch " + (count));
//            PriceGroup.GroupOperation op = ((list) -> {
//                list.removeIf((info) -> info.highPrice() == 0);
//                return PriceList.compilePriceListData(ItemData.getItemName(data), list);
//            });
//            LOGGER.error(new PriceGroup(data, newBranch.priceGroupList(),op, null).getResult().toString());
//            List<ItemData> ingredientList = new ArrayList<>(data.ingredientList);
//            ingredientList.removeIf(stack -> newBranch.invalidList().contains(stack));
//            LOGGER.error("Valid ingredients:" + "(" + ingredientList.size() + ") " + ItemData.getItemDataNames(ingredientList));

            LOGGER.error("Bad ingredients:" + "(" + newBranch.invalidList().size() + ") " + ItemData.getItemDataNames(newBranch.invalidList()));

            priceBranchData = new PriceBranchData(highestBadCount, totalBadCount,
                    highestBranch, totalBranchCount, this.stackList);
            stopWatch.record("");
        }

        public double getAverageBadCount() {
            return totalBadCount / this.stackList.size();
        }

        public double getAverageBranchCount() {
            return totalBranchCount / this.stackList.size();
        }
    }
//    private static void clearData(boolean fullClean) {
//        allItemEntries.clear();
//        allItems.clear();
//        craftResultMap.clear();
//        ingredientItemMap.clear();
//        basicResourceMap.clear();
//        categoryMap.clear();
//        blackListedItems.clear();
//        blackListedTags.clear();
//    }
}
