package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.screen.SellContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenSellContainerMsg {
    boolean clickedWanderer;

    public OpenSellContainerMsg (boolean clickedWanderer){
        this.clickedWanderer = clickedWanderer;
    }

    public static void encode(OpenSellContainerMsg msg, PacketBuffer buffer){
        buffer.writeBoolean(msg.clickedWanderer);
    }

    public static OpenSellContainerMsg decode(PacketBuffer buffer){
        return new OpenSellContainerMsg(buffer.readBoolean());
    }

    //This is how the Network Handler will handle the message
    public static void handle(OpenSellContainerMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity senderPlayer = context.getSender();
            if (senderPlayer == null) return;

            //Open the Sell Container
            NetworkHooks.openGui(senderPlayer, new SimpleNamedContainerProvider((id, playerInv, player) -> {
                return new SellContainer(id, senderPlayer.inventory, msg.clickedWanderer);
            }, new StringTextComponent("Items To Sell")), (buffer -> buffer.writeBoolean(msg.clickedWanderer)));
        });
        context.setPacketHandled(true);
    }
}
