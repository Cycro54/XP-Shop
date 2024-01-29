package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.WorldShopCapability;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class ForceRefreshMsg {

    //This is how the Network Handler will handle the message
    public static void handle(ForceRefreshMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            WorldShopCapability.getShopCap(ServerLifecycleHooks.getCurrentServer().overworld()).refreshDeals();
        });
        context.setPacketHandled(true);
    }

}
