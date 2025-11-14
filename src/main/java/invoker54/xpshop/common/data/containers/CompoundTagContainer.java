package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;

public class CompoundTagContainer extends AbstractContainer {
    public CompoundTag value = new CompoundTag();

    @Override
    public CompoundTagContainer copy() {
        CompoundTagContainer copy = new CompoundTagContainer();
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
        tag.put(DATA, this.value);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value = tag.getCompound(DATA);
    }
}
 