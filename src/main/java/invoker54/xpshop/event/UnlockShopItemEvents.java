package invoker54.xpshop.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.capability.WorldShopCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class UnlockShopItemEvents {

    //I need to add the openshop event too

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        if (event.getEntity().level.isClientSide) return;
        WorldShopCapability.unlockShopItems(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event){
        if (event.getEntity().level.isClientSide) return;
        if (!(event.getEntity() instanceof Player)) return;
        WorldShopCapability.unlockShopItems((Player) event.getEntity());
    }
}