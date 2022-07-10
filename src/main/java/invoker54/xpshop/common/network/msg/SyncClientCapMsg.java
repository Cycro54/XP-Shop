package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.common.api.ShopCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncClientCapMsg {

    private INBT nbtData;

    public SyncClientCapMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncClientCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncClientCapMsg decode(PacketBuffer buffer){
        return new SyncClientCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Give shop cap to player
            ShopCapability.getShopCap(ClientUtil.mC.player).readNBT((CompoundNBT) msg.nbtData);
        });
        context.setPacketHandled(true);
    }
}
