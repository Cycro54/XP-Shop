package invoker54.xpshop.common.config;

import invoker54.xpshop.XPShop;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ShopConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static int randomCategoryCount;
    public static int randomBuyEntryCount;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

//    public static CompoundNBT serialize(){
//        CompoundNBT mainNBT = new CompoundNBT();
//        //First isRandom
////        mainNBT.putBoolean("isRandom", isRandom);
//
//        return mainNBT;
//    }
//
//    public static void deserialize(CompoundNBT mainNBT){
//        //First isRandom
////        isRandom = mainNBT.getBoolean("isRandom");
//    }
    
    public static void bakeCommonConfig(){
        //System.out.println("SYNCING CONFIG SHTUFF");
        randomCategoryCount = COMMON.randomCategoryCount.get();
        randomBuyEntryCount = COMMON.randomBuyEntryCount.get();
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfig.ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());
        if(eventConfig.getConfig().getSpec() == ShopConfig.COMMON_SPEC){
            bakeCommonConfig();
//            markDirty(true);
        }
    }

//    public static void markDirty(boolean dirty){
//        isDirty = dirty;
//    }
//    public static boolean isDirty(){
//        return isDirty;
//    }
    
    public static class CommonConfig {

        //This is how to make a config value
        //public static final ForgeConfigSpec.ConfigValue<Integer> exampleInt;
        //public final ForgeConfigSpec.ConfigValue<Integer> timeLeft;
        public final ForgeConfigSpec.ConfigValue<Integer> randomCategoryCount;
        public final ForgeConfigSpec.ConfigValue<Integer> randomBuyEntryCount;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            //This is what goes on top inside of the config
            builder.push("Ars Gears Config");
            //This is how you place a variable in the config file
            //exampleInt = BUILDER.comment("This is an integer. Default value is 3.").define("Example Integer", 54);
            randomCategoryCount = builder.comment("How many categories to choose from (0 will be all categories)").defineInRange("Random_Category_Count", 3, 0,Integer.MAX_VALUE);
            randomBuyEntryCount = builder.comment("How many buy entries to choose (0 will be all entries in every selected category)").defineInRange("Random_Buy_Entry_Count", 12, 0,Integer.MAX_VALUE);

            builder.pop();
        }
    }
}
