package invoker54.xpshop;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.init.ShopDataInit;
import invoker54.xpshop.init.ShopScreenInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.concurrent.atomic.AtomicBoolean;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(XPShop.MOD_ID)
public class XPShop {
    public static final String MOD_ID = "xp_shop";
    public static IEventBus bus;
    public static final AtomicBoolean debugMode = new AtomicBoolean(true);

    // Directly reference a log4j logger.
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShop.class, debugMode);

    public XPShop() {
        bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
//        bus.addListener(this::setup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        bus.addListener(this::clientSetup);
        //This is for configs
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ReviveMeConfig.COMMON_SPEC, "reviveme-common.toml");
        ShopDataInit.init();
    }

    private void clientSetup(FMLClientSetupEvent event){
        ShopScreenInit.init();
    }

//    private void setup(final FMLCommonSetupEvent event)
//    {
//        AbstractContainer.typeMap.forEach((s, container) -> LOGGER.debug(container.getType()));
//    }
}
