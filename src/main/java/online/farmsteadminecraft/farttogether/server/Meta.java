package online.farmsteadminecraft.farttogether.server;

import java.util.function.DoubleSupplier;

/**
 * Number options a permission mod can set per player or group (LuckPerms meta, e.g.
 * {@code /lp group vip meta set farmstead_fart_together.cooldown 1}). Without one, the server file's value applies.
 */
public enum Meta {
    COOLDOWN("cooldown", () -> ServerConfig.COOLDOWN.value(), 0, 3600),
    CLOUD_RADIUS("cloud.radius", () -> ServerConfig.CLOUD_RADIUS.value(), 0.5, 16),
    CHARGE_MAX("charge.max", () -> ServerConfig.CHARGE_MAX.value(), 1, 10000);

    public final String name;
    private final DoubleSupplier def;
    private final double min, max;

    Meta(String name, DoubleSupplier def, double min, double max) {
        this.name = name;
        this.def = def;
        this.min = min;
        this.max = max;
    }

    public String node() {
        return online.farmsteadminecraft.farttogether.FartTogether.MOD_ID + "." + name;
    }

    public double fallback() {
        return def.getAsDouble();
    }

    /** The permission mod's text for this option, or the server file's value when it is missing or not a number. */
    public double parse(String text) {
        if (text == null || text.isBlank()) return fallback();
        try {
            double v = Double.parseDouble(text.trim());
            return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : fallback();
        } catch (NumberFormatException e) {
            return fallback();
        }
    }
}
