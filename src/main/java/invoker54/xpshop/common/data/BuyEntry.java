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
    public ItemStack lockItem = ItemStack.EMPTY;
    public boolean alwaysShow = false;
    public CategoryEntry parentCategory;

    public BuyEntry(){}

    public BuyEntry(CompoundNBT buyEntryNBT, CategoryEntry parentCategory){
        //Assign all the values
        this.item = ItemStack.of((CompoundNBT) buyEntryNBT.get("item"));

        logger.debug(item.getDisplayName().getString());

        this.buyPrice = buyEntryNBT.getInt("buyPrice");
        this.limitStock = buyEntryNBT.getInt("limitStock");
        this.lockItem = ItemStack.of((CompoundNBT) buyEntryNBT.get("lockItem"));
        this.alwaysShow = buyEntryNBT.getBoolean("alwaysShow");
        this.parentCategory = parentCategory;
    }
    
    public CompoundNBT serialize(){
        CompoundNBT buyEntryNBT = new CompoundNBT();

        //All entries below
        buyEntryNBT.put("item", this.item.serializeNBT());
        buyEntryNBT.putInt("buyPrice", this.buyPrice);
        buyEntryNBT.putInt("limitStock", this.limitStock);
        buyEntryNBT.put("lockItem", this.lockItem.save(new CompoundNBT()));
        buyEntryNBT.putBoolean("alwaysShow", this.alwaysShow);

        return buyEntryNBT;
    }
    
    
}
