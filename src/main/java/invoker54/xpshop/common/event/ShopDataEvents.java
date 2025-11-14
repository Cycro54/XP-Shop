package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.capability.PlayerCapabilityProvider;
import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.message.BuildShopMsg;
import invoker54.xpshop.common.network.message.CompleteShopMsg;
import invoker54.xpshop.common.network.message.ModifyShopDataMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class ShopDataEvents {

    @SubscribeEvent
    public static void attachPlayerCap(AttachCapabilitiesEvent<Entity> event){
        if (!(event.getObject() instanceof Player)) return;
        event.addCapability(PlayerCapabilityProvider.STORAGE_LOCATION, new PlayerCapabilityProvider());
    }

    @SubscribeEvent
    public static void grabShopOnLogin(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(()-> player);
        NetworkHandler.INSTANCE.send(target, new BuildShopMsg());
        for (var data : ShopDataManager.getDataListByType(BasicData.class)) {
            NetworkHandler.INSTANCE.send(target, new ModifyShopDataMsg(data.getID(), data.serializeNBT()));
        }
        NetworkHandler.INSTANCE.send(target, new CompleteShopMsg());

    }
}
