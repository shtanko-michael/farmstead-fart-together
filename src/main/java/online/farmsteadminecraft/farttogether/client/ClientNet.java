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
    private static long lastToot;
    private static boolean hasTooted;
    private static long lastStyleSent;
    private static boolean styleDirty;
    private static Object world;
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
        STYLES.clear();
    }

    /** The settings were saved or reloaded: the server hears the new style (at most every two seconds). */
    public static void styleChanged() {
        styleDirty = true;
    }

    /** The toot key: ask the server, or toot locally if it does not run the mod. */
    public static void tootPressed(Minecraft mc) {
        if (mc.player == null) return;
        long now = now();
        if (serverHasMod()) {
            if (hasTooted && now - lastToot < cooldownMillis) return;
            hasTooted = true;
            lastToot = now;
            Network.get().toServer(Proto.encode(new Proto.TootC2S(0)));
        } else {
            if (hasTooted && now - lastToot < LOCAL_COOLDOWN_MILLIS) return;
            hasTooted = true;
            lastToot = now;
            Toots.play(mc, mc.player, ClientConfig.style(), RANDOM.nextLong(), true);
        }
    }

    public static void receive(byte[] data) {
        Proto.ToClient msg = Proto.decodeToClient(data);
        if (msg == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (msg instanceof Proto.HelloS2C hello) {
            serverVersion = hello.version();
            cooldownMillis = hello.cooldownMillis();
            FartTogether.LOGGER.info("server runs Fart Together protocol {}, cooldown {} ms", hello.version(), hello.cooldownMillis());
        } else if (msg instanceof Proto.StyleS2C style) {
            STYLES.put(style.who(), style.style());
            if (TestHooks.enabled()) FartTogether.LOGGER.info("test: style of {} = {}", style.who(), style.style());
        } else if (msg instanceof Proto.TootS2C toot) {
            if (mc.level == null || mc.player == null) return;
            Player who = mc.level.getPlayerByUUID(toot.who());
            if (who == null) return;
            boolean own = who == mc.player;
            Toots.play(mc, who, styleFor(mc, toot.who(), own), toot.seed(), own);
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
