package online.farmsteadminecraft.farttogether.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.net.Proto;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * The server side of a toot: who has the mod (said hello), their clamped styles, cooldowns. The server decides;
 * clients only ask. Everything runs on the server thread.
 */
public final class TootServer {
    /** Toots further away than this are neither seen nor heard (sound attenuation is 24 blocks). */
    private static final double RANGE = 96;

    private static final class State {
        Style style = Style.SIMPLE;
        long lastToot = Long.MIN_VALUE / 2;
        long lastStyle = Long.MIN_VALUE / 2;
    }

    /** Players whose client said hello; keyed by UUID so a respawn (a new ServerPlayer) keeps the state. */
    private static final Map<UUID, State> MODDED = new HashMap<>();
    private static final Random RANDOM = new Random();

    private TootServer() {
    }

    public static void receive(ServerPlayer player, byte[] data) {
        Proto.ToServer msg = Proto.decodeToServer(data);
        if (msg == null) return;
        if (msg instanceof Proto.HelloC2S hello) hello(player, hello);
        else if (msg instanceof Proto.TootC2S toot) toot(player, toot);
        else if (msg instanceof Proto.StyleC2S style) style(player, style.style());
    }

    private static void hello(ServerPlayer player, Proto.HelloC2S hello) {
        if (hello.version() != Proto.VERSION) {
            FartTogether.LOGGER.info("{} has Fart Together protocol {}, the server {}: toots stay local for them",
                player.getName().getString(), hello.version(), Proto.VERSION);
            return;
        }
        MinecraftServer server = server(player);
        forgetOffline(server);
        State state = MODDED.computeIfAbsent(player.getUUID(), u -> new State());
        state.style = StyleRules.clamp(hello.style(), p -> allowed(player, p));
        state.lastStyle = now();
        send(player, new Proto.HelloS2C(Proto.VERSION, (int) (ServerConfig.COOLDOWN.value() * 1000)));
        // the newcomer learns everybody's style, everybody learns the newcomer's (and the newcomer its clamped own)
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            State os = MODDED.get(other.getUUID());
            if (os == null || other.getUUID().equals(player.getUUID())) continue;
            send(player, new Proto.StyleS2C(other.getUUID(), os.style));
        }
        broadcastStyle(server, player.getUUID(), state.style);
    }

    private static void toot(ServerPlayer player, Proto.TootC2S toot) {
        State state = MODDED.get(player.getUUID());
        if (state == null || !player.isAlive() || player.isSpectator()) return;
        if (!allowed(player, Perm.USE)) return;
        long now = now();
        long cooldown = (long) (ServerConfig.COOLDOWN.value() * 1000);
        // a little slack so a client that waited exactly the cooldown is not refused for network jitter
        if (now - state.lastToot < cooldown - 150) return;
        state.lastToot = now;
        Proto.TootS2C out = new Proto.TootS2C(player.getUUID(), 0, RANDOM.nextLong());
        for (ServerPlayer other : server(player).getPlayerList().getPlayers()) {
            if (!MODDED.containsKey(other.getUUID()) || other.level() != player.level()) continue;
            if (other.distanceToSqr(player) > RANGE * RANGE) continue;
            send(other, out);
        }
    }

    private static void style(ServerPlayer player, Style style) {
        State state = MODDED.get(player.getUUID());
        if (state == null) return;
        long now = now();
        if (now - state.lastStyle < (long) (ServerConfig.STYLE_COOLDOWN.value() * 1000) - 150) return;
        state.lastStyle = now;
        state.style = StyleRules.clamp(style, p -> allowed(player, p));
        broadcastStyle(server(player), player.getUUID(), state.style);
    }

    private static void broadcastStyle(MinecraftServer server, UUID who, Style style) {
        Proto.StyleS2C msg = new Proto.StyleS2C(who, style);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (MODDED.containsKey(other.getUUID())) send(other, msg);
        }
    }

    private static void forgetOffline(MinecraftServer server) {
        for (Iterator<UUID> it = MODDED.keySet().iterator(); it.hasNext(); ) {
            if (server.getPlayerList().getPlayer(it.next()) == null) it.remove();
        }
    }

    /** Whether the player has said hello (tests and future features). */
    public static boolean isModded(UUID player) {
        return MODDED.containsKey(player);
    }

    /** The world closed (single player) or the server stopped: forget everyone. */
    public static void reset() {
        MODDED.clear();
    }

    private static boolean allowed(ServerPlayer player, Perm perm) {
        return ModLoaderAccess.get().permission(player, perm);
    }

    private static void send(ServerPlayer player, Proto.ToClient msg) {
        Network.get().toPlayer(player, Proto.encode(msg));
    }

    private static MinecraftServer server(ServerPlayer player) {
        //? if >=1.21.6 {
        /*return player.level().getServer();
        *///?} else
        return player.server;
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }
}
