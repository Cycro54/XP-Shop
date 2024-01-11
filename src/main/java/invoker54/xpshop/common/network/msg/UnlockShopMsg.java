package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class UnlockShopMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void handle(UnlockShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (context.getSender() == null) return;
            PlayerEntity player = context.getSender();
//            LOGGER.warn(player.getClass());
            ShopCapability playerCap = ShopCapability.getShopCap(player);

            //The fee will be 1/6th of players total allowed xp
            int fee = Math.min(ShopConfig.shopFee,(playerCap.getPlayerTier().getMax()/ 6));
            //Take the xp
            player.giveExperiencePoints(-fee);

            //Unlock the shop by setting the start point (for a limited amount of time)
            playerCap.setStartTime((int) player.level.getGameTime());
//            LOGGER.warn("THIS IS HOW MUCH TIME THEY HAVE: " + (ClientUtil.ticksToTime(playerCap.getShopTimeLeft())));
            NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(playerCap.writeNBT()));
        });
        context.setPacketHandled(true);
    }
}
