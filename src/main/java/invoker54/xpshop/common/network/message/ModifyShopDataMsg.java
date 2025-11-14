package invoker54.xpshop.common.network.message;

import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ModifyShopDataMsg {
    public final UUID id;
    public final CompoundTag dataTag;

    public ModifyShopDataMsg(UUID id, CompoundTag dataTag){
        this.id = id;
        this.dataTag = dataTag;
    }

    public static void Encode(ModifyShopDataMsg msg, FriendlyByteBuf buffer){
        buffer.writeUUID(msg.id);
        buffer.writeNbt(msg.dataTag);
    }

    public static ModifyShopDataMsg Decode(FriendlyByteBuf buffer) {
        return new ModifyShopDataMsg(buffer.readUUID(), buffer.readNbt());}

    //This is how the Network Handler will handle the message
    public static void handle(ModifyShopDataMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (msg.dataTag == null){
                ShopDataManager.removeData(msg.id);
            }
            else {
                ShopDataManager.addData(msg.id, msg.dataTag);
            }
        });
        context.setPacketHandled(true);
    }
}
