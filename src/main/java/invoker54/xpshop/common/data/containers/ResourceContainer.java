package invoker54.xpshop.common.data.containers;

import invoker54.xpshop.common.data.ShopDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ResourceContainer extends AbstractContainer{
    public ResourceLocation value = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");;

    @Override
    public ResourceContainer copy() {
        ResourceContainer copy = new ResourceContainer();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return value.getNamespace();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ShopDataManager.SPECIFIC_TYPE, this.getSpecificType());
        tag.putString(DATA, this.value.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value = ResourceLocation.parse(tag.getString(DATA));
    }
}
