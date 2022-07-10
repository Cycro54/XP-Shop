package invoker54.xpshop.common.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuyEntry {
    public final Logger logger = LogManager.getLogger();

    public ItemStack item = ItemStack.EMPTY;
    public int buyPrice;
    public int limitStock;
    public int replenTime;
    public ItemStack lockItem = ItemStack.EMPTY;

    public BuyEntry(){}
    
    public BuyEntry(CompoundNBT buyEntryNBT){
        //Assign all the values
        this.item = ItemStack.of((CompoundNBT) buyEntryNBT.get("item"));

        logger.debug(item.getDisplayName().getString());

        this.buyPrice = buyEntryNBT.getInt("buyPrice");
        this.limitStock = buyEntryNBT.getInt("limitStock");
        this.replenTime = buyEntryNBT.getInt("replenTime");
        this.lockItem = ItemStack.of((CompoundNBT) buyEntryNBT.get("lockItem"));
    }


    
    public CompoundNBT serialize(){
        CompoundNBT buyEntryNBT = new CompoundNBT();

        //All entries below
        buyEntryNBT.put("item", this.item.save(new CompoundNBT()));
        buyEntryNBT.putInt("buyPrice", this.buyPrice);
        buyEntryNBT.putInt("limitStock", this.limitStock);
        buyEntryNBT.putInt("replenTime", this.replenTime);
        buyEntryNBT.put("lockItem", this.lockItem.save(new CompoundNBT()));

        return buyEntryNBT;
    }
    
    
}
