package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.ShopCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncServerCapMsg {

    private INBT nbtData;

    public SyncServerCapMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncServerCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncServerCapMsg decode(PacketBuffer buffer){
        return new SyncServerCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncServerCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Give shop cap to player
            ShopCapability.getShopCap(context.getSender()).readNBT((CompoundNBT) msg.nbtData);
        });
        context.setPacketHandled(true);
    }
}
