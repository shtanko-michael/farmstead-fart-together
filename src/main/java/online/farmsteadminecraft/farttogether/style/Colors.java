package online.farmsteadminecraft.farttogether.style;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The {@code style.color} setting: {@code #7FBF3F}, a preset name, {@code gradient:#A,#B} (either side may be a
 * preset), {@code random} or {@code rainbow}.
 */
public final class Colors {
    public static final Map<String, Integer> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("swamp", 0x7FBF3F);
        PRESETS.put("bean", 0xB5893B);
        PRESETS.put("lilac", 0xB39DDB);
        PRESETS.put("gold", 0xFFD54F);
        PRESETS.put("pumpkin", 0xFF8F2E);
        PRESETS.put("pine", 0x2E7D4F);
        PRESETS.put("sky", 0x7EC8E3);
        PRESETS.put("rose", 0xF48FB1);
    }

    public record Parsed(Style.ColorMode mode, int color, int color2) {}

    private Colors() {
    }

    /** The parsed setting, or {@code null} if it is not valid (see {@link #problem}). */
    public static Parsed parse(String text) {
        String t = text.trim().toLowerCase(Locale.ROOT);
        if (t.equals("random")) return new Parsed(Style.ColorMode.RANDOM, Style.DEFAULT_COLOR, Style.DEFAULT_COLOR);
        if (t.equals("rainbow")) return new Parsed(Style.ColorMode.RAINBOW, Style.DEFAULT_COLOR, Style.DEFAULT_COLOR);
        if (t.startsWith("gradient:")) {
            String[] two = t.substring("gradient:".length()).split(",");
            if (two.length != 2) return null;
            Integer a = one(two[0]), b = one(two[1]);
            return a == null || b == null ? null : new Parsed(Style.ColorMode.GRADIENT, a, b);
        }
        Integer c = one(t);
        return c == null ? null : new Parsed(Style.ColorMode.SOLID, c, c);
    }

    public static String problem(String text) {
        return parse(text) == null
            ? "expected #RRGGBB, a preset (" + String.join(", ", PRESETS.keySet()) + "), gradient:#A,#B, random or rainbow"
            : null;
    }

    private static Integer one(String s) {
        String t = s.trim();
        if (PRESETS.containsKey(t)) return PRESETS.get(t);
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() != 6) return null;
        try {
            return Integer.parseInt(t, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** HSV hue (0..1) to 0xRRGGBB at full saturation and value. */
    public static int hue(float h) {
        float x = (h % 1f + 1f) % 1f * 6f;
        int i = (int) x;
        float f = x - i;
        float q = 1 - f;
        float r, g, b;
        switch (i) {
            case 0 -> { r = 1; g = f; b = 0; }
            case 1 -> { r = q; g = 1; b = 0; }
            case 2 -> { r = 0; g = 1; b = f; }
            case 3 -> { r = 0; g = q; b = 1; }
            case 4 -> { r = f; g = 0; b = 1; }
            default -> { r = 1; g = 0; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
