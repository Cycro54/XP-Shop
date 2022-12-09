package invoker54.xpshop.common.network.msg;

import invoker54.invocore.client.ClientUtil;
import invoker54.xpshop.common.api.ShopCapability;
import invoker54.xpshop.common.api.WorldShopCapability;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TradeXPMsg {

    public int playerID;
    public int amount;

    public TradeXPMsg(int playerID, int amount){
        this.playerID = playerID;
        this.amount = amount;
    }

    public static void encode(TradeXPMsg msg, PacketBuffer buffer){
        buffer.writeInt(msg.playerID);
        buffer.writeInt(msg.amount);
    }

    public static TradeXPMsg decode(PacketBuffer buffer){
        return new TradeXPMsg(buffer.readInt(), buffer.readInt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(TradeXPMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity giver = context.getSender();
            if (giver == null) return;
            if (giver.totalExperience < msg.amount) return;

            if (!(giver.level.getEntity(msg.playerID) instanceof PlayerEntity)) return;
            PlayerEntity taker = (PlayerEntity) giver.level.getEntity(msg.playerID);

            ShopCapability otherCap = ShopCapability.getShopCap(taker);
            msg.amount = Math.min(otherCap.getPlayerTier().getMax() - taker.totalExperience, msg.amount);
            if (msg.amount <= 0) return;

            giver.giveExperiencePoints(-msg.amount);
            taker.giveExperiencePoints(msg.amount);
        });
        context.setPacketHandled(true);
    }
}
