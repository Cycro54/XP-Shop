package invoker54.xpshop.common.network.message;

import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class ModifyShopDataMsg {
    public final CompoundTag dataTag;
    public final Boolean isComplete;

    public ModifyShopDataMsg(CompoundTag dataTag, boolean isComplete){
        this.dataTag = dataTag;
        this.isComplete = isComplete;
    }

    public static void Encode(ModifyShopDataMsg msg, FriendlyByteBuf buffer){
        buffer.writeNbt(msg.dataTag);
        buffer.writeBoolean(msg.isComplete);
    }

    public static ModifyShopDataMsg Decode(FriendlyByteBuf buffer) {
        return new ModifyShopDataMsg(buffer.readNbt(), buffer.readBoolean());}

    //This is how the Network Handler will handle the message
    public static void handle(ModifyShopDataMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            for (String stringUUID : msg.dataTag.getAllKeys()){
                if (msg.dataTag.getCompound(stringUUID).isEmpty()){
                    ShopDataManager.removeData(UUID.fromString(stringUUID));
                }
                else {
                    ShopDataManager.addData(UUID.fromString(stringUUID), msg.dataTag.getCompound(stringUUID));
                }
            }
            ShopDataManager.setBuildingMode(msg.isComplete);

            if (context.getDirection().getOriginationSide().isClient()){
                NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new ModifyShopDataMsg(msg.dataTag, msg.isComplete));
            }
        });
        context.setPacketHandled(true);
    }
}
