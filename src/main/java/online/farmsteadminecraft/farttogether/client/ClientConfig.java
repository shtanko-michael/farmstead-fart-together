package online.farmsteadminecraft.farttogether.client;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.config.BoolValue;
import online.farmsteadminecraft.farttogether.config.ConfigFile;
import online.farmsteadminecraft.farttogether.config.DoubleValue;
import online.farmsteadminecraft.farttogether.config.EnumValue;
import online.farmsteadminecraft.farttogether.config.IntValue;
import online.farmsteadminecraft.farttogether.config.StringValue;
import online.farmsteadminecraft.farttogether.style.Colors;
import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * {@code config/farmstead_fart_together-client.toml}: what this player sees and hears, and their own style (section
 * {@code style}) that friends see. Gameplay rules live in the server file ({@code server.ServerConfig}).
 */
public final class ClientConfig {
    public static final ConfigFile FILE = new ConfigFile(FartTogether.MOD_ID, "client", 1);

    public static final BoolValue CHECK_UPDATES = FILE.bool("general", "checkUpdates", true)
        .comment("Once at start-up, ask Modrinth whether a newer Fart Together is out and say so in the log. Sends nothing about you.");
    public static final BoolValue STREAMER_SAFE = FILE.bool("general", "streamerSafe", false)
        .comment("Every sound becomes a soft puff: for streams and videos with strict platform rules.");

    public static final BoolValue SHOW_POSES = FILE.bool("general", "poses", true)
        .comment("Play the toot pose (yours and others'). Off: only the cloud and the sound.");

    public static final DoubleValue VOLUME = FILE.number("sounds", "volume", 1.0, 0.0, 2.0)
        .comment("Loudness of every toot, yours and others'.");
    public static final BoolValue MUTE_OTHERS = FILE.bool("sounds", "mutedForOthers", false)
        .comment("Do not hear other players' toots (you still see the cloud).");

    public static final DoubleValue PARTICLES = FILE.number("particles", "amount", 1.0, 0.0, 3.0)
        .comment("How many cloud particles to draw. 0 hides the cloud for you only.");
    public static final IntValue MAX_PER_TOOT = FILE.integer("particles", "maxPerToot", 200, 10, 1000)
        .comment("Most particles one toot of another player may draw on your screen (weak PCs).");

    /** How other players' toots look to you. */
    public enum Others { THEIRS, SIMPLE, MINE }

    public static final EnumValue<Others> OTHERS = FILE.choice("style", "others", Others.THEIRS)
        .comment("Other players' toots: theirs = their own style, simple = the plain green puff, mine = your style.");
    public static final StringValue COLOR = FILE.text("style", "color", "swamp", 40, Colors::problem)
        .comment("Cloud colour: #RRGGBB, a preset (" + String.join(", ", Colors.PRESETS.keySet())
            + "), gradient:#A,#B, random or rainbow. Gradients and rainbow need the server's permission.");
    public static final EnumValue<Style.Shape> PARTICLE = FILE.choice("style", "particle", Style.Shape.PUFF)
        .comment("Particle shape of the cloud.");
    public static final DoubleValue SIZE = FILE.number("style", "size", 1.0, 0.25, 4.0)
        .comment("Particle size. The server may cap it.");
    public static final DoubleValue AMOUNT = FILE.number("style", "amount", 1.0, 0.0, 4.0)
        .comment("How many particles your cloud has. The server may cap it.");
    public static final DoubleValue SPREAD = FILE.number("style", "spread", 1.0, 0.25, 2.0)
        .comment("How far the cloud spreads.");
    public static final DoubleValue DURATION = FILE.number("style", "duration", 1.0, 0.25, 4.0)
        .comment("How long your cloud lingers. The server may cap it.");
    public static final StringValue SOUND = FILE.text("style", "sound", Sounds.RANDOM, Sounds.MAX_NAME,
            t -> Sounds.known(t.toLowerCase(java.util.Locale.ROOT)) ? null : "expected random or one of " + String.join(", ", Sounds.ALL))
        .comment("Your sound: random or one of " + String.join(", ", Sounds.ALL) + ".");
    public static final DoubleValue PITCH = FILE.number("style", "pitch", 1.0, 0.5, 2.0)
        .comment("Pitch of your sound.");
    public static final DoubleValue STYLE_VOLUME = FILE.number("style", "volume", 1.0, 0.0, 1.0)
        .comment("Loudness of your sound for everyone who hears it.");
    public static final EnumValue<Style.Pose> POSE = FILE.choice("style", "pose", Style.Pose.CLASSIC)
        .comment("Your pose: classic (lean forward, hands on knees), hero (hands on hips) or none.");

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
        ClientNet.styleChanged();
        return problems;
    }

    public static void save() {
        FILE.save(dir(), new java.util.ArrayList<>());
        ClientNet.styleChanged();
    }

    /** The style as this player set it (before the server's limits). */
    public static Style style() {
        Colors.Parsed c = Colors.parse(COLOR.get());
        if (c == null) c = new Colors.Parsed(Style.ColorMode.SOLID, Style.DEFAULT_COLOR, Style.DEFAULT_COLOR);
        return new Style(c.mode(), c.color(), c.color2(), PARTICLE.get(), SIZE.floatValue(), AMOUNT.floatValue(),
            SPREAD.floatValue(), DURATION.floatValue(), SOUND.get().toLowerCase(java.util.Locale.ROOT),
            PITCH.floatValue(), STYLE_VOLUME.floatValue(), POSE.get());
    }
}
