package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class XPEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pickupXP(PlayerXpEvent.XpChange event){
        PlayerEntity player = event.getPlayer();
        ShopCapability playerCap = ShopCapability.getShopCap(player);
        if (playerCap == null) return;
        int max = playerCap.getPlayerTier().getMax();

        if (event.isCanceled()) return;
        if (player.isCreative()) return;

        if (event.getAmount() + player.totalExperience > max){
            event.setAmount(max - player.totalExperience);
        }
    }
}
