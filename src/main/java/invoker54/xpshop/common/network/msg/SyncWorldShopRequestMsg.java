package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.api.WorldShopCapability;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncWorldShopRequestMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    public ResourceLocation level;

    public SyncWorldShopRequestMsg(ResourceLocation level){
        this.level = level;
    }

    public static void encode(SyncWorldShopRequestMsg msg, PacketBuffer buffer){
        buffer.writeResourceLocation(msg.level);
    }

    public static SyncWorldShopRequestMsg decode(PacketBuffer buffer){
        return new SyncWorldShopRequestMsg(buffer.readResourceLocation());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncWorldShopRequestMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            WorldShopCapability cap = WorldShopCapability.getShopCap(server.overworld());

            NetworkHandler.sendToPlayer(context.getSender(), new SyncWorldShopMsg(cap.writeNBT()));

//            Iterable<ServerWorld> worlds = server.getAllLevels();
//            for(World world : worlds){
//                if (world.dimension().getRegistryName().equals(msg.level)){
//                    WorldShopCapability cap = WorldShopCapability.getShopCap(world);
//
//                    NetworkHandler.sendToPlayer(context.getSender(), new SyncWorldShopMsg(cap.writeNBT()));
//                }
//            }
        });
        context.setPacketHandled(true);
    }
}
