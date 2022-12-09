package invoker54.xpshop;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapInit {

    public static void RegisterAll(){
        CapabilityManager.INSTANCE.register(ShopCapability.class, new ShopCapability.ShopNBTStorage(),ShopCapability::new);
        CapabilityManager.INSTANCE.register(WorldShopCapability.class, new WorldShopCapability.ShopNBTStorage(), WorldShopCapability::new);
    }
}
