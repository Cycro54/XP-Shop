package invoker54.xpshop.common.api;

import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.BuyEntry;
import invoker54.xpshop.common.item.WalletTier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ShopCapability {
    public static final Logger LOGGER = LogManager.getLogger();

    //region NBT Strings
    //StockNBT Strings
    protected final String ITEM = "ITEM";
    protected final String STOCK_TIMER_START = "STOCK_START";
    protected final String STOCK_LEFT = "STOCK_LEFT";
    protected static final String STOCK_LIST = "STOCK_ITEMS";
    //UnlockNBT Strings
    protected static final String UNLOCKED_LIST = "UNLOCKED_ITEMS";

    //Common string
    protected static final String SIZE = "SIZE";
    protected static final String LEFTOVER_XP = "LEFTOVER_XP";
    protected static final String trader_xp_STRING = "TRADER_XP_FLOAT";
    protected static final String START_TIME = "START_TIME_INT";
    //Player Strings
    protected static final String playerTier_STRING = "WALLET_TIER_STRING";
    protected static final String optionUpgrade_STRING = "OPTION_BOOL";
    protected static final String buyUpgrade_STRING = "BUY_BOOL";
    protected static final String sellUpgrade_STRING = "SELL_BOOL";
    protected static final String transferUpgrade_STRING = "TRANSFER_BOOL";
    protected static final String feeUpgrade_STRING = "FEE_BOOL";
    protected static final String wealthyUpgrade_STRING = "WEALTHY_BOOL";
    //endregion

    //region variables
    //Shop Variables
    protected World level;
    protected final ArrayList<Stock> stockItems = new ArrayList<>();
    protected final ArrayList<ItemStack> unlockedItems = new ArrayList<>();
    public float leftOverXP = 0;
    public float traderXP = 0;
    public int startTime = 0;
    //Player Variables
    protected WalletTier playerTier = WalletTier.ZERO;
    public boolean optionUpgrade = false;
    public boolean buyUpgrade = false;
    public boolean sellUpgrade = false;
    public boolean transferUpgrade = false;
    public boolean feeUpgrade = false;
    public boolean wealthyUpgrade = false;
    //endregion


    public ShopCapability (World level){
        this.level = level;
    }
    public ShopCapability(){}
    public static ShopCapability getShopCap(LivingEntity player){
        ShopCapability playerCap = player.getCapability(ShopProvider.XPSHOPDATA).orElse(null);
        if (playerCap == null) {
            LOGGER.error("MISSING SHOP CAPABILITY!!!");
        }
        return playerCap;
    }
    public void setStartTime(int startTime){
        this.startTime = startTime;
    }
    public int getShopTimeLeft(){
        return (int) ((this.startTime + (ShopConfig.shopUnlockTime * 20F)) - this.level.getGameTime());
    }
    public void refreshTradeXP(){
        traderXP = Math.round(this.getPlayerTier().getMax() * (wealthyUpgrade ? 0.6F : 0.3F));
    }
    public void refreshStock(){
        this.stockItems.clear();
    }
    public WalletTier getPlayerTier(){
        return this.playerTier;
    }
    public void setPlayerTier(WalletTier tier){
        this.playerTier = tier;
    }

    public float getLeftOverXP(){
        return leftOverXP;
    }

    public void setLeftOverXP(float newValue){
        leftOverXP = newValue;
    }

    public void unlockItem(ItemStack lockedItem){
        unlockedItems.add(ItemStack.of(lockedItem.serializeNBT()));
    }

    public boolean isUnlocked(BuyEntry entry){
        if (entry.lockItem.isEmpty()) return true;

        for (ItemStack item : unlockedItems){
            if (item.sameItemStackIgnoreDurability(entry.item)) return true;
        }

        return false;
    }

    public Stock grabStock(BuyEntry entry) {
        //If limitStock is 0 for the BuyEntry, it's always in stock.
        if (entry.limitStock == 0) return null;

        for (Stock stock : stockItems) {
            if (stock.item.equals(entry.item, true)) {
                return stock;
            }
        }

        Stock newStock = new Stock();
        newStock.stockLeft = entry.limitStock;
        newStock.item = entry.item;
        stockItems.add(newStock);
        return newStock;
    }

    //region Save Stuff
    
    public CompoundNBT writeNBT(){
        CompoundNBT mainNBT = new CompoundNBT();

        //region saving shop

        //region First save the stockItem List
        CompoundNBT stockNBT = new CompoundNBT();
        for (int a = 0; a < stockItems.size(); a++){
            //Use a-value as the name of this NBT, then place into stockNBT
            stockNBT.put(String.valueOf(a), stockItems.get(a).serialize());
        }

        //Finally record the size for later
//        LOGGER.error("how many items have limited stock the player bought: " + this.stockItems.size());
        stockNBT.putInt(SIZE, stockItems.size());

        //Place stockNBT into the main NBT variable
        mainNBT.put(STOCK_LIST, stockNBT);
        //endregion

        //region Next, save the unlockedItemList
        CompoundNBT unlockNBT = new CompoundNBT();

        for (int a = 0; a < unlockedItems.size(); a++){
            //Use a-value as the name of this NBT, then place into unlockNBT
            unlockNBT.put(String.valueOf(a), unlockedItems.get(a).serializeNBT());
//            XPShop.LOGGER.debug("Saved item: " + unlockedItems.get(a).getHoverName().getString());
        }

        //Record the size of the list
        unlockNBT.putInt(SIZE, unlockedItems.size());

        //Finally place unlockNBT into main NBT
        mainNBT.put(UNLOCKED_LIST, unlockNBT);
        //endregion

        //Quickly write down the leftover xp, trade xp, and start time .
        mainNBT.putFloat(LEFTOVER_XP, leftOverXP);
        mainNBT.putFloat(trader_xp_STRING, this.traderXP);
        mainNBT.putInt(START_TIME, this.startTime);
        //endregion

        //region saving upgrades
        mainNBT.putString(playerTier_STRING, this.getPlayerTier().name());
        mainNBT.putBoolean(optionUpgrade_STRING, this.optionUpgrade);
        mainNBT.putBoolean(buyUpgrade_STRING, this.buyUpgrade);
        mainNBT.putBoolean(sellUpgrade_STRING, this.sellUpgrade);
        mainNBT.putBoolean(transferUpgrade_STRING, this.transferUpgrade);
        mainNBT.putBoolean(feeUpgrade_STRING, this.feeUpgrade);
        mainNBT.putBoolean(wealthyUpgrade_STRING, this.wealthyUpgrade);
        //endregion

        return mainNBT;
    }
    
    public void readNBT (CompoundNBT mainNBT){

        //region loading shop
        //region First load the stockItem List
        CompoundNBT stockNBT = (CompoundNBT) mainNBT.get(STOCK_LIST);
//        LOGGER.error("How many stock items saved? " + stockNBT.getInt(SIZE));
        for (int a = 0; a < stockNBT.getInt(SIZE); a++){
            Stock stock = new Stock((CompoundNBT) stockNBT.get(a + ""));
//            LOGGER.error("Stock item: " + stock.item.getDisplayName().getString());
//            LOGGER.error("Stock left: " + stock.stockLeft);
            stockItems.add(stock);
        }
        //endregion

        //region Next, save the unlockedItemList
        CompoundNBT unlockNBT = (CompoundNBT) mainNBT.get(UNLOCKED_LIST);

        for (int a = 0; a < unlockNBT.getInt(SIZE); a++){
            //Use a-value as the name of this NBT, then place into unlockNBT
            unlockedItems.add(ItemStack.of((CompoundNBT)unlockNBT.get(String.valueOf(a))));
        }
        //endregion

        //Quickly unload the leftover xp, trade xp, and start time.
        leftOverXP = mainNBT.getFloat(LEFTOVER_XP);
        this.traderXP = mainNBT.getFloat(trader_xp_STRING);
        this.startTime = mainNBT.getInt(START_TIME);
        //endregion

        //region loading upgrades
        if (mainNBT.contains(playerTier_STRING)) {
            this.setPlayerTier(WalletTier.valueOf(mainNBT.getString(playerTier_STRING)));
            this.optionUpgrade = mainNBT.getBoolean(optionUpgrade_STRING);
            this.buyUpgrade = mainNBT.getBoolean(buyUpgrade_STRING);
            this.sellUpgrade = mainNBT.getBoolean(sellUpgrade_STRING);
            this.transferUpgrade = mainNBT.getBoolean(transferUpgrade_STRING);
            this.feeUpgrade = mainNBT.getBoolean(feeUpgrade_STRING);
            this.wealthyUpgrade = mainNBT.getBoolean(wealthyUpgrade_STRING);
        }
        //endregion
    }

    public class Stock{
        public ItemStack item;
        public int stockLeft;

        public Stock(){}

        public Stock(CompoundNBT entryNBT){
            //Item
            item = ItemStack.of((CompoundNBT) entryNBT.get(ITEM));
            //Stock left to buy
            stockLeft = entryNBT.getInt(STOCK_LEFT);
        }

        public CompoundNBT serialize(){
            CompoundNBT entry = new CompoundNBT();
            //Item
            entry.put(ITEM, this.item.serializeNBT());
            //Stock left to buy
            entry.putInt(STOCK_LEFT, this.stockLeft);

            return entry;
        }

        public void reduceStock(){
            //XPShop.LOGGER.debug("REDUCING STOCK!");
            //Reduce stock left
            stockLeft--;
        }
    }

    public static class ShopNBTStorage implements Capability.IStorage<ShopCapability>{

        @Nullable
        @Override
        public INBT writeNBT(Capability<ShopCapability> capability, ShopCapability instance, Direction side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<ShopCapability> capability, ShopCapability instance, Direction side, INBT nbt) {
            CompoundNBT mainNbt = (CompoundNBT) nbt;
            
            instance.readNBT(mainNbt);
        }


    }
    //endregion
}
