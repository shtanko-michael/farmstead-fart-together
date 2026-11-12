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

    static void tick(Minecraft mc) {
        while (TOOT.consumeClick()) ClientNet.tootPressed(mc);
    }
}
