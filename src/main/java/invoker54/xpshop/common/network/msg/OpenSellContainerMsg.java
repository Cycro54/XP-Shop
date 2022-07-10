package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.screen.SellContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenSellContainerMsg {
    //This is how the Network Handler will handle the message
    public static void handle(OpenSellContainerMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Open the Sell Container
            context.getSender().openMenu(new SimpleNamedContainerProvider((id, playerInv, player) -> {
                return new SellContainer(id, playerInv);
            }, new StringTextComponent("Items To Sell")));
        });
        context.setPacketHandled(true);
    }
}
