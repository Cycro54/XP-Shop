package invoker54.xpshop.common.data.shops;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.ListContainer;
import invoker54.xpshop.common.data.sections.Section;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.nbt.CompoundTag;

public class TabShop extends Shop {
    public final ListContainer<Section> sectionList;

    public TabShop(){
        super();
        this.sectionList = this.save("sectionList", new ListContainer<>());
    }

    @Override
    public TabShop copy() {
        TabShop copy = new TabShop();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }

    @Override
    public InvoText getTypeName() {
        return InvoText.translate(XPShopLanguageprovider.tabShopTypeName);
    }

    @Override
    public CompoundTag getTypeIcon() {
        return InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "shop/tab_shop")).serializeNBT();
    }

    @Override
    public InvoText getTypeDescription() {
        return InvoText.translate(XPShopLanguageprovider.tabShopTypeDescription);
    }
}