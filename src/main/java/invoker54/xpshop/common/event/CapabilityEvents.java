package invoker54.xpshop.common.event;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.capability.PlayerCapabilityProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XPShop.MOD_ID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void attachPlayerCap(AttachCapabilitiesEvent<Entity> event){
        if (!(event.getObject() instanceof Player)) return;
        event.addCapability(PlayerCapabilityProvider.STORAGE_LOCATION, new PlayerCapabilityProvider());
    }
}
