package invoker54.xpshop.common.config;

import invoker54.xpshop.XPShop;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ShopConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    //Shop stuff
    public static int randomCategoryCount;
    public static int randomBuyEntryCount;
    public static int refreshTime;
    public static int shopFee;
    public static int shopUnlockTime;
    public static boolean takeLockItem;
    public static boolean showXP;
    private static boolean isDirty = false;

    //Player Stuff
    public static List<Integer> walletAmount = new ArrayList<>();
    public static int permissionLvl;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static CompoundNBT serialize(){
        CompoundNBT mainNBT = new CompoundNBT();
        //First isRandom
//        mainNBT.putBoolean("isRandom", isRandom);
        mainNBT.putIntArray("walletAmount", walletAmount);
        mainNBT.putInt("shopFee", shopFee);
        mainNBT.putInt("shopUnlockTime", shopUnlockTime);

        return mainNBT;
    }

    public static void deserialize(CompoundNBT mainNBT){
        //First isRandom
//        isRandom = mainNBT.getBoolean("isRandom");
        walletAmount = Arrays.stream(mainNBT.getIntArray("walletAmount")).boxed().collect(Collectors.toList());
        shopFee = mainNBT.getInt("shopFee");
        shopUnlockTime = mainNBT.getInt("shopUnlockTime");
    }
    
    public static void bakeCommonConfig(){
        //System.out.println("SYNCING CONFIG SHTUFF");
        randomCategoryCount = COMMON.randomCategoryCount.get();
        randomBuyEntryCount = COMMON.randomBuyEntryCount.get();
        refreshTime = COMMON.refreshTime.get();
        takeLockItem = COMMON.takeLockItem.get();
        showXP = COMMON.showXP.get();
        walletAmount = (List<Integer>) COMMON.walletAmount.get();
        permissionLvl = COMMON.permissionLvl.get();
        shopFee = COMMON.shopFee.get();
        shopUnlockTime = COMMON.shopUnlockTime.get();
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfig.ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());
        if(eventConfig.getConfig().getSpec() == ShopConfig.COMMON_SPEC){
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
        //public static final ForgeConfigSpec.ConfigValue<Integer> exampleInt;
        //public final ForgeConfigSpec.ConfigValue<Integer> timeLeft;
        public final ForgeConfigSpec.ConfigValue<Integer> randomCategoryCount;
        public final ForgeConfigSpec.ConfigValue<Integer> randomBuyEntryCount;
        public final ForgeConfigSpec.ConfigValue<Integer> refreshTime;
        public final ForgeConfigSpec.ConfigValue<Boolean> takeLockItem;
        public final ForgeConfigSpec.ConfigValue<Boolean> showXP;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> walletAmount;
        public final ForgeConfigSpec.ConfigValue<Integer> permissionLvl;
        public final ForgeConfigSpec.ConfigValue<Integer> shopFee;
        public final ForgeConfigSpec.ConfigValue<Integer> shopUnlockTime;


        public CommonConfig(ForgeConfigSpec.Builder builder) {
            //This is what goes on top inside of the config
            builder.push("Shop Stuff");
            //This is how you place a variable in the config file
            //exampleInt = BUILDER.comment("This is an integer. Default value is 3.").define("Example Integer", 54);
            randomCategoryCount = builder.comment("How many categories to choose from (0 will be all categories)").defineInRange("Random_Category_Count", 4, 0,Integer.MAX_VALUE);
            randomBuyEntryCount = builder.comment("How many buy entries to choose (0 will be all entries in every selected category)").defineInRange("Random_Buy_Entry_Count", 14, 0,Integer.MAX_VALUE);
            refreshTime = builder.comment("How long in seconds it takes to refresh current deals").defineInRange("Refresh_Time", 1500, 10,Integer.MAX_VALUE);
            takeLockItem = builder.comment("Take the items you use to unlock shop items").define("Take_Items", true);
            shopFee = builder.comment("Max amount of xp it'll cost to unlock the XP Shop (Setting to 0 disables shop fee)(Fee will be 1/6th of the current wallet tier)").defineInRange("Shop_Max_Fee", 400, 0, Integer.MAX_VALUE);
            shopUnlockTime = builder.comment("How long in seconds the shop will remain unlocked for").defineInRange("Shop_Unlock_Time", 1500, 10,Integer.MAX_VALUE);
            builder.pop();


            builder.push("Player stuff");
            showXP = builder.comment("If total xp should show while not sneaking").define("Show_XP", true);
            walletAmount = builder.comment("How much xp the player may carry per level (blank lvls count as infinite)").defineList("XP_PER_LVL",
                    Arrays.asList(330, 751, 1507, 2920), toTest -> toTest instanceof Integer);
            permissionLvl = builder.comment("What permission level you must be to change the shop contents").defineInRange("Shop_Permission_Level", 2, 0,Integer.MAX_VALUE);
        }
    }
}
