package online.farmsteadminecraft.farttogether.net;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * The mod's protocol, written by hand into a {@link FriendlyByteBuf} so the same bytes work on every loader and
 * Minecraft version. Each loader moves the bytes through one channel per direction ({@code farmstead_fart_together:
 * to_server} / {@code to_client}); the first byte says which message follows.
 *
 * <p>Handshake: the client sends {@link HelloC2S} as soon as the server shows the channel. A server without the mod
 * never shows it, so the client stays in "only for yourself" mode; a client without the mod never says hello, so
 * the server never sends it anything. Either way nobody crashes or gets kicked. Versions must match exactly; on a
 * mismatch both sides behave as if the other had no mod.</p>
 */
public final class Proto {
    public static final int VERSION = 1;
    /** No message of ours is anywhere near this; anything larger is dropped unread. */
    public static final int MAX_BYTES = 512;

    private static final int HELLO = 0, TOOT = 1, STYLE = 2;

    private Proto() {
    }

    public sealed interface ToServer permits HelloC2S, TootC2S, StyleC2S {}

    public sealed interface ToClient permits HelloS2C, TootS2C, StyleS2C {}

    /** First message of a modded client: its protocol version and its style. */
    public record HelloC2S(int version, Style style) implements ToServer {}

    /** "Toot now"; {@code power} is 0 (M1 has one strength; charge levels come with M2). */
    public record TootC2S(int power) implements ToServer {}

    /** The player changed their style. */
    public record StyleC2S(Style style) implements ToServer {}

    /** The server's answer to hello: its version and the toot cooldown, so the client does not spam the key. */
    public record HelloS2C(int version, int cooldownMillis) implements ToClient {}

    /** Player {@code who} tooted; {@code seed} makes every receiver pick the same sound and cloud. */
    public record TootS2C(UUID who, int power, long seed) implements ToClient {}

    /** The (server-clamped) style of player {@code who}. */
    public record StyleS2C(UUID who, Style style) implements ToClient {}

    public static byte[] encode(ToServer msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        // Java 17 (Forge 1.20.1): no pattern switch
        if (msg instanceof HelloC2S m) {
            buf.writeByte(HELLO);
            buf.writeVarInt(m.version());
            m.style().write(buf);
        } else if (msg instanceof TootC2S m) {
            buf.writeByte(TOOT);
            buf.writeVarInt(m.power());
        } else if (msg instanceof StyleC2S m) {
            buf.writeByte(STYLE);
            m.style().write(buf);
        }
        return bytes(buf);
    }

    public static byte[] encode(ToClient msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        if (msg instanceof HelloS2C m) {
            buf.writeByte(HELLO);
            buf.writeVarInt(m.version());
            buf.writeVarInt(m.cooldownMillis());
        } else if (msg instanceof TootS2C m) {
            buf.writeByte(TOOT);
            buf.writeUUID(m.who());
            buf.writeVarInt(m.power());
            buf.writeLong(m.seed());
        } else if (msg instanceof StyleS2C m) {
            buf.writeByte(STYLE);
            buf.writeUUID(m.who());
            m.style().write(buf);
        }
        return bytes(buf);
    }

    /** {@code null} for anything malformed, unknown or too big: the caller ignores it. */
    public static ToServer decodeToServer(byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_BYTES) return null;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            return switch (buf.readByte()) {
                case HELLO -> new HelloC2S(buf.readVarInt(), Style.read(buf));
                case TOOT -> new TootC2S(buf.readVarInt());
                case STYLE -> new StyleC2S(Style.read(buf));
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static ToClient decodeToClient(byte[] data) {
        if (data == null || data.length == 0 || data.length > MAX_BYTES) return null;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            return switch (buf.readByte()) {
                case HELLO -> new HelloS2C(buf.readVarInt(), buf.readVarInt());
                case TOOT -> new TootS2C(buf.readUUID(), buf.readVarInt(), buf.readLong());
                case STYLE -> new StyleS2C(buf.readUUID(), Style.read(buf));
                default -> null;
            };
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static byte[] bytes(FriendlyByteBuf buf) {
        byte[] out = new byte[buf.readableBytes()];
        buf.readBytes(out);
        buf.release();
        return out;
    }
}
