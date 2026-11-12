package online.farmsteadminecraft.farttogether.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * Gas clouds on the server: a toot leaves one behind the player for a few seconds. It is not an entity (nothing to
 * register or render, nothing a vanilla client could choke on): the server keeps the sphere and acts on what is near
 * it, each client draws it from the toot message. Mobs in {@code cloud.scaredMobs} run away and drop their target;
 * living things inside get {@code cloud.effects} (none by default; other players only with
 * {@code cloud.affectsPlayers}, PvP rules and {@code farmstead_fart_together.cloud.bypass}).
 */
public final class GasClouds {
    /** How often (ticks) clouds look around: often enough that a mob does not get a bite in. */
    private static final int PERIOD = 5;
    /** A scared mob forgets its target and keeps running this long after it last smelled a cloud. */
    private static final int SCARED_TICKS = 60;

    private record Cloud(ServerLevel level, Vec3 pos, double radius, long end, UUID owner) {}

    private record Scared(PathfinderMob mob, Vec3 from, long until) {}

    private static final List<Cloud> CLOUDS = new ArrayList<>();
    private static final List<Scared> SCARED = new ArrayList<>();
    private static List<String> parsedFrom;
    private static final List<EntityType<?>> TYPES = new ArrayList<>();
    private static final List<TagKey<EntityType<?>>> TYPE_TAGS = new ArrayList<>();

    private GasClouds() {
    }

    public static void add(ServerLevel level, Vec3 pos, double radius, int ticks, UUID owner) {
        CLOUDS.add(new Cloud(level, pos, radius, level.getGameTime() + ticks, owner));
    }

    public static int count() {
        return CLOUDS.size();
    }

    public static void reset() {
        CLOUDS.clear();
        SCARED.clear();
    }

    public static void tick(net.minecraft.server.MinecraftServer server) {
        if (CLOUDS.isEmpty() && SCARED.isEmpty()) return;
        if (server.getTickCount() % PERIOD != 0) return;
        for (Iterator<Cloud> it = CLOUDS.iterator(); it.hasNext(); ) {
            Cloud c = it.next();
            long now = c.level().getGameTime();
            if (now >= c.end()) {
                it.remove();
                continue;
            }
            if (ServerConfig.SCARE.on()) scare(c, now);
            if (!ServerConfig.EFFECTS.get().isEmpty()) affect(c);
        }
        // the frightened keep running for a while after they smelled the cloud
        for (Iterator<Scared> it = SCARED.iterator(); it.hasNext(); ) {
            Scared s = it.next();
            PathfinderMob mob = s.mob();
            if (mob.isRemoved() || !mob.isAlive() || mob.level().getGameTime() > s.until()) {
                it.remove();
                continue;
            }
            mob.setTarget(null);
            if (mob instanceof Creeper creeper) creeper.setSwellDir(-1);
            if (mob.getNavigation().isDone()) flee(mob, s.from());
        }
    }

    private static void scare(Cloud c, long now) {
        double reach = c.radius() + ServerConfig.SCARE_RANGE.value();
        AABB box = new AABB(c.pos(), c.pos()).inflate(reach);
        for (PathfinderMob mob : c.level().getEntitiesOfClass(PathfinderMob.class, box, m -> m.isAlive() && scares(m.getType()))) {
            if (mob.position().distanceToSqr(c.pos()) > reach * reach) continue;
            boolean known = false;
            for (int i = 0; i < SCARED.size(); i++) {
                if (SCARED.get(i).mob() == mob) {
                    SCARED.set(i, new Scared(mob, c.pos(), now + SCARED_TICKS));
                    known = true;
                    break;
                }
            }
            if (!known) SCARED.add(new Scared(mob, c.pos(), now + SCARED_TICKS));
            mob.setTarget(null);
            if (mob instanceof Creeper creeper) creeper.setSwellDir(-1);
            flee(mob, c.pos());
        }
    }

    private static void flee(PathfinderMob mob, Vec3 from) {
        Vec3 to = DefaultRandomPos.getPosAway(mob, 12, 6, from);
        if (to == null) {
            Vec3 d = mob.position().subtract(from);
            d = d.lengthSqr() < 1e-4 ? new Vec3(1, 0, 0) : d.normalize();
            to = mob.position().add(d.scale(10));
        }
        mob.getNavigation().moveTo(to.x, to.y, to.z, 1.35);
    }

    private static void affect(Cloud c) {
        List<MobEffectInstance> effects = effects();
        if (effects.isEmpty()) return;
        ServerPlayer owner = c.level().getServer().getPlayerList().getPlayer(c.owner());
        AABB box = new AABB(c.pos(), c.pos()).inflate(c.radius());
        for (LivingEntity e : c.level().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (e.getUUID().equals(c.owner())) continue;
            if (e.position().add(0, e.getBbHeight() / 2, 0).distanceToSqr(c.pos()) > c.radius() * c.radius()) continue;
            if (e instanceof ServerPlayer target) {
                if (!ServerConfig.CLOUD_PLAYERS.on() || TootServer.allowed(target, Perm.CLOUD_BYPASS)) continue;
                if (ServerConfig.RESPECT_PVP.on() && owner != null && !owner.canHarmPlayer(target)) continue;
            }
            for (MobEffectInstance fx : effects) e.addEffect(new MobEffectInstance(fx));
        }
    }

    /** {@code "minecraft:nausea 4 1"} lines of the server file as effects; bad lines are skipped. */
    static List<MobEffectInstance> effects() {
        List<MobEffectInstance> out = new ArrayList<>();
        for (String line : ServerConfig.EFFECTS.get()) {
            String[] p = line.trim().split("\\s+");
            ResourceLocation id = FartTogether.parseId(p[0]);
            if (id == null) continue;
            int seconds = p.length > 1 ? parse(p[1], 4) : 4;
            int level = p.length > 2 ? parse(p[2], 1) : 1;
            //? if >=1.21.2 {
            /*var holder = BuiltInRegistries.MOB_EFFECT.get(id);
            if (holder.isEmpty()) continue;
            out.add(new MobEffectInstance(holder.get(), seconds * 20, Math.max(0, level - 1)));
            *///?} else if >=1.20.5 {
            var holder = BuiltInRegistries.MOB_EFFECT.getHolder(id);
            if (holder.isEmpty()) continue;
            out.add(new MobEffectInstance(holder.get(), seconds * 20, Math.max(0, level - 1)));
            //?} else {
            /*MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
            if (effect == null) continue;
            out.add(new MobEffectInstance(effect, seconds * 20, Math.max(0, level - 1)));
            *///?}
        }
        return out;
    }

    private static int parse(String s, int def) {
        try {
            return Math.max(1, Math.min(600, Integer.parseInt(s)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    static boolean scares(EntityType<?> type) {
        List<String> list = ServerConfig.SCARED.get();
        if (list != parsedFrom) {
            parsedFrom = list;
            TYPES.clear();
            TYPE_TAGS.clear();
            for (String s : list) {
                boolean tag = s.startsWith("#");
                ResourceLocation id = FartTogether.parseId(tag ? s.substring(1) : s);
                if (id == null) continue;
                if (tag) TYPE_TAGS.add(TagKey.create(Registries.ENTITY_TYPE, id));
                else BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(TYPES::add);
            }
        }
        if (TYPES.contains(type)) return true;
        for (TagKey<EntityType<?>> t : TYPE_TAGS) {
            if (type.builtInRegistryHolder().is(t)) return true;
        }
        return false;
    }
}
