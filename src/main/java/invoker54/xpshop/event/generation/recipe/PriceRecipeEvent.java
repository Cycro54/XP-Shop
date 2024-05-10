package invoker54.xpshop.event.generation.recipe;

import invoker54.xpshop.data.recipe.ItemData;
import invoker54.xpshop.event.generation.stat.PriceEvent;
import net.minecraft.world.item.ItemStack;

public class PriceRecipeEvent extends PriceEvent {
    private final ItemData itemData;
    public PriceRecipeEvent(ItemStack stack) {
        super(stack);
        this.itemData = ItemData.getMatchingData(stack);
    }
    public ItemData getItemData(){
        return this.itemData;
    }
}
