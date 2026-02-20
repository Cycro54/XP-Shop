package invoker54.xpshop.init;

import invoker54.invocore.client.invoimage.InvoImage;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.ResourceUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.TabShopScreen;
import invoker54.xpshop.common.data.shops.Shop;
import invoker54.xpshop.common.data.shops.TabShop;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShopScreenInit {
    private static final ModLogger LOGGER = ModLogger.getLogger(ShopScreenInit.class, XPShop.debugMode);
    private static final Map<Class<?>, Consumer<Shop>> shopScreenMap = new HashMap<>();
    
    public static void addScreen(Class<?> classType, Consumer<Shop> shopConsumer){
        if (!Shop.class.isAssignableFrom(classType)){
            LOGGER.error("[XP SHOP] class " + classType.getSimpleName() + " does not inherit Shop!");
            return;
        }
        shopScreenMap.put(classType, shopConsumer);
    }
    
    public static void openShop(Shop shopData){
        Consumer<Shop> consumer = shopScreenMap.getOrDefault(shopData.getClass(), null);
        if (consumer == null) throw new NullPointerException("[XP SHOP] Shop Type: " + shopData.getClass() + " is MISSING!");
        consumer.accept(shopData);
    }
    
    public static void init(){
        //Shop Types
        addScreen(TabShop.class, (shop) ->{
            ClientUtil.getMinecraft().setScreen(new TabShopScreen((TabShop) shop));
        });
    }
}
