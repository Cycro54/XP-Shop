package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.event.RefreshDealsEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class ForceRefreshMsg {

    //This is how the Network Handler will handle the message
    public static void handle(ForceRefreshMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            for (ServerWorld world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                WorldShopCapability.getShopCap(world).refreshDeals();
            }
        });
        context.setPacketHandled(true);
    }

}
