package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.screen.SellContainer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClearSellContainerMsg {
    //This is how the Network Handler will handle the message
    public static void handle(ClearSellContainerMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Clear the container
            ((SellContainer)context.getSender().containerMenu).tempInv.clearContent();
        });
        context.setPacketHandled(true);
    }
}
