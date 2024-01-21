package invoker54.xpshop.client.screen;

import invoker54.xpshop.ContainerInit;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.data.ShopData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static invoker54.xpshop.common.data.ShopData.getMatchingStack;

public class SellContainer extends Container {
    private static final Logger LOGGER = LogManager.getLogger();

    public Inventory tempInv;

    //region player inventory variables
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    //endregion

    //region Sell inventory variables
    private static final int SELL_INVENTORY_ROW_COUNT = 4;
    private static final int SELL_INVENTORY_COLUMN_COUNT = 9;
    private static final int SELL_INVENTORY_TOTAL_COUNT = SELL_INVENTORY_COLUMN_COUNT * SELL_INVENTORY_ROW_COUNT;
    //endregion

    public float totalExtraXP;
    public boolean clickedWanderer;

    public static SellContainer createContainer(int containerID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        // on the client side there is no parent TileEntity to communicate with, so we:
        // 1) use a dummy inventory
        // 2) use "do nothing" lambda functions for canPlayerAccessInventory and markDirty
        return new SellContainer(containerID, playerInventory, extraData.readBoolean());
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }

    /**
     * Creates a container suitable for server side or client side
     * @param containerID ID of the container
     * @param playerInventory the inventory of the player
     */
    public SellContainer(int containerID, PlayerInventory playerInventory, boolean clickedWanderer) {
        super(ContainerInit.sellContainerType, containerID);
        this.clickedWanderer = clickedWanderer;
//        LOGGER.error("WHAT IS CLICKED WANDERER? : " + (clickedWanderer));
        tempInv = new Inventory(SELL_INVENTORY_TOTAL_COUNT);
        tempInv.addListener((container) -> totalExtraXP = CalculateXP(playerInventory.player));
        if (ContainerInit.sellContainerType == null)
            throw new IllegalStateException("Must initialise containerBasicContainerType before constructing a ContainerBasic!");

        //region First the player inventory
        PlayerInvWrapper playerInventoryForge = new PlayerInvWrapper(playerInventory);  // wrap the IInventory in a Forge IItemHandler.
        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;
        final int HOTBAR_XPOS = 8;
        final int HOTBAR_YPOS = 198;
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            // Not actually necessary - can use Slot(playerInventory) instead of SlotItemHandler(playerInventoryForge)
            addSlot(new SlotItemHandler(playerInventoryForge, slotNumber, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }

        final int PLAYER_INVENTORY_XPOS = 8;
        final int PLAYER_INVENTORY_YPOS = 140;
        // Add the rest of the player's inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new SlotItemHandler(playerInventoryForge, slotNumber,  xpos, ypos));
            }
        }
        //endregion

        //region Next the Sell Inventory
        final int SELL_INVENTORY_XPOS = 8;
        final int SELL_INVENTORY_YPOS = 18;
        // Add the rest of the player's inventory to the gui
        for (int y = 0; y < SELL_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < SELL_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = x + (y * SELL_INVENTORY_COLUMN_COUNT);
                int xpos = SELL_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = SELL_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(tempInv, slotNumber,  xpos, ypos));
            }
        }
        //endregion
    }

    // Vanilla calls this method every tick to make sure the player is still able to access the inventory, and if not closes the gui
    // Called on the SERVER side only

    // This is where you specify what happens when a player shift clicks a slot in the gui
    //  (when you shift click a slot in the TileEntity Inventory, it moves it to the first available position in the hotbar and/or
    //    player inventory.  When you you shift-click a hotbar or player inventory item, it moves it to the first available
    //    position in the TileEntity inventory)
    // At the very least you must override this and return ItemStack.EMPTY or the game will crash when the player shift clicks a slot
    // returns ItemStack.EMPTY if the source slot is empty, or if none of the the source slot item could be moved
    //   otherwise, returns a copy of the source stack
    @Nonnull
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex < VANILLA_SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, VANILLA_SLOT_COUNT, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        this.clearContainer(player, player.level, tempInv);
    }

    public float CalculateXP(PlayerEntity player){
        float totalXP = 0;
        ArrayList<ItemStack> slotItems = new ArrayList<>();

        for (Slot slot : slots){
            if (slot.container.equals(tempInv)){
                if (!slot.hasItem()) continue;

                if (getMatchingStack(slot.getItem(), slotItems) == null)
                    slotItems.add(slot.getItem());
            }
        }

        for (ItemStack itemStack : slotItems){
            itemStack = getMatchingStack(itemStack, ShopData.sellEntries.keySet());
            if (itemStack != null)
                totalXP += (ShopData.sellEntries.get(itemStack).calcPrice(tempInv.countItem(itemStack.getItem())));

        }
        totalXP = BigDecimal.valueOf(totalXP).setScale(2, RoundingMode.HALF_UP).floatValue();
        /* TODO: Place this in the mod */
        ShopCapability playerCap = ShopCapability.getShopCap(player);
        if (playerCap == null) return 0;
        totalXP = Math.max(
                Math.min(playerCap.getPlayerTier().getMax() - player.totalExperience, totalXP), 0);
        return totalXP;
    }

    //    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotIndex)
//    {
//        Slot sourceSlot = inventorySlots.get(sourceSlotIndex);
//        if (sourceSlot == null || !sourceSlot.getHasStack()) return ItemStack.EMPTY;  //EMPTY_ITEM
//        ItemStack sourceStack = sourceSlot.getStack();
//        ItemStack copyOfSourceStack = sourceStack.copy();
//
//        // Check if the slot clicked is one of the vanilla container slots
//        if (sourceSlotIndex >= VANILLA_FIRST_SLOT_INDEX && sourceSlotIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
//            // This is a vanilla container slot so merge the stack into the tile inventory
//            if (!mergeItemStack(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)){
//                return ItemStack.EMPTY;  // EMPTY_ITEM
//            }
//        } else if (sourceSlotIndex >= TE_INVENTORY_FIRST_SLOT_INDEX && sourceSlotIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
//            // This is a TE slot so merge the stack into the players inventory
//            if (!mergeItemStack(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
//                return ItemStack.EMPTY;
//            }
//        } else {
//            LOGGER.warn("Invalid slotIndex:" + sourceSlotIndex);
//            return ItemStack.EMPTY;
//        }
//
//        // If stack size == 0 (the entire stack was moved) set slot contents to null
//        if (sourceStack.getCount() == 0) {
//            sourceSlot.putStack(ItemStack.EMPTY);
//        } else {
//            sourceSlot.onSlotChanged();
//        }
//
//        sourceSlot.onTake(playerEntity, sourceStack);
//        return copyOfSourceStack;
//    }
}
