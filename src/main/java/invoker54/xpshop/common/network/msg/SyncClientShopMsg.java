package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.screen.AddItemScreen;
import invoker54.xpshop.client.screen.ShopScreen;
import invoker54.xpshop.common.config.ShopConfig;
import invoker54.xpshop.common.data.ShopData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncClientShopMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    private INBT nbtData;

    public SyncClientShopMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncClientShopMsg msg, PacketBuffer buffer){
//        LOGGER.error("MAX PACKET SIZE(CLIENT SHOP): " + buffer.maxCapacity());
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncClientShopMsg decode(PacketBuffer buffer){
//        LOGGER.error("CURRENT PACKET SIZE(CLIENT SHOP)" + buffer.capacity());
        return new SyncClientShopMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            CompoundNBT mainNBT = (CompoundNBT) msg.nbtData;
//            LOGGER.debug(msg.nbtData.getAsString());

            //This is for syncing the shop data
            if(mainNBT.contains("shopNBT")) {
                ShopData.deserialize(mainNBT);

//                if (ExtraUtil.mC.screen != null)
//                    XPShop.LOGGER.debug("What's the current screen?: " + (ExtraUtil.mC.screen.getClass()));

                //If shop screen, refresh it!
                if (ExtraUtil.mC.screen instanceof ShopScreen || ExtraUtil.mC.screen instanceof AddItemScreen) {
//                    XPShop.LOGGER.debug("Refreshing shop screen");
                    ExtraUtil.mC.setScreen(ExtraUtil.mC.screen);
                }
            }

            //This is for syncing the config
            else {
                ShopConfig.deserialize(mainNBT);
            }
        });
        context.setPacketHandled(true);
    }

}
