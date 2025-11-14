package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class ListContainer<R extends AbstractContainer> extends AbstractContainer{
    public final List<R> value = new ArrayList<>();

    @Override
    public ListContainer<R> copy() {
        ListContainer<R> copy = new ListContainer<>();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ShopDataManager.SPECIFIC_TYPE, this.getSpecificType());

        ListTag listTag = new ListTag();
        this.value.forEach(item -> listTag.add(item.serializeNBT()));

        tag.put(DATA, listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value.clear();
        tag.getList(DATA, Tag.TAG_COMPOUND).forEach(compound -> {
            CompoundTag subTag = (CompoundTag) compound;
            AbstractContainer container = ShopDataManager.getType(subTag);
            container.deserializeNBT(subTag.getCompound(DATA));
            this.value.add((R) container);
        });
    }
}
