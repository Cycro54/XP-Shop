package invoker54.xpshop.network.message;

import invoker54.xpshop.capability.WorldShopCapability;
import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddEntryMsg {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    public CompoundTag entryTag;
    public String entryType;

    public AddEntryMsg(String entryType, CompoundTag entryTag){
        this.entryType = entryType;
        this.entryTag = entryTag;
    }

    public static void encode(AddEntryMsg msg, FriendlyByteBuf buffer){
        LOGGER.error("What's the buffer size before my data? " + buffer.readableBytes());
        buffer.writeUtf(msg.entryType);
        LOGGER.error("Size after entry type? " + buffer.readableBytes());
        buffer.writeNbt(msg.entryTag);
        LOGGER.error("final size: " + buffer.readableBytes());
    }

    public static AddEntryMsg decode(FriendlyByteBuf buf){
        return new AddEntryMsg(buf.readUtf(), buf.readAnySizeNbt());
    }

    public static void handle(AddEntryMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            CompoundTag entryTag = msg.entryTag;

            for (String key : msg.entryTag.getAllKeys()) {
                try {
                    WorldShopCapability.addEntry(Integer.valueOf(key), entryTag.getCompound(key), WorldShopCapability.EntryType.valueOf(msg.entryType));
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }

            if (context.getSender() != null){
                NetworkHandler.sendToAllPlayers(msg);
            }
        });

        context.setPacketHandled(true);
    }
}
