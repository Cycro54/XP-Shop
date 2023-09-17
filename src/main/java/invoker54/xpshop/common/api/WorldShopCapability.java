package invoker54.xpshop.common.api;

import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.data.CategoryEntry;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import invoker54.xpshop.common.network.msg.SyncClientCapMsg;
import invoker54.xpshop.common.network.msg.SyncWorldShopMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public class WorldShopCapability {
    public static final Logger LOGGER = LogManager.getLogger();
    protected final String ENTRY_SIZE = "ENTRY_SIZE";
    protected World level;
    protected ArrayList<BuyEntry> buyEntries;
    public WorldShopCapability(){}
    public WorldShopCapability(World level) {
        this.level = level;
        buyEntries = new ArrayList<>();

        //Don't do calculations on the client
        if (level.isClientSide) return;
        LOGGER.error("SETTING UP BUY ENTRIES FOR TRADER");
        refreshDeals();
    }

    public static WorldShopCapability getShopCap(World level){
        return level.getCapability(WorldShopProvider.XPSHOPDATA).orElseThrow(NullPointerException::new);
    }

    public void refreshDeals(){
        //Grab all the categories then remove them until we reach our max category count
        ArrayList<CategoryEntry> categoryEntries = new ArrayList<>(ShopData.catEntries);
        while (categoryEntries.size() > ShopConfig.randomCategoryCount && ShopConfig.randomCategoryCount != 0){
            categoryEntries.remove(level.random.nextInt(categoryEntries.size()));
        }
        //Next grab all the items in those categories
        for (CategoryEntry catEntry : categoryEntries){
            buyEntries.addAll(catEntry.entries);
        }
        //Remove the ones that will always show
        buyEntries.removeIf((entry) -> entry.alwaysShow);
        //Shuffle them
        Collections.shuffle(buyEntries, level.random);
        //remove them until we reach our max buy entry count
        while (buyEntries.size() > ShopConfig.randomBuyEntryCount && ShopConfig.randomBuyEntryCount != 0){
            buyEntries.remove(level.random.nextInt(buyEntries.size()));
        }
        LOGGER.info("THESE ARE THE Buy Entries for this worlds Enchanted books");
        for (BuyEntry entry : this.buyEntries){
            if (entry.item.getItem() instanceof EnchantedBookItem){
                LOGGER.debug(EnchantedBookItem.getEnchantments(entry.item));
            }
        }

        //Update the trader xp
        if (this.level.dimension() == World.OVERWORLD) {
            for (PlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                ShopCapability playerCap = ShopCapability.getShopCap(player);
                if (playerCap == null) return;
                playerCap.refreshTradeXP();
                //This will place all items back in stock
                playerCap.refreshStock();
                NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(playerCap.writeNBT()));
            }
        }

        //Now send to all players within this dimension.
        NetworkHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> this.level.dimension()), new SyncWorldShopMsg(this.writeNBT()));
    }

    public ArrayList<BuyEntry> getBuyEntries(PlayerEntity player){
        ArrayList<BuyEntry> buyList = new ArrayList<>(this.buyEntries);
        ShopCapability playerCap = ShopCapability.getShopCap(player);
        if (playerCap == null) return new ArrayList<>();

        //Only show half the shop if they don't have the more options upgrade
        if (!playerCap.optionUpgrade && buyList.size() >= 4){
            int halfSize = Math.round(buyList.size()/2F);
            LOGGER.debug("HALF SIZE IS: " + (buyList.size()/2F));
            LOGGER.debug("ROUNDED IS: " + Math.round(buyList.size()/2F));
            buyList = new ArrayList<>(buyList.subList(0,halfSize));
//            for (BuyEntry entry : buyList){
//                LOGGER.info("HERES THE NEW LIST");
//                LOGGER.debug(entry.item.getDisplayName().getString());
//            }
        }

        //Readd the ones that will always show
        for (BuyEntry entry: ShopData.buyEntries){
            if (entry.alwaysShow) buyList.add(entry);
        }

        return buyList;
    }

    public CompoundNBT writeNBT(){
        CompoundNBT mainNBT = new CompoundNBT();

        //Save each buy entry item so we can look for it later
        for (int a = 0; a < buyEntries.size(); a++){
            mainNBT.put(""+a, buyEntries.get(a).item.serializeNBT());
        }

        //Finally save the size
        mainNBT.putInt(ENTRY_SIZE, buyEntries.size());

        return mainNBT;
    }

    public void readNBT (CompoundNBT mainNBT){
        buyEntries.clear();
        ArrayList<ItemStack> savedItems = new ArrayList<>();
        int count = mainNBT.getInt(ENTRY_SIZE);

        //Grab the saved items
        for (int a = 0; a < count; a++){
//            LOGGER.debug("" + a);
            savedItems.add(ItemStack.of((CompoundNBT) mainNBT.get(""+a)));
        }

        //Next look for those items in the buyEntries list
        for (ItemStack savedItem : savedItems){
            for (BuyEntry entry : ShopData.buyEntries){
                if (ItemStack.matches(savedItem, entry.item)){
//                    LOGGER.debug("THEY WERE THE SAME: " + entry.item.getDisplayName().getString());
                    buyEntries.add(entry);
                    break;
                }
            }
        }

    }

    public static class ShopNBTStorage implements Capability.IStorage<WorldShopCapability>{

        @Nullable
        @Override
        public INBT writeNBT(Capability<WorldShopCapability> capability, WorldShopCapability instance, Direction side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<WorldShopCapability> capability, WorldShopCapability instance, Direction side, INBT nbt) {
            CompoundNBT mainNbt = (CompoundNBT) nbt;

            instance.readNBT(mainNbt);
        }


    }
}
