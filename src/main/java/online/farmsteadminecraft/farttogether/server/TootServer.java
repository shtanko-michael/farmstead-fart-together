package online.farmsteadminecraft.farttogether.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.net.Proto;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * The server side of a toot: who has the mod (said hello), their clamped styles, cooldowns, gas and clouds. The
 * server decides; clients only ask. Everything runs on the server thread.
 */
public final class TootServer {
    /** Toots further away than this are neither seen nor heard (sound attenuation is 24 blocks). */
    private static final double RANGE = 96;
    /** Strength tiers by the share of a full meter: small, medium, big, full. */
    public static final String[] POWER_NAMES = {"small", "medium", "big", "full"};

    private static final class State {
        Style style = Style.SIMPLE;
        long lastToot = Long.MIN_VALUE / 2;
        long lastStyle = Long.MIN_VALUE / 2;
    }

    /** Players whose client said hello; keyed by UUID so a respawn (a new ServerPlayer) keeps the state. */
    private static final Map<UUID, State> MODDED = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static MinecraftServer current;

    private TootServer() {
    }

    /** The server started: its file and its players' gas. */
    public static void start(MinecraftServer server) {
        current = server;
        ServerConfig.load();
        GasFoods.invalidate();
        Gas.load(server);
    }

    public static void receive(ServerPlayer player, byte[] data) {
        Proto.ToServer msg = Proto.decodeToServer(data);
        if (msg == null) return;
        if (msg instanceof Proto.HelloC2S hello) hello(player, hello);
        else if (msg instanceof Proto.TootC2S toot) toot(player);
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
        send(player, new Proto.HelloS2C(Proto.VERSION, (int) (meta(player, Meta.COOLDOWN) * 1000)));
        sendCharge(player);
        // the newcomer learns everybody's style, everybody learns the newcomer's (and the newcomer its clamped own)
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            State os = MODDED.get(other.getUUID());
            if (os == null || other.getUUID().equals(player.getUUID())) continue;
            send(player, new Proto.StyleS2C(other.getUUID(), os.style));
        }
        broadcastStyle(server, player.getUUID(), state.style);
    }

    private static void toot(ServerPlayer player) {
        State state = MODDED.get(player.getUUID());
        if (state == null || !player.isAlive() || player.isSpectator()) return;
        if (!allowed(player, Perm.USE) || Gas.disabled(player.getUUID())) return;
        long now = now();
        long cooldown = (long) (meta(player, Meta.COOLDOWN) * 1000);
        // a little slack so a client that waited exactly the cooldown is not refused for network jitter
        if (now - state.lastToot < cooldown - 150) return;

        double charge = Gas.charge(player.getUUID());
        int max = Gas.max(player);
        double share;
        if (ServerConfig.CHARGE_REQUIRED.on()) {
            if (charge < Math.min(ServerConfig.CHARGE_MIN.value(), max)) {
                sendCharge(player);
                return;
            }
            share = Math.min(1.0, charge / max);
            Gas.set(player, 0);
            sendCharge(player);
        } else {
            share = 0.5;
        }
        state.lastToot = now;
        int power = share >= 0.999 ? 3 : share >= 0.67 ? 2 : share >= 0.34 ? 1 : 0;

        // the cloud: behind the hips, bigger and longer with more gas
        double yaw = Math.toRadians(player.yBodyRot);
        Vec3 pos = player.position().add(Math.sin(yaw) * 0.6, player.isCrouching() ? 0.55 : 0.75, -Math.cos(yaw) * 0.6);
        double radius = 0;
        int linger = 0;
        if (ServerConfig.CLOUD.on()) {
            radius = meta(player, Meta.CLOUD_RADIUS) * (0.5 + 0.5 * share);
            linger = (int) Math.round(ServerConfig.CLOUD_SECONDS.value() * 20 * (0.35 + 0.65 * share));
            GasClouds.add(level(player), pos, radius, linger, player.getUUID());
        }
        Proto.TootS2C out = new Proto.TootS2C(player.getUUID(), power, RANDOM.nextLong(),
            (float) pos.x, (float) pos.y, (float) pos.z, (float) radius, linger);
        for (ServerPlayer other : server(player).getPlayerList().getPlayers()) {
            if (!MODDED.containsKey(other.getUUID()) || other.level() != player.level()) continue;
            if (other.distanceToSqr(player) > RANGE * RANGE) continue;
            send(other, out);
        }
    }

    /** A player finished eating or drinking {@code stack}: gas from the food tables. */
    public static void ate(ServerPlayer player, ItemStack stack, boolean food) {
        Integer w = GasFoods.weight(server(player), stack, food);
        if (w == null || w == 0) return;
        if (Gas.set(player, Gas.charge(player.getUUID()) + w)) sendCharge(player);
    }

    /** Every server tick (from the loader). */
    public static void tick(MinecraftServer server) {
        current = server;
        GasClouds.tick(server);
        int t = server.getTickCount();
        if (t % 20 == 0 && ServerConfig.PASSIVE.value() > 0) {
            double add = ServerConfig.PASSIVE.value() / 60.0;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (Gas.set(p, Gas.charge(p.getUUID()) + add)) sendCharge(p);
            }
        }
        if (t % (20 * 120) == 0) Gas.save();
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

    /** Tells a modded player their gas (after a meal, a toot, a command). */
    public static void sendCharge(ServerPlayer player) {
        if (!MODDED.containsKey(player.getUUID())) return;
        int min = ServerConfig.CHARGE_REQUIRED.on() ? ServerConfig.CHARGE_MIN.value() : 0;
        boolean enabled = allowed(player, Perm.USE) && !Gas.disabled(player.getUUID());
        send(player, new Proto.ChargeS2C((int) Gas.charge(player.getUUID()), Gas.max(player), min, enabled));
    }

    /** After {@code /fart reload}: the new cooldown and limits reach every modded player. */
    public static void resendAll(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!MODDED.containsKey(p.getUUID())) continue;
            send(p, new Proto.HelloS2C(Proto.VERSION, (int) (meta(p, Meta.COOLDOWN) * 1000)));
            sendCharge(p);
        }
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

    /** Whether the player has said hello (commands, tests). */
    public static boolean isModded(UUID player) {
        return MODDED.containsKey(player);
    }

    /** The world closed (single player) or the server stopped: save gas, forget everyone. */
    public static void reset() {
        MODDED.clear();
        GasClouds.reset();
        Gas.reset();
        current = null;
    }

    public static boolean allowed(ServerPlayer player, Perm perm) {
        return ModLoaderAccess.get().permission(player, perm);
    }

    /** A number option: the permission mod's meta for this player, else the server file. */
    public static double meta(ServerPlayer player, Meta meta) {
        return meta.parse(ModLoaderAccess.get().meta(player, meta));
    }

    private static void send(ServerPlayer player, Proto.ToClient msg) {
        Network.get().toPlayer(player, Proto.encode(msg));
    }

    static MinecraftServer server(ServerPlayer player) {
        //? if >=1.21.6 {
        /*return player.level().getServer();
        *///?} else
        return player.server;
    }

    static ServerLevel level(ServerPlayer player) {
        //? if >=1.21.6 {
        /*return player.level();
        *///?} else
        return player.serverLevel();
    }

    private static long now() {
        return System.nanoTime() / 1_000_000L;
    }
}
