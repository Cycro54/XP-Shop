package invoker54.xpshop.common.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.api.WorldShopProvider;
import invoker54.xpshop.common.config.ShopConfig;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class RefreshDealsEvent {
    protected static final Logger LOGGER = LogManager.getLogger();

    public static String getTimeLeft(World level){
        int ticksPassed = (int) level.getGameTime();
        //Have to convert refreshTime from seconds to ticks
        int totalShopTime = ShopConfig.refreshTime * 20;
        return ClientUtil.ticksToTime(totalShopTime - (ticksPassed % totalShopTime));
    }

    @SubscribeEvent
    public static void tickRefresh(TickEvent.WorldTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (event.world.isClientSide) return;
        if (event.world.dimension() != World.OVERWORLD) return;
        if (event.world.getGameTime() % (ShopConfig.refreshTime * 20F) != 0) return;

        //Refresh every dimension and send to every player
        for (ServerWorld world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            WorldShopCapability.getShopCap(world).refreshDeals();
        }
    }
}
