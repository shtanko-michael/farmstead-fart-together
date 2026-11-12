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
    /** A key press shorter than this is a tap: the smallest toot. */
    static final int TAP_MILLIS = 250;

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
        else if (msg instanceof Proto.TootC2S toot) toot(player, Math.max(0, Math.min(60_000, toot.holdMillis())));
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
        send(player, hello(player));
        sendCharge(player);
        // the newcomer learns everybody's style, everybody learns the newcomer's (and the newcomer its clamped own)
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            State os = MODDED.get(other.getUUID());
            if (os == null || other.getUUID().equals(player.getUUID())) continue;
            send(player, new Proto.StyleS2C(other.getUUID(), os.style));
        }
        broadcastStyle(server, player.getUUID(), state.style);
    }

    private static void toot(ServerPlayer player, int holdMillis) {
        State state = MODDED.get(player.getUUID());
        if (state == null || !player.isAlive() || player.isSpectator()) return;
        if (!allowed(player, Perm.USE) || Gas.disabled(player.getUUID())) return;
        long now = now();
        long cooldown = (long) (meta(player, Meta.COOLDOWN) * 1000);
        // a little slack so a client that waited exactly the cooldown is not refused for network jitter
        if (now - state.lastToot < cooldown - 150) return;

        // the hold: a tap lets out the least a toot needs, a full hold everything the player has
        double hold = holdMillis < TAP_MILLIS ? 0 : Math.min(1.0, holdMillis / (ServerConfig.FULL_HOLD.value() * 1000));
        double share;
        if (ServerConfig.CHARGE_REQUIRED.on()) {
            double charge = Gas.charge(player.getUUID());
            int max = Gas.max(player);
            double min = Math.min(ServerConfig.CHARGE_MIN.value(), max);
            if (charge < min) {
                sendCharge(player);
                return;
            }
            double amount = Math.min(charge, min + (charge - min) * hold);
            share = Math.min(1.0, amount / max);
            Gas.set(player, charge - amount);
            sendCharge(player);
        } else {
            share = 0.25 + 0.75 * hold;
        }
        state.lastToot = now;
        int power = share >= 0.999 ? 3 : share >= 0.67 ? 2 : share >= 0.34 ? 1 : 0;

        ServerLevel level = level(player);
        boolean water = player.isUnderWater();
        boolean golden = ServerConfig.GOLDEN.on() && Gas.takeGolden(player.getUUID());
        boolean jet = !water && ServerConfig.JET.on() && !player.onGround() && !player.isPassenger()
            && !player.getAbilities().flying && !player.isFallFlying() && allowed(player, Perm.JETJUMP);
        if (jet) jet(player, share);

        // the cloud: behind the hips, bigger and longer with more gas; under water the gas bubbles up and is gone
        double yaw = Math.toRadians(player.yBodyRot);
        Vec3 pos = player.position().add(Math.sin(yaw) * 0.6, player.isCrouching() ? 0.55 : 0.75, -Math.cos(yaw) * 0.6);
        double radius = 0;
        int linger = 0;
        if (ServerConfig.CLOUD.on() && !water) {
            radius = meta(player, Meta.CLOUD_RADIUS) * (0.5 + 0.5 * share);
            linger = (int) Math.round(ServerConfig.CLOUD_SECONDS.value() * 20 * (0.35 + 0.65 * share));
            GasClouds.add(level, pos, radius, linger, player.getUUID());
        }
        int flags = (jet ? Proto.JET : 0) | (golden ? Proto.GOLDEN : 0) | (water ? Proto.WATER : 0);
        toNear(player, new Proto.TootS2C(player.getUUID(), power, RANDOM.nextLong(),
            (float) pos.x, (float) pos.y, (float) pos.z, (float) radius, linger, flags));

        Awards.FIRST.grant(player);
        if (power == 3) Awards.LEGENDARY.grant(player);
        if (jet) Awards.JET.grant(player);
        if (golden) Awards.GOLDEN.grant(player);
        if (water) Awards.BUBBLES.grant(player);
        if (witherNear(player, level)) Awards.WITHER.grant(player);

        if (ServerConfig.TOGETHER.on() && allowed(player, Perm.TOGETHER)) {
            Together.Combo combo = Together.toot(player, level, pos, now);
            if (combo.count() >= 2) combo(player, level, combo, water);
        }
    }

    /** Jet jump: a push up (weaker toots push less), no fall damage from before, a moment of slow falling. */
    private static void jet(ServerPlayer player, double share) {
        Vec3 v = player.getDeltaMovement();
        double push = ServerConfig.JET_STRENGTH.value() * (0.4 + 0.6 * share);
        player.setDeltaMovement(v.x, Math.max(v.y, 0) + push, v.z);
        player.resetFallDistance();
        int slow = (int) Math.round(ServerConfig.JET_SLOW_FALL.value() * 20);
        if (slow > 0) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,
                slow, 0, false, false, true));
        }
        // the client moves its own player: tell it about the push now
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
    }

    /** A combo grew: one shared cloud at the players' centre, bigger with every player, and the chorus. */
    private static void combo(ServerPlayer player, ServerLevel level, Together.Combo combo, boolean water) {
        double radius = 0;
        int linger = 0;
        if (ServerConfig.CLOUD.on() && !water) {
            radius = Math.min(12.0, meta(player, Meta.CLOUD_RADIUS) * (1 + 0.35 * combo.count()));
            linger = (int) Math.round(ServerConfig.CLOUD_SECONDS.value() * 20 * 1.5);
            GasClouds.add(level, combo.centre(), radius, linger, player.getUUID());
        }
        Vec3 c = combo.centre();
        toNear(player, new Proto.ComboS2C(combo.count(), RANDOM.nextLong(), (float) c.x, (float) c.y, (float) c.z,
            (float) radius, linger));
        Together.celebrate(combo);
    }

    private static boolean witherNear(ServerPlayer player, ServerLevel level) {
        // by class: 26.2 moved the entity type constants out of EntityType
        return !level.getEntitiesOfClass(net.minecraft.world.entity.boss.wither.WitherBoss.class,
            player.getBoundingBox().inflate(24), net.minecraft.world.entity.LivingEntity::isAlive).isEmpty();
    }

    /** Sends to every modded player in this level within hearing range of {@code player}. */
    private static void toNear(ServerPlayer player, Proto.ToClient msg) {
        for (ServerPlayer other : server(player).getPlayerList().getPlayers()) {
            if (!MODDED.containsKey(other.getUUID()) || other.level() != player.level()) continue;
            if (other.distanceToSqr(player) > RANGE * RANGE) continue;
            send(other, msg);
        }
    }

    /** A player finished eating or drinking {@code stack}: gas from the food tables. */
    public static void ate(ServerPlayer player, ItemStack stack, boolean food) {
        if (ServerConfig.GOLDEN.on() && stack.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)) Gas.makeGolden(player.getUUID());
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
            send(p, hello(p));
            sendCharge(p);
        }
    }

    private static Proto.HelloS2C hello(ServerPlayer player) {
        return new Proto.HelloS2C(Proto.VERSION, (int) (meta(player, Meta.COOLDOWN) * 1000),
            (int) (ServerConfig.FULL_HOLD.value() * 1000));
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
        Together.reset();
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
