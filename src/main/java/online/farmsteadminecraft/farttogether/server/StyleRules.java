package online.farmsteadminecraft.farttogether.server;

import java.util.function.Predicate;

import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;

/** What the server lets a player's style be: its config limits and the player's permissions. */
public final class StyleRules {
    private StyleRules() {
    }

    public static Style clamp(Style s, Predicate<Perm> perms) {
        if (!ServerConfig.ALLOW_CUSTOM.on() || !perms.test(Perm.STYLE_CUSTOM)) return Style.SIMPLE;
        Style out = s;
        if ((s.mode() == Style.ColorMode.RAINBOW || s.mode() == Style.ColorMode.GRADIENT) && !perms.test(Perm.STYLE_RAINBOW)) {
            out = out.withMode(Style.ColorMode.SOLID);
        }
        if (!ServerConfig.shapeAllowed(out.shape())) out = out.withShape(Style.Shape.PUFF);
        String sound = out.sound();
        if (!Sounds.known(sound) || (!Sounds.RANDOM.equals(sound) && !ServerConfig.ALLOWED_SOUNDS.contains(sound))
            || (Sounds.RARE.contains(sound) && !perms.test(Perm.STYLE_SOUNDS_RARE))) {
            out = out.withSound(Sounds.RANDOM);
        }
        float huge = perms.test(Perm.STYLE_HUGE) ? 1.5f : 1f;
        out = out.withScale(
            clamp(out.size(), 0.25f, ServerConfig.MAX_SIZE.floatValue() * huge),
            clamp(out.amount(), 0f, ServerConfig.MAX_AMOUNT.floatValue()),
            clamp(out.spread(), 0.25f, 2f),
            clamp(out.duration(), 0.25f, ServerConfig.MAX_DURATION.floatValue() * huge),
            clamp(out.pitch(), 0.5f, 2f),
            clamp(out.volume(), 0f, 1f));
        return out;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
