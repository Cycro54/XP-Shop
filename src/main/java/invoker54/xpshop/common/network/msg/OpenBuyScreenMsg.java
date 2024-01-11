package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.ExtraUtil;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenBuyScreenMsg {

    //This is how the Network Handler will handle the message
    public static void handle(OpenBuyScreenMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Open the buy screen
            ExtraUtil.openShopFee(true);
        });
        context.setPacketHandled(true);
    }
}
