package online.farmsteadminecraft.farttogether.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Fart Together: two or more players toot within {@code together.windowSeconds} of each other and within
 * {@code together.radius} blocks. Every toot that joins makes the combo one bigger: a bigger shared cloud at the
 * players' centre, a chorus for everyone around, a counter in the action bar of each player in it, a short glow and
 * the advancements for 2, 4 and 8. Server thread only.
 */
public final class Together {
    private record Toot(UUID who, ServerLevel level, Vec3 pos, long at) {}

    private static final List<Toot> RECENT = new ArrayList<>();

    private Together() {
    }

    /** Result of a toot: how many players are in the combo now (1 = alone) and who they are. */
    public record Combo(int count, List<ServerPlayer> players, Vec3 centre) {}

    /** Records a toot and returns the combo it belongs to. */
    public static Combo toot(ServerPlayer player, ServerLevel level, Vec3 pos, long nowMillis) {
        long window = (long) (ServerConfig.TOGETHER_WINDOW.value() * 1000);
        RECENT.removeIf(t -> nowMillis - t.at() > window);
        RECENT.removeIf(t -> t.who().equals(player.getUUID()));
        RECENT.add(new Toot(player.getUUID(), level, pos, nowMillis));
        double r = ServerConfig.TOGETHER_RADIUS.value();
        Map<UUID, ServerPlayer> members = new LinkedHashMap<>();
        Vec3 sum = Vec3.ZERO;
        for (Toot t : RECENT) {
            if (t.level() != level || t.pos().distanceToSqr(pos) > r * r) continue;
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(t.who());
            if (p == null || !p.isAlive() || !TootServer.allowed(p, Perm.TOGETHER)) continue;
            members.put(t.who(), p);
            sum = sum.add(t.pos());
        }
        int n = members.size();
        if (n < 2) return new Combo(1, List.of(player), pos);
        return new Combo(n, new ArrayList<>(members.values()), sum.scale(1.0 / n));
    }

    /** Tells the players of a combo how big it is (action bar) and grants the advancements. */
    public static void celebrate(Combo combo) {
        Component text = Component.translatableWithFallback("message.farmstead_fart_together.together",
            "Fart Together x%s!", combo.count());
        for (ServerPlayer p : combo.players()) {
            //? if >=26.1 {
            /*p.sendOverlayMessage(text);
            *///?} else
            p.displayClientMessage(text, true);
            if (ServerConfig.TOGETHER_GLOW.on()) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,
                    40, 0, false, false, false));
            }
            Awards.TOGETHER_2.grant(p);
            if (combo.count() >= 4) Awards.TOGETHER_4.grant(p);
            if (combo.count() >= 8) Awards.TOGETHER_8.grant(p);
        }
    }

    public static void reset() {
        RECENT.clear();
    }
}
