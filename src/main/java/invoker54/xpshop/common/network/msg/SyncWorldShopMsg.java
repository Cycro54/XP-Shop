package invoker54.xpshop.common.network.msg;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.common.api.WorldShopCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncWorldShopMsg {
    public static final Logger LOGGER = LogManager.getLogger();
    private INBT nbtData;

    public SyncWorldShopMsg(INBT nbtData){
        LOGGER.debug("I AM BEING CREATED");
        this.nbtData = nbtData;
    }

    public static void encode(SyncWorldShopMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncWorldShopMsg decode(PacketBuffer buffer){
        return new SyncWorldShopMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncWorldShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            WorldShopCapability cap = WorldShopCapability.getShopCap(ClientUtil.getWorld());
            cap.readNBT((CompoundNBT) msg.nbtData);

            LOGGER.debug(cap.getBuyEntries(ClientUtil.getPlayer()));
        });
        context.setPacketHandled(true);
    }
}
