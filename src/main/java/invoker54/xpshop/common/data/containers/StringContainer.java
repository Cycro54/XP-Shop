package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;

public class StringContainer extends AbstractContainer{
    public String value = "";

    @Override
    public StringContainer copy() {
        StringContainer copy = new StringContainer();
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
        tag.putString(DATA, this.value);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value = tag.getString(DATA);
    }
}
