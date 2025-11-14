package invoker54.xpshop.common.data.sections;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.*;

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
}
