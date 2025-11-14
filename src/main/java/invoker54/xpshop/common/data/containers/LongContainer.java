package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;

public class LongContainer extends AbstractContainer{
    public Long value = 0L;

    @Override
    public LongContainer copy() {
        LongContainer copy = new LongContainer();
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
        tag.putLong(DATA, this.value);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value = tag.getLong(DATA);
    }
}
