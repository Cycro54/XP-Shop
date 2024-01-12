package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.BuyEntry;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.function.Supplier;

public class UnlockItemMsg {
    private static final Logger LOGGER = LogManager.getLogger();
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
            if (player == null){
                LOGGER.error("The player is missing?!?! Can't unlock item then.");
                return;
            }

            //Take their items if enabled
            if (ShopConfig.takeLockItem) {
                PlayerInventory inventory = player.inventory;
                NonNullList<ItemStack> allItems = NonNullList.create();
                allItems.addAll(inventory.items);
                allItems.addAll(inventory.offhand);
                allItems.addAll(inventory.armor);

                while (entry.lockItem.getCount() > 0) {
                    ItemStack lockStack = getMatchingItemStack(entry.lockItem, allItems);
                    if (lockStack.isEmpty()){
                        LOGGER.error("Player will unlock the shop item without enough of the lock item requirement...");
                        LOGGER.error("Lock Item: " + entry.lockItem.getDisplayName().getString());
                        LOGGER.error("Count left: " + entry.lockItem.getCount());
                        break;
                    }
                    //Get how much I can reduce (Can't be any higher than the items count, nor lower than 0)
                    int reduce = MathHelper.clamp(entry.lockItem.getCount(), 0, lockStack.getCount());
                    //First reduce the itemstack in player inventory
                    lockStack.shrink(reduce);
                    //Then reduce the itemstack in the entry
                    entry.lockItem.shrink(reduce);
                }
            }

            //Now make sure the item is now unlocked for this player
            ShopCapability.getShopCap(player).unlockItem(entry.item);
        });
        context.setPacketHandled(true);
    }

    public static ItemStack getMatchingItemStack(ItemStack stackToFind, Collection<ItemStack> stackList){
        for (ItemStack stack : stackList){
            boolean sameItem = stackToFind.sameItem(stack);
            boolean matchingTags = ItemStack.tagMatches(stackToFind, stack);
            if (sameItem && matchingTags) return stack;
        }
        return ItemStack.EMPTY;
    }
    
}
