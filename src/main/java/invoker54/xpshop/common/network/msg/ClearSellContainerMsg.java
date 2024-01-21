package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.client.screen.SellContainer;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.event.XPEvents;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ClearSellContainerMsg {
    public static final Logger LOGGER = LogManager.getLogger();

    //This is how the Network Handler will handle the message
    public static void handle(ClearSellContainerMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            
            if (player == null) return;
            SellContainer container = ((SellContainer)player.containerMenu);
            if (container == null) return;

            ShopCapability cap = ShopCapability.getShopCap(player);
            if (cap == null) return;
            
            float totalExp = container.totalExtraXP + cap.getLeftOverXP();
            cap.setLeftOverXP(totalExp - ((int)totalExp));
            cap.traderXP -= container.totalExtraXP;
            XPEvents.giveExperience(player, (int)totalExp);

            //Clear the container
            container.tempInv.clearContent();

            NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(cap.writeNBT()));
        });
        context.setPacketHandled(true);
    }
}
