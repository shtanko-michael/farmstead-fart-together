package online.farmsteadminecraft.farttogether.style;

import java.util.List;
import java.util.Random;

/**
 * The toot sounds: each is the sound event {@code farmstead_fart_together:toot.<name>} (sounds.json), one file per
 * event so the seed alone decides which one plays and every listener hears the same. Which recording a slot uses is
 * chosen in {@code art/sounds/slots.json} (see tools/sounds.py), not here.
 */
public final class Sounds {
    public static final String RANDOM = "random";
    /** The streamer-safe replacement for every sound, never picked at random. */
    public static final String PUFF = "puff";
    public static final int MAX_NAME = 32;

    /** Every sound random toots can pick from ({@link #BY_POWER} narrows it by strength). */
    public static final List<String> POOL = List.of("short", "squeaky", "bubbly", "long", "trumpet", "tiny", "rumble");
    /** Every sound a style may name. */
    public static final List<String> ALL = List.of("short", "squeaky", "bubbly", "long", "trumpet", "tiny", "rumble", PUFF);
    /** Sounds only players with {@code farmstead_fart_together.style.sounds.rare} may pick by name. None yet (M3). */
    public static final List<String> RARE = List.of();

    private Sounds() {
    }

    public static boolean known(String name) {
        return RANDOM.equals(name) || ALL.contains(name);
    }

    /** Which sounds fit a strength (0 small .. 3 full meter) when the style says {@link #RANDOM}. */
    public static final List<List<String>> BY_POWER = List.of(
        List.of("tiny", "short", "squeaky"),
        List.of("short", "squeaky", "bubbly", "trumpet"),
        List.of("bubbly", "trumpet", "long", "rumble"),
        List.of("long", "rumble"));

    /** The sound a toot plays: the style's own, or one that fits the strength, chosen by the toot's seed. */
    public static String choose(String styleSound, int power, Random seeded) {
        if (!RANDOM.equals(styleSound) && ALL.contains(styleSound)) return styleSound;
        List<String> pool = BY_POWER.get(Math.max(0, Math.min(BY_POWER.size() - 1, power)));
        return pool.get(seeded.nextInt(pool.size()));
    }
}
