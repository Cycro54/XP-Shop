package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.data.BuyEntry;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UnlockItemMsg {
    CompoundNBT buyEntry;

    public UnlockItemMsg (CompoundNBT buyEntry){
        this.buyEntry = buyEntry;
    }

    public static void encode(UnlockItemMsg msg, PacketBuffer buffer){
        buffer.writeNbt(msg.buyEntry);
    }

    public static UnlockItemMsg decode(PacketBuffer buffer){
        return new UnlockItemMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(UnlockItemMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Grab the buy entry
            BuyEntry entry = new BuyEntry(msg.buyEntry, null);

            //Grab the player
            ServerPlayerEntity player = context.getSender();

            //Take their items.
            while (entry.lockItem.getCount() > 0){
                //Get matching item
                ItemStack itemStack = player.inventory.getItem(player.inventory.findSlotMatchingItem(entry.lockItem));
                //Get how much I can reduce (Can't be any higher than the items count, nor lower than 0)
                int reduce = MathHelper.clamp(entry.lockItem.getCount(),0,itemStack.getCount());
                //First reduce the itemstack in player inventory
                itemStack.shrink(reduce);
                //Then reduce the itemstack in the entry
                entry.lockItem.shrink(reduce);
            }

            //Now make sure the item is now unlocked for this player
            ShopCapability.getShopCap(player).unlockItem(entry.item);
        });
        context.setPacketHandled(true);
    }
    
}
