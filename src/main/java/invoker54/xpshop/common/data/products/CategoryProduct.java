package invoker54.xpshop.common.data.products;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.*;
import invoker54.xpshop.common.datagen.XPShopLanguageprovider;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class CategoryProduct extends Product {
    public final ListContainer<AbstractContainer> customPropertyList;
    public final UUIDContainer id;
    public final StringContainer name;
    public final DoubleContainer cost;
    public final BoolContainer isInvisible;
    public final UUIDContainer parent;
    public final ListContainer<ResourceContainer> customIconList;
    public final LongContainer stockCount;
    public final StringContainer desc;
    public final ListContainer<AbstractContainer> stackList;

    public CategoryProduct(){
        this.customPropertyList = this.save("idList", new ListContainer<>());
        this.id = this.save("id", new UUIDContainer());
        this.name = this.save("name", new StringContainer());
        this.cost = this.save("cost", new DoubleContainer());
        this.isInvisible = this.save("isInvisible", new BoolContainer());
        this.parent = this.save("parent", new UUIDContainer());
        this.customIconList = this.save("customIconList", new ListContainer<>());
        this.stockCount = this.save("stockCount", new LongContainer());
        this.desc = this.save("desc", new StringContainer());
        this.stackList = this.save("stackList", new ListContainer<>());
    }

    @Override
    public InvoText getTypeName() {
        return InvoText.translate(XPShopLanguageprovider.categoryProductTypeName);
    }

    @Override
    public CompoundTag getTypeIcon() {
        return InvoImage.fromTexture(ResourceUtil.create(XPShop.MOD_ID, "product/category_product")).serializeNBT();
    }

    @Override
    public InvoText getTypeDescription() {
        return InvoText.translate(XPShopLanguageprovider.categoryProductTypeDescription);
    }

    @Override
    public CategoryProduct copy() {
        CategoryProduct copy = new CategoryProduct();
        copy.deserializeNBT(this.serializeNBT());
        return copy;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }

    @Override
    public boolean canBuy() {
        return false;
    }

    @Override
    public List<InvoText> getToolTip() {
        return List.of();
    }

    @Override
    public void purchase() {

    }
}