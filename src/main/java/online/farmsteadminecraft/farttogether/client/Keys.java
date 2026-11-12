package online.farmsteadminecraft.farttogether.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/** The toot key (G by default, rebindable in Controls). Each loader registers {@link #TOOT} its own way. */
public final class Keys {
    // InputConstants.KEY_G, not GLFW's: 26.3 moved input to SDL key codes and has no GLFW
    public static final KeyMapping TOOT = new KeyMapping("key.farmstead_fart_together.toot", InputConstants.KEY_G,
        //? if >=1.21.9 {
        /*KeyMapping.Category.MISC);
        *///?} else
        "key.categories.misc");

    private Keys() {
    }

    /** The key was down at the last tick (so a release is ours, not a test hook's hold). */
    private static boolean wasDown;

    /**
     * Hold to charge: the key going down starts the hold, letting go fires it. A press and release inside one tick
     * (a very quick tap) only shows up as a queued click: that is a tap too. Opening a screen cancels a hold.
     */
    static void tick(Minecraft mc) {
        boolean clicked = false;
        while (TOOT.consumeClick()) clicked = true;
        //? if >=26.2 {
        /*boolean screen = mc.gui.screen() != null;
        *///?} else
        boolean screen = mc.screen != null;
        if (screen) {
            if (wasDown) ClientNet.cancelHold();
            wasDown = false;
            return;
        }
        boolean down = TOOT.isDown();
        if (down) {
            ClientNet.keyDown(mc);
        } else if (wasDown) {
            ClientNet.keyUp(mc);
        } else if (clicked) {
            ClientNet.keyDown(mc);
            ClientNet.keyUp(mc);
        }
        wasDown = down;
    }
}
