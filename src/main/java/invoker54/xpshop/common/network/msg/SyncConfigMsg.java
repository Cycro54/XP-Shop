package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.config.ShopConfig;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncConfigMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    private CompoundNBT mainNBT;
    public SyncConfigMsg(CompoundNBT mainNBT){
        this.mainNBT = mainNBT;
    }

    public static void encode(SyncConfigMsg msg, PacketBuffer buffer){
        buffer.writeNbt(msg.mainNBT);
    }

    public static SyncConfigMsg decode(PacketBuffer buffer){
        return new SyncConfigMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncConfigMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ShopConfig.deserialize(msg.mainNBT);
        });
        context.setPacketHandled(true);
    }

}
