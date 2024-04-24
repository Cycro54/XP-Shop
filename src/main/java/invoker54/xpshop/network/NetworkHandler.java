package invoker54.xpshop.network;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.network.message.AddEntryMsg;
import invoker54.xpshop.network.message.RemoveEntryMsg;
import invoker54.xpshop.network.message.SyncConfigMsg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static int PROTOCOL_VERSION = 0;

    public static int nextID() {
        return PROTOCOL_VERSION++;
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(

            //Name of the channel
            new ResourceLocation(XPShop.MOD_ID, "network"),
            //Supplier<String> that returns protocol version
            () -> String.valueOf(PROTOCOL_VERSION),
            //Checks incoming network protocol version for client (so it's pretty much PROTOCOL_VERSION == INCOMING_PROTOCOL_VERSION)
            String.valueOf(PROTOCOL_VERSION)::equals,
            //Checks incoming network protocol version for server (If they don't equal, it won't work.)
            String.valueOf(PROTOCOL_VERSION)::equals
    );

    public static void init(){
        INSTANCE.registerMessage(nextID(), AddEntryMsg.class, AddEntryMsg::encode, AddEntryMsg::decode, AddEntryMsg::handle);
        INSTANCE.registerMessage(nextID(), RemoveEntryMsg.class, RemoveEntryMsg::encode, RemoveEntryMsg::decode, RemoveEntryMsg::handle);
        INSTANCE.registerMessage(nextID(), SyncConfigMsg.class, SyncConfigMsg::encode, SyncConfigMsg::decode, SyncConfigMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(Player player, Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }

    public static void sendToServer(Object message){
        NetworkHandler.INSTANCE.sendToServer(message);
    }

    public static void sendToAllPlayers(Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

}