package invoker54.xpshop.config;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.data.ModLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class XPShopConfig {
    //Debug setting
    public static AtomicBoolean debugMode = new AtomicBoolean(true);
    private static final ModLogger LOGGER = ModLogger.getLogger(debugMode);
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    //Wallet Tiers config
    public static int tierZero;
    public static int tierOne;
    public static int tierTwo;
    public static int tierThree;
    public static int tierFour;

    //Selling config
    public static double salePercentage;
    public static double xpPerJunk;

    //Base Auto-generation config
    public static double xpPerArmor;
    public static double xpPerDurability;
    public static double xpPerDamage;
    public static double xpPerEnchantLvl;
    public static double xpPerFood;
    public static double xpPerMobHealth;
    public static double xpOreStep;
    public static double xpPerOddity;
    public static List<? extends String> basicResourceList = new ArrayList<>();
    public static double xpRarityMultiplier;

    public static double priceJitter;

    //Auto-generation locks config
    public static boolean lockModItems;
    public static boolean lockUncraftableItems;
    public static boolean hideLockedItems;

    //Auto-generation Black list config
    public static List<? extends String> itemBlacklist = new ArrayList<>();
    public static List<? extends String> tagBlacklist = new ArrayList<>();
    public static List<? extends String> modBlacklist = new ArrayList<>();

    private static boolean isDirty = false;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static CompoundTag serialize(){
        CompoundTag mainTag = new CompoundTag();
        mainTag.putInt("tierZero",tierZero);
        mainTag.putInt("tierOne",tierOne);
        mainTag.putInt("tierTwo",tierTwo);
        mainTag.putInt("tierThree",tierThree);
        mainTag.putInt("tierFour",tierFour);
        
        mainTag.putDouble("salePercentage", salePercentage);
        mainTag.putDouble("xpPerJunk", xpPerJunk);
        
        mainTag.putDouble("xpPerArmor", xpPerArmor);
        mainTag.putDouble("xpPerDurability", xpPerDurability);
        mainTag.putDouble("xpPerDamage", xpPerDamage);
        mainTag.putDouble("xpPerEnchantLvl", xpPerEnchantLvl);
        mainTag.putDouble("xpPerFood", xpPerFood);
        mainTag.putDouble("xpPerMobHealth", xpPerMobHealth);
        mainTag.putDouble("xpOreStep", xpOreStep);
        mainTag.putDouble("xpPerOddity", xpPerOddity);
        mainTag.putString("basicResourceList", String.join(";",basicResourceList));
        mainTag.putDouble("xpRarityMultiplier", xpRarityMultiplier);
        mainTag.putDouble("priceJitter", priceJitter);

        mainTag.putBoolean("lockModItems", lockModItems);
        mainTag.putBoolean("lockUncraftableItems", lockUncraftableItems);
        mainTag.putBoolean("hideLockedItems", hideLockedItems);

        mainTag.putString("itemBlacklist", String.join(";",itemBlacklist));
        mainTag.putString("tagBlacklist", String.join(";",tagBlacklist));
        mainTag.putString("modBlacklist", String.join(";",modBlacklist));
        return mainTag;
    }

    public static void deserialize(CompoundTag mainTag){
        tierZero = mainTag.getInt("tierZero");
        tierOne = mainTag.getInt("tierOne");
        tierTwo = mainTag.getInt("tierTwo");
        tierThree = mainTag.getInt("tierThree");
        tierFour = mainTag.getInt("tierFour");
        
        salePercentage = mainTag.getDouble("salePercentage");
        xpPerJunk = mainTag.getDouble("xpPerJunk");

        xpPerArmor = mainTag.getDouble("xpPerArmor");
        xpPerDurability = mainTag.getDouble("xpPerDurability");
        xpPerDamage = mainTag.getDouble("xpPerDamage");
        xpPerEnchantLvl = mainTag.getDouble("xpPerEnchantLvl");
        xpPerFood = mainTag.getDouble("xpPerFood");
        xpPerMobHealth = mainTag.getDouble("xpPerMobHealth");
        xpOreStep = mainTag.getDouble("xpOreStep");
        xpPerOddity = mainTag.getDouble("xpPerOddity");
        basicResourceList = Arrays.stream(mainTag.getString("basicResourceList").split(";")).toList();
        xpRarityMultiplier = mainTag.getDouble("xpRarityMultiplier");
        priceJitter = mainTag.getDouble("priceJitter");

        lockModItems = mainTag.getBoolean("lockModItems");
        lockUncraftableItems = mainTag.getBoolean("lockUncraftableItems");
        hideLockedItems = mainTag.getBoolean("hideLockedItems");

        itemBlacklist = Arrays.stream(mainTag.getString("itemBlacklist").split(";")).toList();
        tagBlacklist = Arrays.stream(mainTag.getString("tagBlacklist").split(";")).toList();
        modBlacklist = Arrays.stream(mainTag.getString("modBlacklist").split(";")).toList();
    }
    
    public static void bakeCommonConfig(){
        //System.out.println("SYNCING CONFIG SHTUFF");
//        randomCategoryCount = COMMON.randomCategoryCount.get();
        tierZero = COMMON.tierZero.get();
        tierOne = COMMON.tierOne.get();
        tierTwo = COMMON.tierTwo.get();
        tierThree = COMMON.tierThree.get();
        tierFour = COMMON.tierFour.get();

        salePercentage = COMMON.salePercentage.get();
        xpPerJunk = COMMON.xpPerJunk.get();

        xpPerArmor = COMMON.xpPerArmor.get();
        xpPerDurability = COMMON.xpPerDurability.get();
        xpPerDamage = COMMON.xpPerDamage.get();
        xpPerEnchantLvl = COMMON.xpPerEnchantLvl.get();
        xpPerFood = COMMON.xpPerFood.get();
        xpPerMobHealth = COMMON.xpPerMobHealth.get();
        xpOreStep = COMMON.xpOreStep.get();
        xpPerOddity = COMMON.xpPerOddity.get();
        basicResourceList = COMMON.basicResourceList.get();
        xpRarityMultiplier = COMMON.xpRarityMultiplier.get();
        priceJitter = COMMON.priceJitter.get();

        lockModItems = COMMON.lockModItems.get();
        lockUncraftableItems = COMMON.lockUncraftableItems.get();
        hideLockedItems = COMMON.hideLockedItems.get();

        itemBlacklist = COMMON.itemBlacklist.get();
        tagBlacklist = COMMON.tagBlacklist.get();
        modBlacklist = COMMON.modBlacklist.get();

        debugMode.set(COMMON.debugMode.get());
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());
        if(eventConfig.getConfig().getSpec() == XPShopConfig.COMMON_SPEC){
            bakeCommonConfig();
            markDirty(true);
        }
    }

    public static void markDirty(boolean dirty){
        isDirty = dirty;
    }
    public static boolean isDirty(){
        return isDirty;
    }
    
    public static class CommonConfig {

        //This is how to make a config value
//        public final ForgeConfigSpec.ConfigValue<Integer> exampleInt;
        //public final ForgeConfigSpec.ConfigValue<Integer> timeLeft;
        //Wallet Tiers config
        public final ForgeConfigSpec.ConfigValue<Integer> tierZero;
        public final ForgeConfigSpec.ConfigValue<Integer> tierOne;
        public final ForgeConfigSpec.ConfigValue<Integer> tierTwo;
        public final ForgeConfigSpec.ConfigValue<Integer> tierThree;
        public final ForgeConfigSpec.ConfigValue<Integer> tierFour;

        //Selling config
        public final ForgeConfigSpec.ConfigValue<Double> salePercentage;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerJunk;

        //Auto-generation config
        public final ForgeConfigSpec.ConfigValue<Double> xpPerArmor;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerDurability;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerDamage;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerEnchantLvl;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerFood;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerMobHealth;
        public final ForgeConfigSpec.ConfigValue<Double> xpOreStep;
        public final ForgeConfigSpec.ConfigValue<Double> xpPerOddity;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> basicResourceList;
        public final ForgeConfigSpec.ConfigValue<Double> xpRarityMultiplier;
        public final ForgeConfigSpec.ConfigValue<Double> priceJitter;
        public final ForgeConfigSpec.ConfigValue<Boolean> lockModItems;
        public final ForgeConfigSpec.ConfigValue<Boolean> lockUncraftableItems;
        public final ForgeConfigSpec.ConfigValue<Boolean> hideLockedItems;


        //Auto-generation Black list config
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> itemBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> tagBlacklist;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> modBlacklist;

        //Debug
        public final ForgeConfigSpec.ConfigValue<Boolean> debugMode;


        public CommonConfig(ForgeConfigSpec.Builder builder) {
            //This is what goes on top inside the config
            builder.push("Wallet Tiers");
            //This is how you place a variable in the config file
            tierZero = builder.comment("Base max XP").defineInRange("Tier_Zero", 315, 0, Integer.MAX_VALUE);
            tierOne = builder.comment("Tier one max XP").defineInRange("Tier_One", 850, 0, Integer.MAX_VALUE);
            tierTwo = builder.comment("Tier two max XP").defineInRange("Tier_Two", 1646, 0, Integer.MAX_VALUE);
            tierThree = builder.comment("Tier three max XP").defineInRange("Tier_Three", 2702, 0, Integer.MAX_VALUE);
            tierFour = builder.comment("Tier four max XP").defineInRange("Tier_Four", 4020, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sale Config");
            salePercentage = builder.comment("Percentage of buy price that you can sell an item for").defineInRange("Sale_Percentage", 0.25D, 0, 1);
            xpPerJunk = builder.comment("Flat value for items without a Buy Price").defineInRange("XP_For_Junk", 3, 0, Double.MAX_VALUE);
            builder.pop();

            builder.push("Auto-Generation Config");
            xpPerArmor = builder.comment("How much XP per protection and toughness").defineInRange("XP_Per_Armor", 45, 0, Double.MAX_VALUE);
            xpPerDurability = builder.comment("How much XP per durability").defineInRange("XP_Per_Durability", 0.75, 0, Double.MAX_VALUE);
            xpPerDamage = builder.comment("How much XP per damage the item deals").defineInRange("XP_Per_Damage", 50, 0, Double.MAX_VALUE);
            xpPerEnchantLvl = builder.comment("How much XP per enchant level").defineInRange("XP_Per_Enchant", 200, 0, Double.MAX_VALUE);
            xpPerFood = builder.comment("How much XP per food and saturation").defineInRange("XP_Per_Food", 2.5, 0, Double.MAX_VALUE);
            xpPerMobHealth = builder.comment("How much XP per mob health point").defineInRange("XP_Per_Mob_Health", 0.75, 0, Double.MAX_VALUE);
            xpOreStep = builder.comment("XP increase per ore rarity level").defineInRange("XP_Ore_Step", 8.5, 0, Double.MAX_VALUE);
            xpPerOddity = builder.comment("Base XP for items that couldn't be priced").defineInRange("XP_Per_Oddity", 32, 0, Double.MAX_VALUE);
            basicResourceList = builder.comment("For block and item tags that are considered a basic resource (USAGE: MOD_ID:TAG:XP or TAG:XP)").defineList("Basic_Resource_Tags",
                    new ArrayList<>(Arrays.asList("forge:logs:8", "minecraft:logs:8", "minecraft:leaves:4", "forge:cobblestone:2", "forge:stone:2", "carver_replaceable:2", "base_stone:2", "forge:obsidian:16",
                    "forge:end_stones:4", "minecraft:crops:32", "minecraft:barrier:1", "forge:ores/iron:68")), (string) -> string instanceof String);
            xpRarityMultiplier = builder.comment("XP cost multiplier based on rarity stat of item (Common is 0)").defineInRange("Rarity_Multiplier", 0.25F, 0, Double.MAX_VALUE);
            priceJitter = builder.comment("Shifts the price up or down by a random percentage between 0 and this value").defineInRange("Price_Jitter", 0.25F, 0, 1);

            lockModItems = builder.comment("If modded items should automatically be locked").define("Lock_Modded_items", true);
            lockUncraftableItems = builder.comment("If uncraftable items should be locked").define("Lock_Uncraftable_items", true);
            hideLockedItems = builder.comment("If locked items are hidden").define("Hide_Locked_Items", false);

            itemBlacklist = builder.comment("List for items/blocks that shouldn't be considered for auto-generation (USAGE: MOD_ID:ITEM or ITEM)").defineList("Blacklisted_Items",
                    new ArrayList<>(Arrays.asList("tipped_arrow", "ae2:facade", "integrateddynamics:facade", "xnet:facade", "coral_block", "refinedstorage:cover", "creative")), (string) -> string instanceof String);
            tagBlacklist = builder.comment("List of tags for items/blocks that shouldn't be considered for auto-generation (USAGE: MOD_ID:TAG or TAG)").defineList("Blacklisted_Tags",
                    new ArrayList<>(Arrays.asList("slabs", "stairs", "fences", "stained_glass", "walls", "carpets", "banners", "pressure_plates",
                            "beds", "candles", "fence_gates", "doors", "buttons", "trapdoors", "boats", "signs", "planks", "dirt", "forge:concrete")), (string) -> string instanceof String);
            modBlacklist = builder.comment("List of mod ids for items/blocks that shouldn't be considered for auto-generation (USAGE: MOD_ID)").defineList("Blacklisted_Mod_IDs",
                    new ArrayList<>(Arrays.asList("rechiseled")), (string) -> string instanceof String);

            debugMode = builder.comment("Should debug mode be on").define("Debug_Mode", true);
            builder.pop();
        }
    }
}