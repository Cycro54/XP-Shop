package invoker54.xpshop.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.capability.PlayerShopCapability;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class ItemCountEvents {

    @SubscribeEvent
    public static void onOpenContainer(PlayerContainerEvent.Open event){
        if (event.isCanceled()) return;

        PlayerShopCapability.getDataCap(event.getEntity())
                .ifPresent(PlayerShopCapability::countInventoryItems);
    }

    @SubscribeEvent
    public static void onCloseContainer(PlayerContainerEvent.Close event){
        if (event.isCanceled()) return;

        PlayerShopCapability.getDataCap(event.getEntity())
                .ifPresent(PlayerShopCapability::countInventoryItems);
    }

    @SubscribeEvent
    public static void onDropItem(PlayerEvent.ItemPickupEvent event){
        if (event.isCanceled()) return;

        PlayerShopCapability.getDataCap(event.getEntity())
                .ifPresent(PlayerShopCapability::countInventoryItems);
    }
}
