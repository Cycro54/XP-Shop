package invoker54.xpshop.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.network.NetworkHandler;
import invoker54.xpshop.network.message.SyncConfigMsg;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class SyncConfigEvent {

    @SubscribeEvent
    public static void onUpdateConfig(TickEvent.ServerTickEvent event){
        if (event.type == TickEvent.Type.CLIENT) return;
        if (event.phase == TickEvent.Phase.START) return;
        if (XPShopConfig.isDirty()){
            //Then finally send the config data to all players
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigMsg(XPShopConfig.serialize()));

            XPShopConfig.markDirty(false);
        }
    }
}
