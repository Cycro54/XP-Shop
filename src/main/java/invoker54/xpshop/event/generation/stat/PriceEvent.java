package invoker54.xpshop.event.generation.stat;

import invoker54.xpshop.data.PriceList;
import invoker54.xpshop.event.generation.ShopGenerationCopyEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;


public class PriceEvent extends Event {
    protected final ItemStack currentItem;
    private final PriceList priceList;

    public PriceEvent(ItemStack currentItem){
        this(currentItem, ShopGenerationCopyEvent.getPriceList(currentItem));
    }

    public PriceEvent(ItemStack currentItem, PriceList priceList){
        this.currentItem = currentItem;
        this.priceList = priceList;
    }

    public ItemStack getCurrentItem(){
        return this.currentItem;
    }
    public PriceList getPriceList(){
        return this.priceList;
    }
}
