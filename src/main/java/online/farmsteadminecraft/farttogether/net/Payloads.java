package online.farmsteadminecraft.farttogether.net;

//? if >=1.20.5 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * Minecraft 1.20.5+: the two channels as custom payloads that carry {@link Proto} bytes unchanged (Fabric API and
 * NeoForge both register these). 1.20.1 has no payload types; its loaders move the bytes in their own way.
 */
public final class Payloads {
    private Payloads() {
    }

    public record ToServer(byte[] data) implements CustomPacketPayload {
        public static final Type<ToServer> TYPE = new Type<>(FartTogether.id("to_server"));
        public static final StreamCodec<FriendlyByteBuf, ToServer> CODEC =
            CustomPacketPayload.codec((p, buf) -> buf.writeBytes(p.data()), buf -> new ToServer(rest(buf)));

        @Override
        public Type<ToServer> type() {
            return TYPE;
        }
    }

    public record ToClient(byte[] data) implements CustomPacketPayload {
        public static final Type<ToClient> TYPE = new Type<>(FartTogether.id("to_client"));
        public static final StreamCodec<FriendlyByteBuf, ToClient> CODEC =
            CustomPacketPayload.codec((p, buf) -> buf.writeBytes(p.data()), buf -> new ToClient(rest(buf)));

        @Override
        public Type<ToClient> type() {
            return TYPE;
        }
    }

    /** The whole payload body; an oversized one is skipped and becomes empty (ignored by Proto). */
    private static byte[] rest(FriendlyByteBuf buf) {
        int n = buf.readableBytes();
        if (n > Proto.MAX_BYTES) {
            buf.skipBytes(n);
            return new byte[0];
        }
        byte[] out = new byte[n];
        buf.readBytes(out);
        return out;
    }
}
//?}
