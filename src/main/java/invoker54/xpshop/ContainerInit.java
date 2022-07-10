package invoker54.xpshop;

import invoker54.xpshop.client.screen.SellContainer;
import invoker54.xpshop.client.screen.SellContainerScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerInit {
    public static ContainerType<SellContainer> sellContainerType = null;

    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event){
        sellContainerType = IForgeContainerType.create(SellContainer::createContainer);
        sellContainerType.setRegistryName("xpshop_sell_container");

        event.getRegistry().register(sellContainerType);
        ScreenManager.register(sellContainerType, SellContainerScreen::new);
    }
}
