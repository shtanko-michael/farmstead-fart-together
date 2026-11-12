package online.farmsteadminecraft.farttogether.style;

import java.util.Locale;

import net.minecraft.network.FriendlyByteBuf;

/**
 * How one player's toot looks and sounds. The player sets it in the client file (section {@code style}); the server
 * clamps it ({@link StyleRules}) and sends it to everyone, so friends see and hear the same thing. A toot packet
 * carries only who, how strong and a seed: the style is already known to every receiver.
 *
 * @param color   main colour, 0xRRGGBB
 * @param color2  second colour of a gradient, 0xRRGGBB (ignored for other modes)
 * @param sound   a {@link Sounds} name or {@link Sounds#RANDOM}
 */
public record Style(ColorMode mode, int color, int color2, Shape shape, float size, float amount, float spread,
                    float duration, String sound, float pitch, float volume, Pose pose) {

    public enum ColorMode { SOLID, GRADIENT, RANDOM, RAINBOW }

    /** Particle shape of the cloud; each maps to a vanilla particle (see client Toots). */
    public enum Shape { PUFF, BUBBLES, SPARKS, LEAVES, NOTES, HEARTS, SNOW }

    public enum Pose { CLASSIC, HERO, NONE }

    public static final int DEFAULT_COLOR = 0x7FBF3F;

    /** The plain green puff with a random sound: what everyone looks like without a style or with "simple". */
    public static final Style SIMPLE = new Style(ColorMode.SOLID, DEFAULT_COLOR, DEFAULT_COLOR, Shape.PUFF, 1f, 1f, 1f, 1f,
        Sounds.RANDOM, 1f, 1f, Pose.CLASSIC);

    /** Bytes on the wire: a few dozen; the server refuses style packets over {@code net.Proto.MAX_STYLE_BYTES}. */
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(mode.ordinal());
        buf.writeInt(color);
        buf.writeInt(color2);
        buf.writeByte(shape.ordinal());
        buf.writeFloat(size);
        buf.writeFloat(amount);
        buf.writeFloat(spread);
        buf.writeFloat(duration);
        buf.writeUtf(sound, Sounds.MAX_NAME);
        buf.writeFloat(pitch);
        buf.writeFloat(volume);
        buf.writeByte(pose.ordinal());
    }

    /** Reads what {@link #write} wrote; out-of-range enums and non-finite numbers fall back to {@link #SIMPLE}'s. */
    public static Style read(FriendlyByteBuf buf) {
        ColorMode mode = pick(ColorMode.values(), buf.readByte());
        int color = buf.readInt() & 0xFFFFFF;
        int color2 = buf.readInt() & 0xFFFFFF;
        Shape shape = pick(Shape.values(), buf.readByte());
        float size = finite(buf.readFloat(), 1f), amount = finite(buf.readFloat(), 1f);
        float spread = finite(buf.readFloat(), 1f), duration = finite(buf.readFloat(), 1f);
        String sound = buf.readUtf(Sounds.MAX_NAME).toLowerCase(Locale.ROOT);
        float pitch = finite(buf.readFloat(), 1f), volume = finite(buf.readFloat(), 1f);
        Pose pose = pick(Pose.values(), buf.readByte());
        return new Style(mode, color, color2, shape, size, amount, spread, duration, sound, pitch, volume, pose);
    }

    private static <E> E pick(E[] values, int ordinal) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : values[0];
    }

    private static float finite(float v, float def) {
        return Float.isFinite(v) ? v : def;
    }

    public Style withMode(ColorMode m) {
        return new Style(m, color, color2, shape, size, amount, spread, duration, sound, pitch, volume, pose);
    }

    public Style withShape(Shape s) {
        return new Style(mode, color, color2, s, size, amount, spread, duration, sound, pitch, volume, pose);
    }

    public Style withSound(String s) {
        return new Style(mode, color, color2, shape, size, amount, spread, duration, s, pitch, volume, pose);
    }

    public Style withScale(float size, float amount, float spread, float duration, float pitch, float volume) {
        return new Style(mode, color, color2, shape, size, amount, spread, duration, sound, pitch, volume, pose);
    }
}
