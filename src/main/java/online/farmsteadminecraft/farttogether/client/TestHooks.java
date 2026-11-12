package online.farmsteadminecraft.farttogether.client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * Hooks for automated in-game tests, off unless the JVM runs with {@code -Dfarmstead.testhooks=true}
 * (the build adds it for {@code -PtestHooks}). The test runner drops {@code <name>.shot} files into
 * {@code <gameDir>/testhooks/}; each one becomes a screenshot named {@code <name>.png}. This avoids
 * sending keystrokes to a window that may not have focus.
 */
public final class TestHooks {
    private static final boolean ENABLED = Boolean.getBoolean("farmstead.testhooks");
    /** Forge 1.20.1 quickPlay joins as a vanilla client and never finishes the FML handshake,
     *  so there the runner passes the address here and the hook connects from the title screen. */
    private static final String JOIN = System.getProperty("farmstead.testhooks.join");
    private static boolean joined;

    private TestHooks() {
    }

    public static void tick(Minecraft mc) {
        // on with -Dfarmstead.testhooks=true, or when a <gameDir>/testhooks folder exists (capture rigs
        // whose launch arguments we do not control)
        if (!ENABLED && !Files.isDirectory(mc.gameDirectory.toPath().resolve("testhooks"))) return;
        //? if forge {
        /*if (JOIN != null && !joined && mc.level == null
            && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen title) {
            joined = true;
            join(mc, title);
        }
        *///?}
        Path dir = mc.gameDirectory.toPath().resolve("testhooks");
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path req : files.toList()) {
                String file = req.getFileName().toString();
                if (file.endsWith(".shot")) {
                    Files.deleteIfExists(req);
                    shoot(mc, file.substring(0, file.length() - 5));
                } else if (file.equals("config.open")) {
                    Files.deleteIfExists(req);
                    FartTogetherClient.openConfigNextTick();
                } else if (file.equals("config.tab")) {
                    String tab = Files.readString(req).trim();
                    Files.deleteIfExists(req);
                    if (screen(mc) instanceof online.farmsteadminecraft.farttogether.config.ConfigScreen cs) cs.showTab(tab);
                } else if (file.equals("config.pick")) {
                    // "<path> <search>"
                    String[] arg = Files.readString(req).trim().split(" ", 2);
                    Files.deleteIfExists(req);
                    if (screen(mc) instanceof online.farmsteadminecraft.farttogether.config.ConfigScreen cs) cs.openPicker(arg[0], arg.length > 1 ? arg[1] : "");
                    else if (screen(mc) instanceof online.farmsteadminecraft.farttogether.config.PickerScreen ps) ps.search(arg.length > 1 ? arg[1] : "");
                } else if (file.equals("try.it")) {
                    Files.deleteIfExists(req);
                    //? if >=26.2 {
                    /*mc.gui.setScreen(null);
                    *///?} else
                    mc.setScreen(null);
                    FartTogetherClient.tryIt();
                } else if (file.equals("camera")) {
                    // "first", "back" or "front": the poses and clouds are seen from outside
                    String view = Files.readString(req).trim();
                    Files.deleteIfExists(req);
                    mc.options.setCameraType(switch (view) {
                        case "back" -> net.minecraft.client.CameraType.THIRD_PERSON_BACK;
                        case "front" -> net.minecraft.client.CameraType.THIRD_PERSON_FRONT;
                        default -> net.minecraft.client.CameraType.FIRST_PERSON;
                    });
                } else if (file.equals("puff.player")) {
                    // a puff behind another player (by name), to check what others see
                    String who = Files.readString(req).trim();
                    Files.deleteIfExists(req);
                    if (mc.level != null) for (var pl : mc.level.players()) if (pl.getName().getString().equals(who)) FartTogetherClient.puff(mc, pl);
                } else if (file.equals("command.run")) {
                    // types a command as a player would (exercises the client command path of each loader)
                    String command = Files.readString(req).trim();
                    Files.deleteIfExists(req);
                    if (mc.player != null) mc.player.connection.sendCommand(command);
                } else if (file.equals("record")) {
                    // "<name> <seconds>": every tick's frame to recordings/<name>/
                    String[] arg = Files.readString(req).trim().split(" ");
                    Files.deleteIfExists(req);
                    Recorder.start(mc, arg[0], arg.length > 1 ? Float.parseFloat(arg[1]) : 4f);
                } else if (file.equals("config.reload")) {
                    Files.deleteIfExists(req);
                    FartTogether.LOGGER.info("test hook reload: problems={}", ClientConfig.load());
                }
            }
        } catch (Exception e) {
            FartTogether.LOGGER.warn("test hook failed", e);
        }
    }

    //? if forge {
    /*private static void join(Minecraft mc, net.minecraft.client.gui.screens.Screen parent) {
        net.minecraft.client.gui.screens.ConnectScreen.startConnecting(parent, mc,
            net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(JOIN),
            new net.minecraft.client.multiplayer.ServerData("test", JOIN, false), false);
    }
    *///?}

    private static net.minecraft.client.gui.screens.Screen screen(Minecraft mc) {
        //? if >=26.2 {
        /*return mc.gui.screen();
        *///?} else
        return mc.screen;
    }

    private static void shoot(Minecraft mc, String name) {
        File gameDir = mc.gameDirectory;
        //? if >=26.2 {
        /*Screenshot.grab(gameDir, name + ".png", mc.gameRenderer.mainRenderTarget(), 1, msg -> FartTogether.LOGGER.info("test hook: {}", msg.getString()));
        *///?} else if >=1.21.6 {
        /*Screenshot.grab(gameDir, name + ".png", mc.getMainRenderTarget(), 1, msg -> FartTogether.LOGGER.info("test hook: {}", msg.getString()));
        *///?} else
        Screenshot.grab(gameDir, name + ".png", mc.getMainRenderTarget(), msg -> FartTogether.LOGGER.info("test hook: {}", msg.getString()));
    }
}
