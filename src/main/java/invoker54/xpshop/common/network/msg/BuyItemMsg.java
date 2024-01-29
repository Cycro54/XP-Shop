package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.event.XPEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BuyItemMsg {
    CompoundNBT buyEntry;

    public BuyItemMsg (CompoundNBT buyEntry){
        this.buyEntry = buyEntry;
    }

    public static void encode(BuyItemMsg msg, PacketBuffer buffer){
        buffer.writeNbt(msg.buyEntry);
    }

    public static BuyItemMsg decode(PacketBuffer buffer){
        return new BuyItemMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(BuyItemMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //Grab the buy entry
            BuyEntry entry = new BuyEntry(msg.buyEntry, null);

            //Grab the player
            ServerPlayerEntity player = context.getSender();

            //Check if it was a limited stock item
            ShopCapability cap = ShopCapability.getShopCap(player);
            if (cap == null) return;
            ShopCapability.Stock stock = cap.grabStock(entry, true);
            if (stock != null){
                stock.reduceStock();
            }

            //Give the player the item (if not enough space, drop on floor)
            if (!player.addItem(entry.item.copy())) player.drop(entry.item.copy(), true);

            //NOW TAKE THEIR XP!!!! TAKE IT ALL, HAHAHAHAHAHAAA!!
            XPEvents.giveExperience(player, -entry.buyPrice);
//            player.giveExperiencePoints(-entry.buyPrice);
        });
        context.setPacketHandled(true);
    }

}
