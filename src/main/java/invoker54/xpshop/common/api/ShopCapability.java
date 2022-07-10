package invoker54.xpshop.common.api;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ClientUtil;
import invoker54.xpshop.common.data.BuyEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ShopCapability {
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
    //endregion

    public ShopCapability (World level){
        this.level = level;
    }
    public ShopCapability(){}

    protected World level;
    protected final ArrayList<Stock> stockItems = new ArrayList<>();
    protected final ArrayList<ItemStack> unlockedItems = new ArrayList<>();
    public float leftOverXP = 0;

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

    public Stock grabStock(BuyEntry entry){
        for (Stock stockItem : stockItems){
            if (stockItem.item.sameItem(entry.item))
                return stockItem;
        }

        if (entry.limitStock != 0){
            Stock newStock = new Stock();
            newStock.stockLeft = entry.limitStock;
            newStock.item = entry.item;
            stockItems.add(newStock);
            return newStock;
        }

        //If the item doesn't exist in the stock list, it's always in stock.
        return null;
    }

    public static ShopCapability getShopCap(LivingEntity player){
        return player.getCapability(ShopProvider.XPSHOPDATA).orElseThrow(NullPointerException::new);
    }
    
    public CompoundNBT writeNBT(){
        CompoundNBT mainNBT = new CompoundNBT();
        
        //region First save the stockItem List
        CompoundNBT stockNBT = new CompoundNBT();
        for (int a = 0; a < stockItems.size(); a++){
            //Use a-value as the name of this NBT, then place into stockNBT
            stockNBT.put(String.valueOf(a), stockItems.get(a).serialize());
        }

        //Finally record the size for later
        stockNBT.putInt(SIZE, stockItems.size());

        //Place stockNBT into the main NBT variable
        mainNBT.put(STOCK_LIST, stockNBT);
        //endregion

        //region Next, save the unlockedItemList
        CompoundNBT unlockNBT = new CompoundNBT();

        for (int a = 0; a < unlockedItems.size(); a++){
            //Use a-value as the name of this NBT, then place into unlockNBT
            unlockNBT.put(String.valueOf(a), unlockedItems.get(a).serializeNBT());
            XPShop.LOGGER.debug("Saved item: " + unlockedItems.get(a).getHoverName().getString());
        }

        //Record the size of the list
        unlockNBT.putInt(SIZE, unlockedItems.size());

        //Finally place unlockNBT into main NBT
        mainNBT.put(UNLOCKED_LIST, unlockNBT);
        //endregion

        //Quickly write down the leftover xp too.
        mainNBT.putFloat(LEFTOVER_XP, leftOverXP);

        return mainNBT;
    }
    
    public void readNBT (CompoundNBT mainNBT){

        //region First load the stockItem List
        CompoundNBT stockNBT = (CompoundNBT) mainNBT.get(STOCK_LIST);
        for (int a = 0; a < stockNBT.getInt(SIZE); a++){
            stockItems.add(new Stock((CompoundNBT) stockNBT.get(String.valueOf(a))));
        }
        //endregion

        //region Next, save the unlockedItemList
        CompoundNBT unlockNBT = (CompoundNBT) mainNBT.get(UNLOCKED_LIST);

        for (int a = 0; a < unlockNBT.getInt(SIZE); a++){
            //Use a-value as the name of this NBT, then place into unlockNBT
            unlockedItems.add(ItemStack.of((CompoundNBT)unlockNBT.get(String.valueOf(a))));
        }
        //endregion

        //Quickly unload the leftover xp too.
        leftOverXP = mainNBT.getFloat(LEFTOVER_XP);
    }

    public class Stock{
        public ItemStack item;
        public int stockTimerStart;
        public int stockLeft;

        public Stock(){}

        public Stock(CompoundNBT entryNBT){
            //Item
            item = ItemStack.of((CompoundNBT) entryNBT.get(ITEM));
            //Stock Timer Start
            stockTimerStart = entryNBT.getInt(STOCK_TIMER_START);
            //Stock left to buy
            stockLeft = entryNBT.getInt(STOCK_LEFT);
        }

        public CompoundNBT serialize(){
            CompoundNBT entry = new CompoundNBT();
            //Item
            entry.put(ITEM, this.item.serializeNBT());
            //Stock Timer Start
            entry.putInt(STOCK_TIMER_START, this.stockTimerStart);
            //Stock left to buy
            entry.putInt(STOCK_LEFT, this.stockLeft);

            return entry;
        }

        public void reduceStock(){
            //XPShop.LOGGER.debug("REDUCING STOCK!");
            //Reduce stock left
            stockLeft--;

//            XPShop.LOGGER.debug("What's current game time?: " + level.getGameTime());
//            XPShop.LOGGER.debug("What's current game time in full numbers?: " + (int)level.getGameTime());
            if (stockTimerStart == 0) stockTimerStart = (int) level.getGameTime();
        }
        
        public String checkStock(BuyEntry entry){
//            XPShop.LOGGER.debug("stock timer start: " + stockTimerStart);
//            XPShop.LOGGER.debug("replenish time (in seconds): " + (entry.replenTime * 20));
//            XPShop.LOGGER.debug("Current time: " + (int) level.getGameTime());
            int timeLeft = (stockTimerStart + (entry.replenTime * 20)) - (int) level.getGameTime();

            if (timeLeft <= 0){
                stockLeft = entry.limitStock;
                stockTimerStart = 0;
            }

            //XPShop.LOGGER.debug("TIME LEFT: " + ClientUtil.ticksToTime(timeLeft));

            //Add 20 ticks to offset time
            return ClientUtil.ticksToTime(timeLeft + 20);
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
}
