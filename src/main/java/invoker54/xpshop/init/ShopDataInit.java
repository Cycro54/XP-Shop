package invoker54.xpshop.init;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.ShopDataManager;
import invoker54.xpshop.common.data.containers.*;
import invoker54.xpshop.common.data.products.CategoryProduct;
import invoker54.xpshop.common.data.properties.CommandProperty;
import invoker54.xpshop.common.data.properties.ItemProperty;
import invoker54.xpshop.common.data.sections.CategorySection;
import invoker54.xpshop.common.data.shops.TabShop;

public class ShopDataInit {
    private static final ModLogger LOGGER = ModLogger.getLogger(XPShop.class, XPShop.debugMode);

    public static void init(){
        //Container init
        ShopDataManager.addType(new BoolContainer());
        ShopDataManager.addType(new CompoundTagContainer());
        ShopDataManager.addType(new DoubleContainer());
        ShopDataManager.addType(new ItemstackContainer());
        ShopDataManager.addType(new ListContainer<>());
        ShopDataManager.addType(new LongContainer());
        ShopDataManager.addType(new ResourceContainer());
        ShopDataManager.addType(new StringContainer());
        ShopDataManager.addType(new UUIDContainer());

        //Shop init
        ShopDataManager.addType(new TabShop());

        //Section init
        ShopDataManager.addType(new CategorySection());

        //Product init
        ShopDataManager.addType(new CategoryProduct());

        //Property init
        ShopDataManager.addType(new CommandProperty());
        ShopDataManager.addType(new ItemProperty());
    }
}
