package invoker54.xpshop.data;

import invoker54.xpshop.capability.WorldShopCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class ShopEntry implements INBTSerializable<CompoundTag> {
    //The shops identification number for code
    private int shopId;
    private final List<String> owners = new ArrayList<>();
    //The items in the shop
    private final List<Integer> itemIds = new ArrayList<>();
    //pools for a randomly chosen item
    private final List<ItemPool> itemPools = new ArrayList<>();
    //When the shop opens in ticks
    private int openTime;
    //When the shop closes in ticks (this number must always be greater than open time.
    private int closeTime;
    //When the shop does a stock/item refresh. This is only useful if the store is open all day
    private int refreshTime;

    public int getShopId(){
        return this.shopId;
    }
    public int getOpenTime(){ return this.openTime;}
    public int getCloseTime(){ return this.closeTime;}
    public int getRefreshTime(){ return this.refreshTime;}
    public ArrayList<Integer> getMainItemList(){
        return new ArrayList<>(this.itemIds);
    }
    public ArrayList<Integer> getFullItemList(){
        ArrayList<Integer> allItemIds = this.getMainItemList();

        for (ItemPool itemPool : this.itemPools){
            allItemIds.addAll(itemPool.getSelectedItemIds());
        }

        Collections.shuffle(allItemIds);

        return allItemIds;
    }
    public void refreshPoolItems(){
        if (itemPools.isEmpty()) return;

        ArrayList<Integer> mainItemIdList = this.getMainItemList();
        for (ItemPool itemPool : this.itemPools){
            itemPool.refreshSelection(mainItemIdList);
        }
    }

    public ShopEntry(CompoundTag tag){
        this.deserializeNBT(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag shopEntryTag = new CompoundTag();

        shopEntryTag.putInt("shopId", this.shopId);
        shopEntryTag.putIntArray("items", this.itemIds);
        shopEntryTag.putString("owners", String.join(",", owners));

        CompoundTag poolTag = new CompoundTag();
        for (ItemPool itemPool : itemPools){
            poolTag.put("" + poolTag.size(), itemPool.serialize());
        }
        shopEntryTag.put("itemPools", poolTag);

        shopEntryTag.putInt("openTime", this.openTime);
        shopEntryTag.putInt("closeTime", this.closeTime);
        shopEntryTag.putInt("refreshTime", this.refreshTime);

        return shopEntryTag;
    }

    @Override
    public void deserializeNBT(CompoundTag shopEntryTag) {
        this.shopId = shopEntryTag.getInt("shopId");

        this.itemIds.clear();
        this.itemIds.addAll(Arrays.stream(shopEntryTag.getIntArray("items")).boxed().toList());

        this.owners.clear();
        this.owners.addAll(Arrays.stream(shopEntryTag.getString("owners").split(",")).toList());

        this.itemPools.clear();
        CompoundTag itemPoolTag = shopEntryTag.getCompound("itemPools");
        for (int a = 0; a < itemPoolTag.size(); a++) {
            this.itemPools.add(new ItemPool(itemPoolTag.getCompound("" + a)));
        }

        this.openTime = shopEntryTag.getInt("openTime");
        this.closeTime = shopEntryTag.getInt("closeTime");
        this.refreshTime = shopEntryTag.getInt("refreshTime");
    }

    public static class ItemPool {
        private int selectionLimit;
        private final List<Integer> selectedItemIds = new ArrayList<Integer>();
        private final List<Integer> allItemIds = new ArrayList<Integer>();
        private boolean pickSimilarItems = true;
        public void editItemPool(List<Integer> newList){
            this.allItemIds.clear();
            this.allItemIds.addAll(newList);
        }
        public List<Integer> getSelectedItemIds(){return new ArrayList<>(selectedItemIds);}
        public void refreshSelection(List<Integer> excludedItemIds){
            ArrayList<Class<?>> itemTypes = new ArrayList<>();
            this.selectedItemIds.clear();

            ArrayList<Integer> itemPoolCopy = new ArrayList<>(this.allItemIds);
            Collections.shuffle(itemPoolCopy, new Random());

            itemPoolCopy.removeIf(excludedItemIds::contains);

            //This will run if we wish to avoid picking similar items
            if (!this.pickSimilarItems) {
                for (Integer id : itemPoolCopy) {
                    Item item = WorldShopCapability.itemEntryMap.get(id).getShopItem().getItem();

                    if (itemTypes.contains(item.getClass())) continue;
                    itemTypes.add(item.getClass());

                    this.selectedItemIds.add(id);

                    if (this.selectedItemIds.size() < this.selectionLimit) return;
                }
            }

            //This will run if we don't care if the items are similar
            else {
                for (Integer id : itemPoolCopy) {
                    this.selectedItemIds.add(id);
                    if (this.selectedItemIds.size() < this.selectionLimit) return;
                }
            }
        }

        public ItemPool(CompoundTag tag){
            this.deserialize(tag);
        }

        public CompoundTag serialize(){
            CompoundTag itemPoolTag = new CompoundTag();
            itemPoolTag.putInt("selectionLimit", this.selectionLimit);
            itemPoolTag.putIntArray("selectedItemIds", this.selectedItemIds);
            itemPoolTag.putIntArray("allItemIds", this.allItemIds);
            itemPoolTag.putBoolean("pickSimilarItems", this.pickSimilarItems);

            return itemPoolTag;
        }

        public void deserialize(CompoundTag itemPoolTag){
            this.selectionLimit = itemPoolTag.getInt("selectionLimit");

            this.selectedItemIds.clear();
            this.selectedItemIds.addAll(Arrays.stream(itemPoolTag.getIntArray("selectedItemIds")).boxed().toList());

            this.allItemIds.clear();
            this.allItemIds.addAll(Arrays.stream(itemPoolTag.getIntArray("allItemIds")).boxed().toList());

            this.pickSimilarItems = itemPoolTag.getBoolean("pickSimilarItems");
        }
    }
}