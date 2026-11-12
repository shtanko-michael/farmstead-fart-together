package online.farmsteadminecraft.farttogether.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.brand.FarmsteadBrand;
import online.farmsteadminecraft.farttogether.config.ConfigScreen;

/** Client-side entry shared by all loaders. */
public final class FartTogetherClient {
    private static boolean openConfig;

    private FartTogetherClient() {
    }

    /** Loads the settings file; every loader calls this once when the client starts. */
    public static void init() {
        ClientConfig.load();
        if (ClientConfig.CHECK_UPDATES.on()) FartTogether.checkForUpdate();
    }

    /** The settings screen, for the mod list (NeoForge/Forge) and Mod Menu (Fabric). */
    public static Screen configScreen(Screen parent) {
        String version = FartTogether.VERSION;
        var extras = new ConfigScreen.Extras(FartTogether.NAME, version, () -> FarmsteadBrand.newerThan(version),
            List.of(
                new ConfigScreen.Link("modrinth", "Modrinth page", "https://modrinth.com/mod/" + FartTogether.MODRINTH_SLUG),
                new ConfigScreen.Link("curseforge", "CurseForge page", "https://www.curseforge.com/minecraft/mc-mods/" + FartTogether.MODRINTH_SLUG),
                new ConfigScreen.Link("issues", "Report a bug or an idea", FartTogether.REPO + "/issues")),
            FartTogetherClient::tryIt);
        return new ConfigScreen(parent, ClientConfig.FILE, ClientConfig::save, ClientConfig::load, extras);
    }

    /**
     * "Try it" in the settings: a local puff behind you with the current particle setting. Scaffold stand-in until
     * the real toot (pose, own particles and sounds) exists; nothing is sent to the server.
     */
    public static void tryIt() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        puff(mc, mc.player);
    }

    /** A cloud of vanilla sneeze particles (the panda's green puff) behind a player's hips. */
    public static void puff(Minecraft mc, Player player) {
        int count = (int) Math.round(24 * ClientConfig.PARTICLES.value());
        double yaw = Math.toRadians(player.getYRot());
        double bx = Math.sin(yaw) * 0.35, bz = -Math.cos(yaw) * 0.35;
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < count; i++) {
            mc.level.addParticle(ParticleTypes.SNEEZE,
                player.getX() + bx + (rnd.nextDouble() - 0.5) * 0.3, player.getY() + 0.75 + (rnd.nextDouble() - 0.5) * 0.2,
                player.getZ() + bz + (rnd.nextDouble() - 0.5) * 0.3,
                bx * 0.15 + (rnd.nextDouble() - 0.5) * 0.03, 0.01, bz * 0.15 + (rnd.nextDouble() - 0.5) * 0.03);
        }
    }

    /** {@code /farttogether config}: the chat screen closes after the command, so open on the next tick. */
    public static void openConfigNextTick() {
        openConfig = true;
    }

    public static void tick(Minecraft mc) {
        TestHooks.tick(mc);
        Recorder.tick(mc);
        if (openConfig) {
            openConfig = false;
            //? if >=26.2 {
            /*mc.gui.setScreen(configScreen(null));
            *///?} else
            mc.setScreen(configScreen(null));
        }
    }
}
