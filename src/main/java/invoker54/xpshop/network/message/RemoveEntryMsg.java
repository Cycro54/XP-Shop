package invoker54.xpshop.network.message;

import invoker54.xpshop.capability.WorldShopCapability;
import invoker54.xpshop.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveEntryMsg {
    public CompoundTag numberTag;
    public String entryType;

    public RemoveEntryMsg(CompoundTag numberTag, String entryType){
        this.numberTag = numberTag;
        this.entryType = entryType;
    }

    public static void encode(RemoveEntryMsg msg, FriendlyByteBuf buffer){
        buffer.writeNbt(msg.numberTag);
        buffer.writeUtf(msg.entryType);
    }

    public static RemoveEntryMsg decode(FriendlyByteBuf buf){
        return new RemoveEntryMsg(buf.readAnySizeNbt(), buf.readUtf());
    }

    public static void handle(RemoveEntryMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            int[] array = msg.numberTag.getIntArray("entries");

            for (Integer index : array){
                WorldShopCapability.removeEntry(index, WorldShopCapability.EntryType.valueOf(msg.entryType));
            }

            if (context.getSender() != null){
                NetworkHandler.sendToAllPlayers(msg);
            }
        });

        context.setPacketHandled(true);
    }
}
