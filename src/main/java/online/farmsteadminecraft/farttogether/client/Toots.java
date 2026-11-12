package online.farmsteadminecraft.farttogether.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.style.Colors;
import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * What a toot looks and sounds like on this screen: the pose ({@link TootPose}), a cloud of particles that keeps
 * puffing out behind the hips for a moment, and one sound. Everything random comes from the toot's seed, so every
 * player sees the same sound and the same colours.
 */
public final class Toots {
    /** Base particles of a cloud before the style's amount and this player's own setting. */
    private static final int BASE_PARTICLES = 26;
    private static final int BASE_TICKS = 8;

    private record Cloud(Player who, Style style, Random rnd, int perTick, float hue, boolean own, int[] left) {}

    private static final List<Cloud> CLOUDS = new ArrayList<>();

    private Toots() {
    }

    public static void play(Minecraft mc, Player who, Style style, long seed, boolean own) {
        if (mc.level == null) return;
        Random rnd = new Random(seed);
        // the sound first, so the same seed gives the same sound whatever the particle settings
        String sound = ClientConfig.STREAMER_SAFE.on() ? Sounds.PUFF : Sounds.choose(style.sound(), rnd);
        float pitch = style.pitch() * (0.94f + rnd.nextFloat() * 0.12f);
        float hue = rnd.nextFloat();
        if (own || !ClientConfig.MUTE_OTHERS.on()) {
            float volume = style.volume() * ClientConfig.VOLUME.floatValue();
            if (volume > 0) {
                SoundEvent event = SoundEvent.createVariableRangeEvent(FartTogether.id("toot." + sound));
                mc.level.playLocalSound(who.getX(), who.getY() + 0.7, who.getZ(), event, SoundSource.PLAYERS, volume, pitch, false);
            }
        }
        if (TestHooks.enabled()) {
            FartTogether.LOGGER.info("test: toot by {} sound={} shape={} color={} own={}", who.getName().getString(), sound,
                style.shape(), Integer.toHexString(style.color()), own);
        }
        if (style.pose() != Style.Pose.NONE) TootPose.start(who, style.pose());

        int total = Math.round(BASE_PARTICLES * style.amount() * ClientConfig.PARTICLES.floatValue());
        if (!own) total = Math.min(total, ClientConfig.MAX_PER_TOOT.value());
        int ticks = Math.max(2, Math.round(BASE_TICKS * style.duration()));
        if (total <= 0) return;
        CLOUDS.add(new Cloud(who, style, rnd, Math.max(1, total / ticks), hue, own, new int[]{ticks}));
    }

    /** Every client tick: clouds puff out their next particles; finished ones are dropped. */
    public static void tick(Minecraft mc) {
        TootPose.tick();
        if (mc.level == null || mc.isPaused()) {
            if (mc.level == null) CLOUDS.clear();
            return;
        }
        for (Iterator<Cloud> it = CLOUDS.iterator(); it.hasNext(); ) {
            Cloud c = it.next();
            if (c.who().isRemoved() || c.left()[0]-- <= 0) {
                it.remove();
                continue;
            }
            for (int i = 0; i < c.perTick(); i++) emit(mc, c);
        }
    }

    private static void emit(Minecraft mc, Cloud c) {
        Player p = c.who();
        Style s = c.style();
        Random r = c.rnd();
        double yaw = Math.toRadians(p.yBodyRot);
        // behind the hips: opposite of where the body faces
        double bx = Math.sin(yaw), bz = -Math.cos(yaw);
        double hip = p.isCrouching() ? 0.55 : 0.72;
        double spread = 0.18 * s.spread();
        double x = p.getX() + bx * 0.3 + (r.nextDouble() - 0.5) * spread;
        double y = p.getY() + hip + (r.nextDouble() - 0.5) * spread * 0.6;
        double z = p.getZ() + bz * 0.3 + (r.nextDouble() - 0.5) * spread;
        double speed = 0.05 + r.nextDouble() * 0.06 * s.spread();
        double dx = bx * speed + (r.nextDouble() - 0.5) * 0.03 * s.spread();
        double dy = 0.006 + r.nextDouble() * 0.012;
        double dz = bz * speed + (r.nextDouble() - 0.5) * 0.03 * s.spread();
        ParticleOptions type = switch (s.shape()) {
            case PUFF -> ParticleTypes.CLOUD;
            case BUBBLES -> ParticleTypes.BUBBLE_POP;
            case SPARKS -> ParticleTypes.ELECTRIC_SPARK;
            case LEAVES -> ParticleTypes.CHERRY_LEAVES;
            case NOTES -> ParticleTypes.NOTE;
            case HEARTS -> ParticleTypes.HEART;
            case SNOW -> ParticleTypes.SNOWFLAKE;
        };
        if (s.shape() == Style.Shape.NOTES) {
            // the note particle takes its colour (0..1 along the note palette) from the x speed
            dx = r.nextDouble();
            dy = 0;
            dz = 0;
        }
        Particle particle = mc.particleEngine.createParticle(type, x, y, z, dx, dy, dz);
        if (particle == null) return;
        if (s.shape() != Style.Shape.NOTES && s.shape() != Style.Shape.HEARTS) tint(particle, color(s, c.hue(), r));
        particle.scale(s.size());
        if (s.shape() == Style.Shape.PUFF) particle.setLifetime(18 + r.nextInt(14));
    }

    private static int color(Style s, float hue, Random r) {
        int base = switch (s.mode()) {
            case SOLID -> s.color();
            case GRADIENT -> Colors.lerp(s.color(), s.color2(), r.nextFloat());
            case RANDOM -> Colors.hue(hue);
            case RAINBOW -> Colors.hue(hue + r.nextFloat());
        };
        // a little variation keeps the cloud from looking flat
        float k = 0.88f + r.nextFloat() * 0.18f;
        int red = Math.min(255, (int) (((base >> 16) & 255) * k));
        int green = Math.min(255, (int) (((base >> 8) & 255) * k));
        int blue = Math.min(255, (int) ((base & 255) * k));
        return (red << 16) | (green << 8) | blue;
    }

    private static void tint(Particle particle, int rgb) {
        float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
        //? if >=1.21.9 {
        /*if (particle instanceof net.minecraft.client.particle.SingleQuadParticle quad) quad.setColor(r, g, b);
        *///?} else
        particle.setColor(r, g, b);
    }
}
