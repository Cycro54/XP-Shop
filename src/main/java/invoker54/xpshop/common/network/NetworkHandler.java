package invoker54.xpshop.common.network;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.common.network.msg.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkHandler {
    //Increment the first number if you add new stuff to NetworkHandler class
    //Increment the middle number each time you make a new Message
    //Increment the last number each time you fix a bug
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1.14.0";
    private static final ResourceLocation channel = new ResourceLocation(XPShop.MOD_ID, "main_channel");

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(

            //Name of the channel
            channel,
            //Supplier<String> that returns protocol version
            () -> PROTOCOL_VERSION,
            //Checks incoming network protocol version for client (so it's pretty much PROTOCOL_VERSION == INCOMING_PROTOCOL_VERSION)
            PROTOCOL_VERSION::equals,
            //Checks incoming network protocol version for server (If they don't equal, it won't work.)
            PROTOCOL_VERSION::equals
    );
    private static final PacketSplitter splitter = new PacketSplitter(9, INSTANCE, channel);

    public static void init(){
        //This is how you avoid sending anything to the server when you don't need to.
        // (change encode with an empty lambda, and just make decode create a new instance of the target message class)
        //INSTANCE.registerMessage(0, SpawnDiamondMsg.class, (message, buf) -> {}, it -> new SpawnDiamondMsg(), SpawnDiamondMsg::handle);
        //INSTANCE.registerMessage(0, SyncClientCapMsg.class, SyncClientCapMsg::Encode, SyncClientCapMsg::Decode, SyncClientCapMsg::handle);
        splitter.registerMessage(0, SyncClientShopMsg.class,SyncClientShopMsg::encode,SyncClientShopMsg::decode,SyncClientShopMsg::handle);
        splitter.registerMessage(1, SyncServerShopMsg.class,SyncServerShopMsg::encode,SyncServerShopMsg::decode,SyncServerShopMsg::handle);
        INSTANCE.registerMessage(2, BuyItemMsg.class, BuyItemMsg::encode, BuyItemMsg::decode, BuyItemMsg::handle);
        INSTANCE.registerMessage(3, UnlockItemMsg.class, UnlockItemMsg::encode, UnlockItemMsg::decode, UnlockItemMsg::handle);
        splitter.registerMessage(4, SyncClientCapMsg.class, SyncClientCapMsg::encode, SyncClientCapMsg::decode, SyncClientCapMsg::handle);
        splitter.registerMessage(5, SyncServerCapMsg.class, SyncServerCapMsg::encode, SyncServerCapMsg::decode, SyncServerCapMsg::handle);
        INSTANCE.registerMessage(6, OpenSellContainerMsg.class, OpenSellContainerMsg::encode, OpenSellContainerMsg::decode, OpenSellContainerMsg::handle);
        INSTANCE.registerMessage(7, ClearSellContainerMsg.class, (message, buf) -> {}, it -> new ClearSellContainerMsg(), ClearSellContainerMsg::handle);
        INSTANCE.registerMessage(8, SyncWorldShopMsg.class, SyncWorldShopMsg::encode, SyncWorldShopMsg::decode, SyncWorldShopMsg::handle);
        INSTANCE.registerMessage(9, SyncWorldShopRequestMsg.class, SyncWorldShopRequestMsg::encode, SyncWorldShopRequestMsg::decode, SyncWorldShopRequestMsg::handle);
        INSTANCE.registerMessage(10, TradeXPMsg.class, TradeXPMsg::encode, TradeXPMsg::decode, TradeXPMsg::handle);
        INSTANCE.registerMessage(11, ForceRefreshMsg.class, (message, buf) -> {}, it -> new ForceRefreshMsg(), ForceRefreshMsg::handle);
        INSTANCE.registerMessage(12, SyncConfigMsg.class,SyncConfigMsg::encode, SyncConfigMsg::decode, SyncConfigMsg::handle);
        INSTANCE.registerMessage(13, SplitPacketMsg.class, SplitPacketMsg::encode, SplitPacketMsg::decode, SplitPacketMsg::handle);
        INSTANCE.registerMessage(14, UnlockShopMsg.class, (message, buf) -> {}, it -> new UnlockShopMsg(), UnlockShopMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(PlayerEntity player, Object message) {
//        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);

        if (!(player instanceof FakePlayer)) {
            if (splitter.shouldMessageBeSplit(message.getClass())) {
//                LOGGER.debug("SENDING A SPLIT PACKET TO PLAYER");
                splitter.sendToPlayer((ServerPlayerEntity) player, message);
            } else {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
            }
        }
    }
    public static void sendToServer(Object message) {
        if (splitter.shouldMessageBeSplit(message.getClass())) {
//            LOGGER.debug("SENDING A SPLIT PACKET TO SERVER");
            splitter.sendToServer(message);
        } else {
            INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
        }
    }

    public static void addPackagePart(int communicationId, int packetIndex, byte[] payload) {
        splitter.addPackagePart(communicationId, packetIndex, payload);
    }
}
