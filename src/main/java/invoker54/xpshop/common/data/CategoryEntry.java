package invoker54.xpshop.common.data;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class CategoryEntry {
    public String categoryName = "";

    public ItemStack categoryItem = ItemStack.EMPTY;

    public final ArrayList<BuyEntry> entries = new ArrayList<>();
}
