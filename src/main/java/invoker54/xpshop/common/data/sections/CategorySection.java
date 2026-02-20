package invoker54.xpshop.common.data.sections;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.*;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.nbt.CompoundTag;

public class CategorySection extends Section {
    public final ListContainer<UUIDContainer> productList;

    public CategorySection(){
        this.productList = this.save("productList", new ListContainer<>());
    }

    @Override
    public CategorySection copy() {
        CategorySection copy = new CategorySection();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }

    @Override
    public InvoText getTypeName() {
        return InvoText.translate(XPShopLanguageprovider.categorySectionTypeName);
    }

    @Override
    public CompoundTag getTypeIcon() {
        return InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "shop/tab_shop")).serializeNBT();
    }

    @Override
    public InvoText getTypeDescription() {
        return InvoText.translate(XPShopLanguageprovider.categorySectionTypeDescription);
    }
}
