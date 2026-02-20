package invoker54.xpshop.common.data.containers;

import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.nbt.CompoundTag;

public class InvoTextContainer extends AbstractContainer{
    public InvoText value = InvoText.translate(XPShopLanguageprovider.defaultInvoTextContainer);

    @Override
    public InvoTextContainer copy() {
        InvoTextContainer copy = new InvoTextContainer();
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
        tag.put(DATA, this.value.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.value.deserializeNBT(tag.getCompound(DATA));
    }
}
