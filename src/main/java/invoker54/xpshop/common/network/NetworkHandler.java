package invoker54.xpshop.common.network;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.network.msg.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    //Increment the first number if you add new stuff to NetworkHandler class
    //Increment the middle number each time you make a new Message
    //Increment the last number each time you fix a bug
    private static final String PROTOCOL_VERSION = "1.7.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(

            //Name of the channel
            new ResourceLocation(XPShop.MOD_ID, "network"),
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
        //INSTANCE.registerMessage(0, SyncClientCapMsg.class, SyncClientCapMsg::Encode, SyncClientCapMsg::Decode, SyncClientCapMsg::handle);
        INSTANCE.registerMessage(0, SyncClientShopMsg.class,SyncClientShopMsg::encode,SyncClientShopMsg::decode,SyncClientShopMsg::handle);
        INSTANCE.registerMessage(1, SyncServerShopMsg.class,SyncServerShopMsg::encode,SyncServerShopMsg::decode,SyncServerShopMsg::handle);
        INSTANCE.registerMessage(2, BuyItemMsg.class, BuyItemMsg::encode, BuyItemMsg::decode, BuyItemMsg::handle);
        INSTANCE.registerMessage(3, UnlockItemMsg.class, UnlockItemMsg::encode, UnlockItemMsg::decode, UnlockItemMsg::handle);
        INSTANCE.registerMessage(4, SyncClientCapMsg.class, SyncClientCapMsg::encode, SyncClientCapMsg::decode, SyncClientCapMsg::handle);
        INSTANCE.registerMessage(5, SyncServerCapMsg.class, SyncServerCapMsg::encode, SyncServerCapMsg::decode, SyncServerCapMsg::handle);
        INSTANCE.registerMessage(6, OpenSellContainerMsg.class, (message, buf) -> {}, it -> new OpenSellContainerMsg(), OpenSellContainerMsg::handle);
        INSTANCE.registerMessage(7, ClearSellContainerMsg.class, (message, buf) -> {}, it -> new ClearSellContainerMsg(), ClearSellContainerMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(PlayerEntity player, Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }
}
