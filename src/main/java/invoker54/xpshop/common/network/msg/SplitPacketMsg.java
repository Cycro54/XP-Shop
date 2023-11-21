package invoker54.xpshop.common.network.msg;

import invoker54.xpshop.common.network.NetworkHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SplitPacketMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Internal communication id. Used to indicate to what wrapped message this belongs to.
     */
    private int communicationId;

    /**
     * The index of the split message in the wrapped message.
     */
    private int packetIndex;

    /**
     * The payload.
     */
    private final byte[] payload;

    public SplitPacketMsg(final int communicationId, final int packetIndex, final byte[] payload) {
//        LOGGER.debug("I AM MAKING A SPLIT PACKET MSG");
        this.communicationId = communicationId;
        this.packetIndex = packetIndex;
        this.payload = payload;
    }

    public static void encode(SplitPacketMsg message, PacketBuffer buf) {
        buf.writeVarInt(message.communicationId);
        buf.writeVarInt(message.packetIndex);
        buf.writeByteArray(message.payload);
    }

    public static SplitPacketMsg decode(final PacketBuffer buf) {
        return new SplitPacketMsg(buf.readVarInt(), buf.readVarInt(), buf.readByteArray());
    }

    public static boolean handle(SplitPacketMsg data, Supplier<NetworkEvent.Context> ctx) {
        NetworkHandler.addPackagePart(data.communicationId, data.packetIndex, data.payload);
        ctx.get().setPacketHandled(true);
        return true;
    }
}
