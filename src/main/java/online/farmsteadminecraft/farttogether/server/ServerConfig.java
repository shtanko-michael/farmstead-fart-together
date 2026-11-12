package online.farmsteadminecraft.farttogether.server;

import java.util.List;
import java.util.Map;

import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.config.BoolValue;
import online.farmsteadminecraft.farttogether.config.ConfigFile;
import online.farmsteadminecraft.farttogether.config.DoubleValue;
import online.farmsteadminecraft.farttogether.config.IntValue;
import online.farmsteadminecraft.farttogether.config.StringListValue;
import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * {@code config/farmstead_fart_together-server.toml}: the rules of the server (or of the single-player world).
 * {@code /fart reload} reads it again. See PLAN.md section 5 and the README for every key.
 */
public final class ServerConfig {
    public static final ConfigFile FILE = new ConfigFile(FartTogether.MOD_ID, "server", 1);

    public static final DoubleValue COOLDOWN = FILE.number("toot", "cooldownSeconds", 3.0, 0.0, 120.0)
        .comment("Seconds a player waits between two toots. LuckPerms meta farmstead_fart_together.cooldown overrides it per player or group.");
    public static final DoubleValue FULL_HOLD = FILE.number("toot", "fullChargeSeconds", 1.5, 0.2, 10.0)
        .comment("Holding the toot key builds strength: after this many seconds a toot lets out all the gas the player has. A quick tap lets out only charge.minToToot.");

    public static final BoolValue CHARGE_REQUIRED = FILE.bool("charge", "required", true)
        .comment("A toot needs gas, which comes from food. Off: everyone can always toot at medium strength.");
    public static final IntValue CHARGE_MAX = FILE.integer("charge", "max", 100, 10, 1000)
        .comment("Most gas a player can hold. LuckPerms meta farmstead_fart_together.charge.max overrides it.");
    public static final IntValue CHARGE_MIN = FILE.integer("charge", "minToToot", 10, 1, 1000)
        .comment("Gas a toot needs at least; a quick tap lets out this much. Holding the key lets out more, up to all the gas the player has: more gas = a stronger toot.");
    public static final DoubleValue PASSIVE = FILE.number("charge", "passivePerMinute", 0.0, 0.0, 100.0)
        .comment("Gas every player gains each minute without eating.");
    public static final IntValue UNLISTED_FOOD = FILE.integer("charge", "unlistedFood", 2, 0, 100)
        .comment("Gas from a food that no gas_foods file lists (modded food, for example). Weights live in data/<namespace>/gas_foods/*.json and datapacks can change them.");

    public static final BoolValue CLOUD = FILE.bool("cloud", "enabled", true)
        .comment("A toot leaves a gas cloud behind that lingers for a while.");
    public static final DoubleValue CLOUD_SECONDS = FILE.number("cloud", "durationSeconds", 8.0, 1.0, 60.0)
        .comment("How long a full-strength cloud lingers (weaker toots: shorter).");
    public static final DoubleValue CLOUD_RADIUS = FILE.number("cloud", "radius", 2.5, 0.5, 8.0)
        .comment("Radius of a full-strength cloud in blocks. LuckPerms meta farmstead_fart_together.cloud.radius overrides it.");
    public static final BoolValue SCARE = FILE.bool("cloud", "scareMobs", true)
        .comment("Mobs in scaredMobs run away from a cloud (and stop chasing whoever is in it).");
    public static final DoubleValue SCARE_RANGE = FILE.number("cloud", "scareRange", 4.0, 0.0, 16.0)
        .comment("How far beyond the cloud's radius mobs smell it.");
    public static final StringListValue SCARED = FILE.list("cloud", "scaredMobs", List.of(
            "minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_villager", "minecraft:skeleton",
            "minecraft:stray", "minecraft:creeper", "minecraft:spider", "minecraft:cave_spider", "minecraft:witch",
            "minecraft:pillager", "minecraft:vindicator", "minecraft:piglin", "minecraft:hoglin", "minecraft:enderman",
            "minecraft:cow", "minecraft:pig", "minecraft:sheep", "minecraft:chicken", "minecraft:rabbit",
            "minecraft:villager", "minecraft:wandering_trader", "minecraft:fox", "minecraft:wolf", "minecraft:cat"))
        .comment("Mobs that flee from a cloud: ids or #tags (#minecraft:skeletons). Bosses and mobs without legs ignore it anyway.");
    public static final StringListValue EFFECTS = FILE.list("cloud", "effects", List.of())
        .comment("Effects for living things inside a cloud, as \"<effect id> <seconds> <level>\", e.g. \"minecraft:nausea 4 1\". Empty: the cloud is harmless.");
    public static final BoolValue CLOUD_PLAYERS = FILE.bool("cloud", "affectsPlayers", false)
        .comment("Whether cloud effects reach other players (the one who tooted never gets them). Off by default: no PvP.");
    public static final BoolValue RESPECT_PVP = FILE.bool("cloud", "respectServerPvp", true)
        .comment("With affectsPlayers on, still spare players when the server has PvP switched off.");

    public static final BoolValue GOLDEN = FILE.bool("charge", "goldenApple", true)
        .comment("An enchanted golden apple makes the player's next toot golden: gold cloud, sparkles, its own advancement.");

    public static final BoolValue JET = FILE.bool("jetJump", "enabled", true)
        .comment("A toot in mid-air pushes the player up and softens the landing (permission farmstead_fart_together.jetjump).");
    public static final DoubleValue JET_STRENGTH = FILE.number("jetJump", "strength", 0.6, 0.0, 2.0)
        .comment("Upward push of a full-strength jet jump (a normal jump is about 0.42); weaker toots push less.");
    public static final DoubleValue JET_SLOW_FALL = FILE.number("jetJump", "slowFallSeconds", 1.5, 0.0, 10.0)
        .comment("Seconds of slow falling after a jet jump. 0: none (the fall distance is still reset).");

    public static final BoolValue TOGETHER = FILE.bool("together", "enabled", true)
        .comment("Fart Together: players who toot close to each other within a moment make one big shared cloud, a chorus and a combo counter (permission farmstead_fart_together.together).");
    public static final DoubleValue TOGETHER_WINDOW = FILE.number("together", "windowSeconds", 1.5, 0.2, 10.0)
        .comment("How close in time the toots of a combo must be.");
    public static final DoubleValue TOGETHER_RADIUS = FILE.number("together", "radius", 6.0, 1.0, 32.0)
        .comment("How close together (blocks) the players of a combo must stand.");
    public static final BoolValue TOGETHER_GLOW = FILE.bool("together", "glow", true)
        .comment("Players of a combo glow for two seconds.");

    public static final BoolValue ITEMS = FILE.bool("items", "enabled", true)
        .comment("Dedicated servers: register beans and cabbage (needs a restart). Off: the mod adds nothing to the registries, so players without the mod can join too.");
    public static final DoubleValue BEANS_FROM_GRASS = FILE.number("items", "beansFromGrass", 0.05, 0.0, 1.0)
        .comment("Chance that breaking grass or a fern drops beans.");
    public static final DoubleValue CABBAGE_FROM_GRASS = FILE.number("items", "cabbageSeedsFromGrass", 0.04, 0.0, 1.0)
        .comment("Chance that breaking grass or a fern drops cabbage seeds.");
    public static final BoolValue TRADES = FILE.bool("items", "beansInVillagerTrades", true)
        .comment("Farmer villagers buy beans and cabbage and sell bowls of beans (needs a restart; on 26.x a datapack decides).");
    public static final BoolValue LOOT = FILE.bool("items", "beansInLoot", true)
        .comment("Beans and cabbage seeds in village, dungeon, mineshaft and shipwreck chests (applies when the loot tables load: /reload or a restart).");

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
        SCARED.picker(StringListValue.Picker.ENTITIES);
        FILE.reloadCommand("fart reload");
        FILE.preset("family", Map.of("toot.cooldownSeconds", 5.0, "cloud.effects", "[]"))
            .preset("plain", Map.of("style.allowCustom", false))
            .preset("chaos", Map.of("toot.cooldownSeconds", 1.0, "charge.minToToot", 5, "charge.passivePerMinute", 10.0,
                "cloud.durationSeconds", 15.0, "cloud.radius", 4.0, "cloud.effects", "[\"minecraft:nausea 5 1\"]",
                "cloud.affectsPlayers", true, "jetJump.strength", 1.0, "together.windowSeconds", 2.5));
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
