package invoker54.xpshop;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.init.ItemInit;
import invoker54.xpshop.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(XPShop.MOD_ID)
public class XPShop {
    public static IEventBus bus;
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "xp_shop";
//    // Create a Deferred Register to hold Blocks which will all be registered under the "test_mod" namespace
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);

    public static CreativeModeTab itemGroup = new CreativeModeTab(CreativeModeTab.getGroupCountSafe(), MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return ItemInit.XP_TRADER.get().getDefaultInstance();
        }
    };

    // Directly reference a slf4j logger
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public XPShop() {
        bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        bus.addListener(this::otherSetup);

        // Register the Deferred Register to the mod event bus so items get registered
        ItemInit.ITEMS.register(bus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //This is for configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, XPShopConfig.COMMON_SPEC, "xp-shop/xp-shop-common.toml");
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.init();
    }

    private void otherSetup(final FMLLoadCompleteEvent event){
        Testing.test();
    }


}