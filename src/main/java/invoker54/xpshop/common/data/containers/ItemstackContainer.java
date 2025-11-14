package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemstackContainer extends AbstractContainer{
    public ItemStack value = new ItemStack(Items.DIRT);

    @Override
    public ItemstackContainer copy() {
        ItemstackContainer copy = new ItemstackContainer();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return this.value.getItem().getCreatorModId(this.value);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ShopDataManager.SPECIFIC_TYPE, this.getSpecificType());
        tag.put(DATA, this.value.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value = ItemStack.of(tag.getCompound(DATA));
    }
}
