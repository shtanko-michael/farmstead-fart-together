package online.farmsteadminecraft.farttogether.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.net.Proto;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * The client side of the protocol: says hello when the server shows our channel, keeps everyone's styles, turns the
 * toot key into a request, and plays the toots the server announces. Without a modded server every toot stays
 * local ("only for yourself").
 */
public final class ClientNet {
    private static final long STYLE_RESEND_MILLIS = 2_100;
    private static final long LOCAL_COOLDOWN_MILLIS = 1_000;

    private static boolean helloSent;
    /** Set by the server's hello; 0 = no modded server (local mode). */
    private static int serverVersion;
    private static int cooldownMillis = 3_000;
    /** How long a hold takes to reach full strength (the server's {@code toot.fullChargeSeconds}). */
    private static int fullHoldMillis = 1_500;
    /** A press shorter than this is a tap (the server uses the same number). */
    static final int TAP_MILLIS = 250;
    /** When the toot key went down (-1: not holding); a test hook may schedule the release. */
    private static long holdStart = -1, autoRelease = -1;
    private static long lastToot;
    private static boolean hasTooted;
    private static long lastStyleSent;
    private static boolean styleDirty;
    private static Object world;
    /** The last gas the server told us; {@code chargeMax} 0 = not known (no modded server). */
    private static int charge, chargeMax, chargeMin;
    private static boolean tootEnabled = true;
    private static long lastRefusal;
    private static final Map<UUID, Style> STYLES = new HashMap<>();
    private static final Random RANDOM = new Random();

    private ClientNet() {
    }

    public static boolean serverHasMod() {
        return serverVersion == Proto.VERSION;
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            if (world != null) reset();
            return;
        }
        if (world != mc.level) {
            // a new connection (or a dimension change, which keeps the connection: keep what we know then)
            if (world == null) reset();
            world = mc.level;
        }
        if (autoRelease > 0 && now() >= autoRelease) {
            autoRelease = -1;
            keyUp(mc);
        }
        Network net = Network.get();
        if (!helloSent && net != null && net.serverHasChannel()) {
            helloSent = true;
            net.toServer(Proto.encode(new Proto.HelloC2S(Proto.VERSION, ClientConfig.style())));
            lastStyleSent = now();
            styleDirty = false;
        }
        if (styleDirty && serverHasMod() && now() - lastStyleSent >= STYLE_RESEND_MILLIS) {
            styleDirty = false;
            lastStyleSent = now();
            net.toServer(Proto.encode(new Proto.StyleC2S(ClientConfig.style())));
        }
    }

    private static void reset() {
        world = null;
        helloSent = false;
        serverVersion = 0;
        styleDirty = false;
        charge = chargeMax = chargeMin = 0;
        fullHoldMillis = 1_500;
        holdStart = autoRelease = -1;
        tootEnabled = true;
        STYLES.clear();
    }

    /** The settings were saved or reloaded: the server hears the new style (at most every two seconds). */
    public static void styleChanged() {
        styleDirty = true;
    }

    /**
     * The toot key went down (called every tick while it is held): starts a hold, unless the toot would be refused
     * anyway (then the action bar says why) or the cooldown is still running.
     */
    public static void keyDown(Minecraft mc) {
        if (holdStart >= 0 || mc.player == null) return;
        long now = now();
        if (serverHasMod()) {
            if (!tootEnabled || charge < chargeMin) {
                // say why nothing happens (not more than once a second for a held key)
                if (now - lastRefusal > 1000) {
                    lastRefusal = now;
                    var text = net.minecraft.network.chat.Component.translatableWithFallback(
                        tootEnabled ? "message.farmstead_fart_together.no_gas" : "message.farmstead_fart_together.disabled",
                        tootEnabled ? "Not enough gas: eat something first (beans work best)" : "Toots are switched off for you on this server");
                    //? if >=26.1 {
                    /*mc.player.sendOverlayMessage(text);
                    *///?} else
                    mc.player.displayClientMessage(text, true);
                }
                return;
            }
            if (hasTooted && now - lastToot < cooldownMillis) return;
        } else if (hasTooted && now - lastToot < LOCAL_COOLDOWN_MILLIS) {
            return;
        }
        holdStart = now;
    }

    /** The toot key was let go: the hold so far fires (a tap when it was short). */
    public static void keyUp(Minecraft mc) {
        if (holdStart < 0 || mc.player == null) {
            holdStart = -1;
            return;
        }
        long now = now();
        int held = (int) Math.min(60_000, now - holdStart);
        holdStart = -1;
        hasTooted = true;
        lastToot = now;
        if (serverHasMod()) {
            Network.get().toServer(Proto.encode(new Proto.TootC2S(held)));
        } else {
            // only for yourself: the hold alone sets the strength
            float share = held < TAP_MILLIS ? 0f : Math.min(1f, held / 1500f);
            int power = share >= 0.999f ? 3 : share >= 0.67f ? 2 : share >= 0.34f ? 1 : 0;
            Toots.play(mc, mc.player, ClientConfig.style(), RANDOM.nextLong(), true, power, null, 0, 0, 0);
        }
    }

    /** A screen opened over a held key: drop the hold without tooting. */
    public static void cancelHold() {
        holdStart = -1;
        autoRelease = -1;
    }

    /** Test hooks: hold the key for {@code millis}, then let go (0: a tap). */
    public static void holdFor(Minecraft mc, int millis) {
        keyDown(mc);
        if (holdStart < 0) return;
        if (millis <= 0) keyUp(mc);
        else autoRelease = now() + millis;
    }

    /**
     * How far the current hold has built up, 0 (a tap) .. 1 (full strength), or -1 when the key is not held. On a
     * modded server the server's {@code toot.fullChargeSeconds} sets the pace; alone it is a second and a half.
     */
    public static float holdShare() {
        if (holdStart < 0) return -1;
        long held = now() - holdStart;
        if (held < TAP_MILLIS) return 0;
        return Math.min(1f, held / (float) (serverHasMod() ? fullHoldMillis : 1500));
    }

    /** Gas the current hold would let out if the key were let go now (what the server will compute). */
    public static float holdGas() {
        float share = holdShare();
        if (share < 0) return 0;
        return Math.min(charge, chargeMin + (charge - chargeMin) * share);
    }

    /** Gas for the HUD meter: {@code [charge, max, min]}, max 0 when the server does not run the mod. */
    public static int[] gas() {
        return new int[]{charge, chargeMax, chargeMin};
    }

    public static void receive(byte[] data) {
        Proto.ToClient msg = Proto.decodeToClient(data);
        if (msg == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (msg instanceof Proto.HelloS2C hello) {
            serverVersion = hello.version();
            cooldownMillis = hello.cooldownMillis();
            fullHoldMillis = Math.max(1, hello.fullHoldMillis());
            FartTogether.LOGGER.info("server runs Fart Together protocol {}, cooldown {} ms", hello.version(), hello.cooldownMillis());
        } else if (msg instanceof Proto.StyleS2C style) {
            STYLES.put(style.who(), style.style());
            if (TestHooks.enabled()) FartTogether.LOGGER.info("test: style of {} = {}", style.who(), style.style());
        } else if (msg instanceof Proto.ChargeS2C c) {
            charge = c.charge();
            chargeMax = c.max();
            chargeMin = c.min();
            tootEnabled = c.enabled();
            if (TestHooks.enabled()) FartTogether.LOGGER.info("test: charge {}/{} min {} enabled {}", charge, chargeMax, chargeMin, tootEnabled);
        } else if (msg instanceof Proto.TootS2C toot) {
            if (mc.level == null || mc.player == null) return;
            Player who = mc.level.getPlayerByUUID(toot.who());
            if (who == null) return;
            boolean own = who == mc.player;
            Toots.play(mc, who, styleFor(mc, toot.who(), own), toot.seed(), own, toot.power(),
                new net.minecraft.world.phys.Vec3(toot.x(), toot.y(), toot.z()), toot.radius(), toot.lingerTicks(), toot.flags());
        } else if (msg instanceof Proto.ComboS2C combo) {
            if (mc.level == null || mc.player == null) return;
            // the shared cloud takes a blend of the members' colours (as this screen shows their styles)
            int r = 0, g = 0, b = 0, n = 0;
            for (UUID m : combo.members()) {
                int rgb = styleFor(mc, m, m.equals(mc.player.getUUID())).color();
                r += (rgb >> 16) & 255;
                g += (rgb >> 8) & 255;
                b += rgb & 255;
                n++;
            }
            int blend = n == 0 ? Style.DEFAULT_COLOR : ((r / n) << 16) | ((g / n) << 8) | (b / n);
            Toots.combo(mc, combo.count(), combo.seed(), new net.minecraft.world.phys.Vec3(combo.x(), combo.y(), combo.z()),
                combo.radius(), combo.lingerTicks(), blend);
        }
    }

    /** The style a toot by {@code who} is shown with on this screen. */
    static Style styleFor(Minecraft mc, UUID who, boolean own) {
        // my own toots: the server's clamped copy, which is what everyone else sees
        Style mine = STYLES.getOrDefault(mc.player.getUUID(), ClientConfig.style());
        if (own) return mine;
        return switch (ClientConfig.OTHERS.get()) {
            case THEIRS -> STYLES.getOrDefault(who, Style.SIMPLE);
            case SIMPLE -> Style.SIMPLE;
            case MINE -> mine;
        };
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }
}
