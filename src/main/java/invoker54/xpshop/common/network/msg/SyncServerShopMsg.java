package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.ShopData;
import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncServerShopMsg {
    private static final Logger LOGGER = LogManager.getLogger();
    private CompoundNBT nbtData;

    public SyncServerShopMsg(CompoundNBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncServerShopMsg msg, PacketBuffer buffer){
//        LOGGER.error("MAX PACKET SIZE(SERVER SHOP)" + buffer.maxCapacity());
        buffer.writeNbt(msg.nbtData);
    }

    public static SyncServerShopMsg decode(PacketBuffer buffer){
//        LOGGER.error("CURRENT PACKET SIZE(SERVER SHOP)" + buffer.capacity());
        return new SyncServerShopMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncServerShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());
            if (context.getSender() == null){
                LOGGER.error("NO SENDER FOR SYNC SERVER SHOP!!");
                return;
            }
            if (!context.getSender().hasPermissions(ShopConfig.permissionLvl)){
                context.getSender().closeContainer();
                context.getSender().sendMessage(new StringTextComponent("Your permission level isn't high enough!"), Util.NIL_UUID);
            }
            else {
                ShopData.deserialize(msg.nbtData);
            }

            for (PlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()){
                NetworkHandler.sendToPlayer(player, new SyncClientShopMsg(ShopData.serialize()));
            }
//            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), );
        });
        context.setPacketHandled(true);
    }
}
