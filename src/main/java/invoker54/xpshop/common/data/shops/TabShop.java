package invoker54.xpshop.common.data.shops;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.ListContainer;
import invoker54.xpshop.common.data.sections.Section;

public class TabShop extends Shop {
    public final ListContainer<Section> sectionList;

    public TabShop(){
        this.sectionList = this.save("sectionList", new ListContainer<>());
    }

    @Override
    public void addID() {

    }

    @Override
    public void removeID() {

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
}