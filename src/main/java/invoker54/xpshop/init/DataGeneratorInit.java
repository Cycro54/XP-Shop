package invoker54.xpshop.init;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneratorInit {
    private static final ModLogger LOGGER = ModLogger.getLogger(DataGeneratorInit.class, XPShop.debugMode);

    @SubscribeEvent
    public static void gatherTranslations(GatherDataEvent event){
        event.getGenerator().addProvider(event.includeClient(),
                (DataProvider.Factory<XPShopLanguageprovider>)
                        output -> new XPShopLanguageprovider(output, XPShop.MOD_ID, "en_us"));
    }
}
