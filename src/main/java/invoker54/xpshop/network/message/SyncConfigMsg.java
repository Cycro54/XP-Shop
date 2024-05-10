package invoker54.xpshop.network.message;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncConfigMsg {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    public CompoundTag configTag;


    public SyncConfigMsg(CompoundTag configTag){
        this.configTag = configTag;
    }

    public static void encode(SyncConfigMsg msg, FriendlyByteBuf buf){
        buf.writeNbt(msg.configTag);
    }

    public static SyncConfigMsg decode(FriendlyByteBuf buf){
        LOGGER.error("How large is the config msg in Bytes? " + buf.readableBytes());
        return new SyncConfigMsg(buf.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncConfigMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            XPShopConfig.deserialize(msg.configTag);
        });
        context.setPacketHandled(true);
    }
}
