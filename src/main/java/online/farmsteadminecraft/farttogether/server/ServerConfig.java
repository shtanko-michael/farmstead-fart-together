package online.farmsteadminecraft.farttogether.server;

import java.util.List;
import java.util.Map;

import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.config.BoolValue;
import online.farmsteadminecraft.farttogether.config.ConfigFile;
import online.farmsteadminecraft.farttogether.config.DoubleValue;
import online.farmsteadminecraft.farttogether.config.StringListValue;
import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * {@code config/farmstead_fart_together-server.toml}: the rules of the server (or of the single-player world).
 * Gameplay keys beyond the toot itself (charge, cloud, jet jump, combos) arrive with M2/M3, see PLAN.md section 5.
 */
public final class ServerConfig {
    public static final ConfigFile FILE = new ConfigFile(FartTogether.MOD_ID, "server", 1);

    public static final DoubleValue COOLDOWN = FILE.number("toot", "cooldownSeconds", 3.0, 0.0, 120.0)
        .comment("Seconds a player waits between two toots.");

    public static final BoolValue ALLOW_CUSTOM = FILE.bool("style", "allowCustom", true)
        .comment("Let players use their own style (colour, particles, sound, pose). Off: everyone looks plain.");
    public static final DoubleValue MAX_SIZE = FILE.number("style", "maxSize", 2.0, 0.25, 4.0)
        .comment("Largest particle size multiplier a style may use (x1.5 with farmstead_fart_together.style.huge).");
    public static final DoubleValue MAX_AMOUNT = FILE.number("style", "maxAmount", 2.0, 0.25, 4.0)
        .comment("Largest particle amount multiplier a style may use.");
    public static final DoubleValue MAX_DURATION = FILE.number("style", "maxDuration", 2.0, 0.25, 4.0)
        .comment("Longest cloud duration multiplier a style may use (x1.5 with farmstead_fart_together.style.huge).");
    public static final StringListValue ALLOWED_PARTICLES = FILE.list("style", "allowedParticles",
            List.of("puff", "bubbles", "sparks", "leaves", "notes", "hearts", "snow"))
        .comment("Particle shapes players may pick; others become puff.");
    public static final StringListValue ALLOWED_SOUNDS = FILE.list("style", "allowedSounds", Sounds.ALL)
        .comment("Sounds players may pick by name; others become random.");
    public static final DoubleValue STYLE_COOLDOWN = FILE.number("style", "updateCooldownSeconds", 2.0, 0.0, 60.0)
        .comment("How often a player may change their style.");

    static {
        FILE.preset("family", Map.of("toot.cooldownSeconds", 5.0))
            .preset("plain", Map.of("style.allowCustom", false));
    }

    private ServerConfig() {
    }

    public static List<String> load() {
        List<String> problems = FILE.load(ModLoaderAccess.get().configDir());
        problems.forEach(p -> FartTogether.LOGGER.warn("server config: {}", p));
        return problems;
    }

    public static boolean shapeAllowed(Style.Shape s) {
        return ALLOWED_PARTICLES.contains(s.name().toLowerCase(java.util.Locale.ROOT));
    }
}
