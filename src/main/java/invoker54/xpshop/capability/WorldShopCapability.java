package invoker54.xpshop.capability;

import invoker54.xpshop.config.XPShopConfig;
import invoker54.xpshop.data.CategoryEntry;
import invoker54.xpshop.data.ItemEntry;
import invoker54.xpshop.data.ModLogger;
import invoker54.xpshop.data.ShopEntry;
import invoker54.xpshop.network.NetworkHandler;
import invoker54.xpshop.network.message.AddEntryMsg;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorldShopCapability implements INBTSerializable<CompoundTag> {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShopConfig.debugMode);

    public enum EntryType{
        SHOP,
        CATEGORY,
        ITEM
    }
    public static final Map<String, ArrayList<Integer>> classShopMap = new HashMap<>();
    public static final Map<Integer, ShopEntry> shopEntryMap = new HashMap<>();
    public static final Map<Integer, CategoryEntry> categoryEntryMap = new HashMap<>();
    public static final Map<Integer, ItemEntry> itemEntryMap = new HashMap<>();
    public static final Map<ItemStack, Double> skippedStackMap = new HashMap<>();

    public WorldShopCapability(){
        resetAll();
    }

    public static void resetAll() {
        classShopMap.clear();
        shopEntryMap.clear();
        categoryEntryMap.clear();
        itemEntryMap.clear();
        skippedStackMap.clear();
    }

    public static void unlockShopItems(Player player){
        PlayerShopCapability.getDataCap(player).ifPresent((playerCap) -> {
            for (Integer id : itemEntryMap.keySet()){
                ItemStack entryStack = itemEntryMap.get(id).getLockItem();
                if (entryStack.getCount() <= playerCap.getItemCount(entryStack)){
                    playerCap.unlockShopItem(id);
                }
            }
        });
    }

    public static void syncInitialCapToClient(Player player) {
        try {
            //First do categories
            LOGGER.error("How many categories? " + categoryEntryMap.size());
            syncMapInPiecesToClient(player, EntryType.CATEGORY, categoryEntryMap);

            //Then do Items
            LOGGER.error("How many items? " + itemEntryMap.size());
            syncMapInPiecesToClient(player, EntryType.ITEM, itemEntryMap);

            //Finally do shops
            LOGGER.error("How many shops? " + shopEntryMap.size());
            syncMapInPiecesToClient(player, EntryType.SHOP, shopEntryMap);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            LOGGER.info("Something went wrong with syncing!");
        }

    }
    private static void syncMapInPiecesToClient(Player player, EntryType entryType, Map<Integer,? extends INBTSerializable<CompoundTag>> map){
        LOGGER.warn("What's the entry type? " + entryType.toString());
        int prevSize = 0;
        int count = 0;
        CompoundTag packetTag = new CompoundTag();
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeUtf(entryType.toString());
        for (Integer key : new ArrayList<>(map.keySet())){
            prevSize = buffer.readableBytes();
            CompoundTag tag = map.get(key).serializeNBT();
            buffer.writeNbt(tag);
            count++;
//            LOGGER.info("Packet size is: " + buffer.readableBytes());
            if (buffer.readableBytes() < 524288) {
                packetTag.put("" + key, tag);
            }
            else {
                LOGGER.error("Count: " + count);
                count = 0;
                LOGGER.error("buffer has reached its limit! " + "Prev: " + prevSize + ", Current: " + buffer.readableBytes());
                buffer.clear();
                buffer.writeUtf(entryType.toString());
                buffer.writeNbt(packetTag);
                LOGGER.error("This is the true size of the buffer: " + buffer.readableBytes());
                NetworkHandler.sendToPlayer(player, new AddEntryMsg(entryType.toString(), packetTag));
                packetTag = new CompoundTag();
                packetTag.put("" + key, tag);

                buffer.clear();
                buffer.writeNbt(tag);
                buffer.writeUtf(entryType.toString());
            }
        }

        if (buffer.readableBytes() != 0) {
            LOGGER.error("Final packet! " + buffer.readableBytes());
            NetworkHandler.sendToPlayer(player, new AddEntryMsg(entryType.toString(), packetTag));
        }
    }
    public static void addEntry(Integer index, CompoundTag entryTag, EntryType entryType){
        switch (entryType){
            case SHOP -> {
                var value = shopEntryMap.putIfAbsent(index, new ShopEntry(entryTag));
                if (value != null) shopEntryMap.get(index).deserializeNBT(entryTag);
            }
            case CATEGORY -> {
                var value = categoryEntryMap.putIfAbsent(index, new CategoryEntry(entryTag));
                if (value != null) categoryEntryMap.get(index).deserializeNBT(entryTag);
            }
            case ITEM -> {
                var value = itemEntryMap.putIfAbsent(index, new ItemEntry(entryTag));
                if (value != null) itemEntryMap.get(index).deserializeNBT(entryTag);
            }
        }
    }
    public static void removeEntry(Integer index, EntryType entryType){
        switch (entryType){
            case SHOP -> shopEntryMap.remove(index);
            case CATEGORY -> categoryEntryMap.remove(index);
            case ITEM -> itemEntryMap.remove(index);
        }
    }
    public CompoundTag serializeNBT(){
        LOGGER.debug("SERIALIZING ENTRIES");
        CompoundTag tag = new CompoundTag();

        //First the shopEntryMap
        tag.put("shopEntryMap", convertMapToTag(shopEntryMap));

        //Next the categoryEntryMap
        tag.put("categoryEntryMap", convertMapToTag(categoryEntryMap));

        //Next the itemEntryMap
        tag.put("itemEntryMap", convertMapToTag(itemEntryMap));

        //Finally the skipped items
        CompoundTag skippedStackTag = new CompoundTag();
        int count = 0;
        for (Map.Entry<ItemStack, Double> entry : skippedStackMap.entrySet()){
            CompoundTag entryTag = new CompoundTag();
            entryTag.put("item", entry.getKey().serializeNBT());
            entryTag.putDouble("price", entry.getValue());
            skippedStackTag.put(count + "", entryTag);
            count++;
        }
        tag.put("skippedStackMap", skippedStackTag);
        return tag;
    }

    public static CompoundTag convertMapToTag(Map<Integer,? extends INBTSerializable<CompoundTag>> map){
        CompoundTag tag = new CompoundTag();
        int count = 0;
        for (Integer mapKey : map.keySet()){
//            if (count == 300) break;
//            LOGGER.debug("Key: " + mapKey);
            tag.put("" + mapKey, map.get(mapKey).serializeNBT());
//            LOGGER.error(tag.getCompound(""+mapKey).toString());
            count++;
        }

        return tag;
    }

    public void deserializeNBT(CompoundTag tag){
        LOGGER.debug("DESERIALIZING ENTRIES");
//        LOGGER.info(tag.toString());
        classShopMap.clear();

        CompoundTag shopEntryTag = tag.getCompound("shopEntryMap");
        shopEntryMap.clear();
        for (String key : shopEntryTag.getAllKeys()){
            try {
                shopEntryMap.put(Integer.valueOf(key), new ShopEntry(shopEntryTag.getCompound(key)));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        CompoundTag categoryEntryTag = tag.getCompound("categoryEntryMap");
        categoryEntryMap.clear();
        for (String key : categoryEntryTag.getAllKeys()){
            try {
                categoryEntryMap.put(Integer.valueOf(key), new CategoryEntry(categoryEntryTag.getCompound(key)));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        CompoundTag itemEntryTag = tag.getCompound("itemEntryMap");
//        LOGGER.error(itemEntryTag.toString());
        itemEntryMap.clear();
        for (String key : itemEntryTag.getAllKeys()){
            try {
                itemEntryMap.put(Integer.valueOf(key), new ItemEntry(itemEntryTag.getCompound(key)));
//                LOGGER.info(tag.getCompound(key).toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        //Finally the skipped items
        skippedStackMap.clear();
        CompoundTag skippedStacksTag = tag.getCompound("skippedStackMap");
        for (String key : skippedStacksTag.getAllKeys()){
            try {
                CompoundTag entryTag = skippedStacksTag.getCompound(key);
                skippedStackMap.put(ItemStack.of(entryTag.getCompound("item")), entryTag.getDouble("price"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
