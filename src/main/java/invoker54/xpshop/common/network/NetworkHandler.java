package invoker54.xpshop.common.network;

import invoker54.invocore.Invocore;
import invoker54.xpshop.common.network.message.OpenShopMenuMsg;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    //Increment the first number if you add new stuff to NetworkHandler class
    //Increment the middle number each time you make a new Message
    //Increment the last number each time you fix a bug
    private static final String PROTOCOL_VERSION = "1.0.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            //Name of the channel
            ResourceLocation.fromNamespaceAndPath(Invocore.MOD_ID, "network"),
            //Supplier<String> that returns protocol version
            () -> PROTOCOL_VERSION,
            //Checks incoming network protocol version for client (so it's pretty much PROTOCOL_VERSION == INCOMING_PROTOCOL_VERSION)
            PROTOCOL_VERSION::equals,
            //Checks incoming network protocol version for server (If they don't equal, it won't work.)
            PROTOCOL_VERSION::equals
    );

    public static void init(){
        //This is how you avoid sending anything to the server when you don't need to.
        // (change encode with an empty lambda, and just make decode create a new instance of the target message class)
        //INSTANCE.registerMessage(0, SpawnDiamondMsg.class, (message, buf) -> {}, it -> new SpawnDiamondMsg(), SpawnDiamondMsg::handle);
        INSTANCE.registerMessage(0, OpenShopMenuMsg.class, (message, buf) -> {}, it -> new OpenShopMenuMsg(), OpenShopMenuMsg::handle);
    }
}
