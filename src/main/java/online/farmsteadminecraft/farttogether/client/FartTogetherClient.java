package online.farmsteadminecraft.farttogether.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
        else FartTogether.updateCheckOff();
    }

    /** The settings screen, for the mod list (NeoForge/Forge) and Mod Menu (Fabric). */
    public static Screen configScreen(Screen parent) {
        String version = FartTogether.VERSION;
        var extras = new ConfigScreen.Extras(FartTogether.NAME, version, () -> FarmsteadBrand.newerThan(version), FarmsteadBrand::source,
            List.of(
                new ConfigScreen.Link("modrinth", "Modrinth page", "https://modrinth.com/mod/" + FartTogether.MODRINTH_SLUG),
                new ConfigScreen.Link("curseforge", "CurseForge page", "https://www.curseforge.com/minecraft/mc-mods/" + FartTogether.MODRINTH_SLUG),
                new ConfigScreen.Link("issues", "Report a bug or an idea", FartTogether.REPO + "/issues")),
            FartTogetherClient::tryIt,
            List.of(new ConfigScreen.CustomTab("my_style", "My style", new MyStyleTab())));
        return new ConfigScreen(parent, ClientConfig.FILE, ClientConfig::save, ClientConfig::load, extras);
    }

    /**
     * "Try it" in the settings: your current style, unsaved, played locally for you only (nothing is sent; others
     * see the saved style after "Done").
     */
    public static void tryIt() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Toots.play(mc, mc.player, ClientConfig.style(), new java.util.Random().nextLong(), true, 1, null, 0, 0, 0);
    }

    /** {@code /farttogether config}: the chat screen closes after the command, so open on the next tick. */
    public static void openConfigNextTick() {
        openConfig = true;
    }

    public static void tick(Minecraft mc) {
        ClientNet.tick(mc);
        Keys.tick(mc);
        Toots.tick(mc);
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
