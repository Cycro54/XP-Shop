package invoker54.xpshop.common.network.message;

import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CompleteShopMsg {
    //This is how the Network Handler will handle the message
    public static void handle(CompleteShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ShopDataManager.setBuildingMode(false);
        });
        context.setPacketHandled(true);
    }
}
