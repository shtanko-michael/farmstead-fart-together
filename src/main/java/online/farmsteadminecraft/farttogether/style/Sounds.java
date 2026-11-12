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

    /** Picked at random (equal chance) when a style says {@link #RANDOM}. */
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

    /** The sound a toot plays: the style's own, or one from the pool chosen by the toot's seed. */
    public static String choose(String styleSound, Random seeded) {
        if (!RANDOM.equals(styleSound) && ALL.contains(styleSound)) return styleSound;
        return POOL.get(seeded.nextInt(POOL.size()));
    }
}
