package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.ShopCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class XPEvents {
    public static final Logger LOGGER = LogManager.getLogger();

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

    @SubscribeEvent
    public static void correctTotalXP(TickEvent.PlayerTickEvent event){
        if (event.isCanceled()) return;
        if (event.phase == TickEvent.Phase.START) return;

        int actualTotalXP = 0;

        for (int a = 0; a < event.player.experienceLevel; a++){
            actualTotalXP += getXpNeededForNextLevel(a);
        }

        int maxXPForCurrentLvl = getXpNeededForNextLevel(event.player.experienceLevel);
        int currentAmountForLvl = Math.round(event.player.experienceProgress * maxXPForCurrentLvl);
        event.player.experienceProgress = (currentAmountForLvl/(float)maxXPForCurrentLvl);

        actualTotalXP +=  currentAmountForLvl;
        event.player.totalExperience = actualTotalXP;
    }

    public static int getXpNeededForNextLevel(int lvl){
        if (lvl >= 30) {
            return 112 + (lvl - 30) * 9;
        } else {
            return lvl >= 15 ? 37 + (lvl - 15) * 5 : 7 + lvl * 2;
        }
    }
}
