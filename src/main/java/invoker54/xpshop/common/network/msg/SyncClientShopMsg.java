package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.XPShop;
import invoker54.xpshop.client.ExtraUtil;
import invoker54.xpshop.client.screen.ShopScreen;
import invoker54.xpshop.common.data.ShopData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncClientShopMsg {

    private INBT nbtData;

    public SyncClientShopMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void encode(SyncClientShopMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncClientShopMsg decode(PacketBuffer buffer){
        return new SyncClientShopMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientShopMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            CompoundNBT mainNBT = (CompoundNBT) msg.nbtData;

            ShopData.deserialize(mainNBT);

            if (ExtraUtil.mC.screen != null)
            XPShop.LOGGER.debug("What's the current screen?: " + (ExtraUtil.mC.screen.getClass()));

            //If shop screen, refresh it!
            if (ExtraUtil.mC.screen instanceof ShopScreen){
                XPShop.LOGGER.debug("Refreshing shop screen");
                ExtraUtil.mC.setScreen(ExtraUtil.mC.screen);
            }
        });
        context.setPacketHandled(true);
    }

}
