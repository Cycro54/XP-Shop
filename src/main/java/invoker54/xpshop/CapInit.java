package invoker54.xpshop;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WanderShopCapability;
import invoker54.xpshop.common.api.WanderShopProvider;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapInit {

    public static void RegisterAll(){
        CapabilityManager.INSTANCE.register(ShopCapability.class, new ShopCapability.ShopNBTStorage(),ShopCapability::new);
        CapabilityManager.INSTANCE.register(WanderShopCapability.class, new WanderShopCapability.ShopNBTStorage(),WanderShopCapability::new);
    }
}
