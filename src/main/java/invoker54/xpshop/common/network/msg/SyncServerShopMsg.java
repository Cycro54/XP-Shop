package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncServerShopMsg {

    private INBT nbtData;

    public SyncServerShopMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncServerShopMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncServerShopMsg decode(PacketBuffer buffer){
        return new SyncServerShopMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncServerShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            CompoundNBT mainNBT = (CompoundNBT) msg.nbtData;

            ShopData.deserialize(mainNBT);

            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncClientShopMsg(mainNBT));
        });
        context.setPacketHandled(true);
    }
}
