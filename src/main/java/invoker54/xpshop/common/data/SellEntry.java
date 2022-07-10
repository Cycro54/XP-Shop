package invoker54.xpshop.common.data;

import net.minecraft.item.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SellEntry {
    public ItemStack item;
    private float sellPrice;

    public SellEntry(ItemStack item, float sellPrice){
        this.item = item;
        this.sellPrice = BigDecimal.valueOf(sellPrice).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public void setItem(ItemStack newValue){
        this.item = newValue;
    }
    public ItemStack getItem(){
        return this.item;
    }

    public void setPrice(float sellPrice){
        this.sellPrice = BigDecimal.valueOf(sellPrice).setScale(2, RoundingMode.HALF_UP).floatValue();
    }
    public float getSellPrice(){
        return sellPrice;
    }

    public float calcPrice(int count){
        return BigDecimal.valueOf(count * sellPrice).setScale(2, RoundingMode.HALF_UP).floatValue();
    }
}
