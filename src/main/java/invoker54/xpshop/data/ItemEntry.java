package invoker54.xpshop.data;

import invoker54.xpshop.config.XPShopConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

public class ItemEntry implements INBTSerializable<CompoundTag> {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);
    private int itemId;
    private int categoryId;
    private ItemStack shopItem;
    private ItemStack lockItem;
    private boolean hiddenIfLocked;
    private int stockAmount;
    private PriceList priceList;

    public ItemEntry(int itemId, int categoryId, ItemStack shopItem, ItemStack lockItem, boolean hiddenIfLocked, int stockAmount, PriceList priceList){
        this.itemId = itemId;
        this.categoryId = categoryId;
        this.shopItem = shopItem;
        this.lockItem = lockItem;
        this.hiddenIfLocked = hiddenIfLocked;
        this.stockAmount = stockAmount;
        this.priceList = priceList;
    }

    public ItemEntry(CompoundTag tag){
        this.deserializeNBT(tag);
    }

    public int getItemId(){return this.itemId;}
    public int getCategoryId(){return this.categoryId;}
    public ItemStack getShopItem(){return this.shopItem.copy();}
    public ItemStack getLockItem(){return this.lockItem.copy();}
    public boolean isHiddenIfLocked(){return this.hiddenIfLocked;}
    public int getStockAmount(){return this.stockAmount;}
    public double getPrice(boolean applyJitter){return this.priceList.getFullPrice(applyJitter);}

    public PriceList getPriceList(){
        return this.priceList;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag itemEntryTag = new CompoundTag();
        itemEntryTag.putInt("itemId", this.itemId);
        itemEntryTag.putInt("categoryId", this.categoryId);
        itemEntryTag.put("shopItem", this.shopItem.serializeNBT());
        itemEntryTag.put("lockItem", this.lockItem.serializeNBT());
        itemEntryTag.putBoolean("hiddenIfLocked", this.hiddenIfLocked);
        itemEntryTag.putInt("stockAmount", this.stockAmount);
        itemEntryTag.put("price", this.priceList.serializeNBT());

        return itemEntryTag;
    }

    @Override
    public void deserializeNBT(CompoundTag itemEntryTag) {
        this.itemId = itemEntryTag.getInt("itemId");
        this.categoryId = itemEntryTag.getInt("categoryId");
        this.shopItem = ItemStack.of(itemEntryTag.getCompound("shopItem"));
//        if (!this.shopItem.isEmpty()) LOGGER.error("What's the item?? " + this.shopItem.getDisplayName().getString());
        this.lockItem = ItemStack.of(itemEntryTag.getCompound("lockItem"));
        this.hiddenIfLocked = itemEntryTag.getBoolean("hiddenIfLocked");
        this.stockAmount = itemEntryTag.getInt("stockAmount");
        this.priceList = new PriceList(itemEntryTag.getCompound("price"));
    }
}
