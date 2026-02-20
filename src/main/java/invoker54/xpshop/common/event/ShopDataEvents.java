package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.capability.PlayerCapabilityProvider;
import invoker54.xpshop.common.data.BasicData;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.message.ClearShopMsg;
import invoker54.xpshop.common.network.message.ModifyShopDataMsg;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
    public static void grabShopOnLogin(PlayerEvent.PlayerLoggedInEvent event){
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PacketDistributor.PacketTarget target = PacketDistributor.PLAYER.with(()-> player);
        NetworkHandler.INSTANCE.send(target, new ClearShopMsg());
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        CompoundTag poolTag = new CompoundTag();

        for (var data : ShopDataManager.getDataListByType(BasicData.class)) {
            if (friendlyByteBuf.readableBytes() > 524288){
                NetworkHandler.INSTANCE.send(target, new ModifyShopDataMsg(poolTag, false));
                poolTag = new CompoundTag();
            }

            poolTag.put(data.getID().toString(), data.serializeNBT());

            friendlyByteBuf.clear();
            friendlyByteBuf.writeNbt(poolTag);
        }
        NetworkHandler.INSTANCE.send(target, new ModifyShopDataMsg(poolTag, true));
    }
}
