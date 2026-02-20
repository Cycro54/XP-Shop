package invoker54.xpshop.common.network.message;

import invoker54.invocore.common.ModLogger;
import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.screens.editscreens.ExampleTextProperties;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenShopMenuMsg {
    private static final ModLogger LOGGER = ModLogger.getLogger(OpenShopMenuMsg.class, XPShop.debugMode);
    //This is how the Network Handler will handle the message
    public static void handle(OpenShopMenuMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() ->{
            try {
                ExampleTextProperties.open();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
        context.setPacketHandled(true);
    }
}