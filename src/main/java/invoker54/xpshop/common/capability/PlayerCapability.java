package invoker54.xpshop.common.capability;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.data.containers.AbstractContainer;
import invoker54.xpshop.common.data.containers.MapContainer;
import net.minecraft.world.entity.player.Player;

public class PlayerCapability extends MapContainer implements IPlayerCapability {
    public static PlayerCapability get(Player player){
        return player.getCapability(PlayerCapabilityProvider.PLAYER_DATA).orElse(new PlayerCapability());
    }

    @Override
    public AbstractContainer copy() {
        return null;
    }

    @Override
    public String getModId() {
        return XPShop.MOD_ID;
    }
}
