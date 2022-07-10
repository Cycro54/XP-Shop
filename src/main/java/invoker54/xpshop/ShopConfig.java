package invoker54.xpshop;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShopConfig {
//
//        public static final CommonConfig COMMON;
//        public static final ForgeConfigSpec COMMON_SPEC;
//
//        public static Integer timeLeft;
//        public static ArrayList<HashMap<String, Object>> buyList;
//
//        static {
//            final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
//            COMMON_SPEC = specPair.getRight();
//            COMMON = specPair.getLeft();
//        }
//
//        public static void bakeConfig(){
//            timeLeft = COMMON.timeLeft.get();
//            buyList = COMMON.buyList.get();
//        }
//
//        public static class CommonConfig {
//
//            //This is how to make a config value
//            //public static final ForgeConfigSpec.ConfigValue<Integer> exampleInt;
//            public final ForgeConfigSpec.ConfigValue<Integer> timeLeft;
//            public final ForgeConfigSpec.ConfigValue<ArrayList<HashMap<String, Object>>> buyList;
//
//            public CommonConfig(ForgeConfigSpec.Builder builder) {
//                //This is what goes on top inside of the config
//                builder.push("Revive Me! Config");
//                //This is how you place a variable in the config file
//                //exampleInt = BUILDER.comment("This is an integer. Default value is 3.").define("Example Integer", 54);
//                timeLeft = builder.comment("How long you have before death. Default is 30 seconds").define("Time Left", 30);
//                ArrayList<HashMap<String, Object>> buyList1 = new ArrayList<>();
//
//                buyList1.add(new HashMap<String,Object>() {{
//                    put("blah", "now");
//                }});
//                buyList = builder.comment("List of items you can buy").define("Buy Items", buyList1);
//
//                builder.pop();
//            }
//
//        }
//
//        //public Dictionary<String, Object> createBuyEntry
}
