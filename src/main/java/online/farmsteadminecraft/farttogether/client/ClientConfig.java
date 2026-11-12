package online.farmsteadminecraft.farttogether.client;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.config.BoolValue;
import online.farmsteadminecraft.farttogether.config.ConfigFile;
import online.farmsteadminecraft.farttogether.config.DoubleValue;

/**
 * {@code config/farmstead_fart_together-client.toml}: what this player sees and hears. Gameplay rules live in the
 * server file (PLAN.md section 5), which is not part of the scaffold yet.
 */
public final class ClientConfig {
    public static final ConfigFile FILE = new ConfigFile(FartTogether.MOD_ID, "client", 1);

    public static final BoolValue CHECK_UPDATES = FILE.bool("general", "checkUpdates", true)
        .comment("Once at start-up, ask Modrinth whether a newer Fart Together is out and say so in the log. Sends nothing about you.");
    public static final BoolValue STREAMER_SAFE = FILE.bool("general", "streamerSafe", false)
        .comment("Every sound becomes a soft puff: for streams and videos with strict platform rules.");

    public static final DoubleValue VOLUME = FILE.number("sounds", "volume", 1.0, 0.0, 2.0)
        .comment("Loudness of every toot, yours and others'.");
    public static final BoolValue MUTE_OTHERS = FILE.bool("sounds", "mutedForOthers", false)
        .comment("Do not hear other players' toots (you still see the cloud).");

    public static final DoubleValue PARTICLES = FILE.number("particles", "amount", 1.0, 0.0, 3.0)
        .comment("How many cloud particles to draw. 0 hides the cloud for you only.");

    static {
        FILE.preset("family", Map.of(
                "general.streamerSafe", true,
                "sounds.volume", 0.6))
            .preset("performance", Map.of(
                "particles.amount", 0.4));
    }

    private ClientConfig() {
    }

    public static Path dir() {
        return ModLoaderAccess.get().configDir();
    }

    /** Loads (or creates) the file and applies it; returns problems for the player. */
    public static List<String> load() {
        List<String> problems = FILE.load(dir());
        problems.forEach(p -> FartTogether.LOGGER.warn("config: {}", p));
        return problems;
    }

    public static void save() {
        FILE.save(dir(), new java.util.ArrayList<>());
    }
}
