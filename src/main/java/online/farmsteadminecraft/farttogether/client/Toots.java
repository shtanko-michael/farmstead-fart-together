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
import net.minecraft.world.phys.Vec3;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.net.Proto;
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

    /** Particles and size by strength (small, medium, big, full meter). */
    private static final float[] AMOUNT = {0.55f, 1f, 1.6f, 2.2f};
    private static final float[] SIZE = {0.8f, 1f, 1.2f, 1.4f};
    private static final float[] VOLUME = {0.6f, 0.8f, 1f, 1f};

    private record Cloud(Player who, Style style, Random rnd, int perTick, float hue, boolean own, float size, int flags,
                         int[] left) {}

    /** Golden gas (after an enchanted golden apple): a gold gradient whatever the style's colour. */
    private static final int GOLD = 0xFFD84A, GOLD_LIGHT = 0xFFF6B0;
    /** The soft puff every cloud is made of by default. */
    private static final net.minecraft.core.particles.SimpleParticleType PUFF = ParticleTypes.CAMPFIRE_COSY_SMOKE;
    /** A cloud of the jet blast (not a protocol flag: only inside this class). */
    private static final int JET_BLAST = 0x100;
    /** The chorus of a combo: existing sounds layered (a real chorus recording is still to come). */
    private static final String[] CHORUS = {"trumpet", "long", "rumble"};

    /** The gas cloud left behind: a few slow particles at a fixed place for its lifetime. */
    private record Linger(Vec3 pos, double radius, Style style, Random rnd, float hue, boolean own, int[] left) {}

    private static final List<Cloud> CLOUDS = new ArrayList<>();
    private static final List<Linger> LINGERS = new ArrayList<>();

    private Toots() {
    }

    /**
     * Plays a toot: {@code power} 0..3 sets the sound pool, loudness and cloud size; {@code cloud} (null for none)
     * is where the server keeps the gas cloud of {@code radius} for {@code linger} ticks.
     */
    public static void play(Minecraft mc, Player who, Style style, long seed, boolean own, int power, Vec3 cloud,
                            float radius, int linger, int flags) {
        if (mc.level == null) return;
        int tier = Math.max(0, Math.min(3, power));
        Random rnd = new Random(seed);
        boolean water = (flags & Proto.WATER) != 0;
        if ((flags & Proto.GOLDEN) != 0) {
            style = new Style(Style.ColorMode.GRADIENT, GOLD, GOLD_LIGHT, style.shape(), style.size(), style.amount(),
                style.spread(), style.duration(), style.sound(), style.pitch(), style.volume(), style.pose());
        }
        // the sound first, so the same seed gives the same sound whatever the particle settings
        String sound = ClientConfig.STREAMER_SAFE.on() ? Sounds.PUFF : Sounds.choose(style.sound(), tier, rnd);
        // under water every toot is the bubbly one (the seed was still used, so all listeners stay in step)
        if (water && !ClientConfig.STREAMER_SAFE.on()) sound = "bubbly";
        float pitch = style.pitch() * (0.94f + rnd.nextFloat() * 0.12f);
        float hue = rnd.nextFloat();
        if (own || !ClientConfig.MUTE_OTHERS.on()) {
            float volume = style.volume() * ClientConfig.VOLUME.floatValue() * VOLUME[tier];
            if (volume > 0) {
                SoundEvent event = SoundEvent.createVariableRangeEvent(FartTogether.id("toot." + sound));
                mc.level.playLocalSound(who.getX(), who.getY() + 0.7, who.getZ(), event, SoundSource.PLAYERS, volume, pitch, false);
            }
        }
        if (TestHooks.enabled()) {
            FartTogether.LOGGER.info("test: toot by {} sound={} shape={} color={} own={} power={} radius={} linger={} flags={}",
                who.getName().getString(), sound, style.shape(), Integer.toHexString(style.color()), own, tier, radius, linger, flags);
        }
        if (style.pose() != Style.Pose.NONE) TootPose.start(who, style.pose());

        int total = Math.round(BASE_PARTICLES * style.amount() * AMOUNT[tier] * ClientConfig.PARTICLES.floatValue());
        if (!own) total = Math.min(total, ClientConfig.MAX_PER_TOOT.value());
        int ticks = Math.max(2, Math.round(BASE_TICKS * style.duration()));
        // soft puffs are big: a few of them read as one cloud, many would be a fog
        if (style.shape() == Style.Shape.PUFF && !water) total = Math.max(3, total / 4);
        if (total <= 0) return;
        CLOUDS.add(new Cloud(who, style, rnd, Math.max(1, total / ticks), hue, own, SIZE[tier], flags, new int[]{ticks}));
        if ((flags & Proto.JET) != 0) {
            // the jet: a short blast straight down from the feet
            CLOUDS.add(new Cloud(who, style, new Random(seed ^ 0x9E3779B97F4A7C15L), Math.max(2, total / 4), hue, own,
                SIZE[tier], JET_BLAST, new int[]{5}));
        }
        if ((flags & Proto.GOLDEN) != 0) {
            // a few extra pale-gold puffs make it glow (soft puffs, no sparks: owner's rule for the default look)
            Style shine = new Style(Style.ColorMode.SOLID, GOLD_LIGHT, GOLD_LIGHT, Style.Shape.PUFF, style.size(), 1f, 1f, 1f,
                style.sound(), 1f, 1f, Style.Pose.NONE);
            CLOUDS.add(new Cloud(who, shine, new Random(seed ^ 0x6A09E667L), 1, hue, own, 1.2f, 0, new int[]{ticks / 2 + 2}));
        }
        if (cloud != null && linger > 0 && radius > 0 && ClientConfig.LINGER.on()) {
            LINGERS.add(new Linger(cloud, radius, style, new Random(seed ^ 0x5DEECE66DL), hue, own, new int[]{linger}));
        }
    }

    /**
     * Fart Together: {@code count} players tooted together. The chorus (one more layered sound per player, up to
     * three), a ring of colourful puffs bursting out of the centre and the big shared cloud lingering there.
     */
    public static void combo(Minecraft mc, int count, long seed, Vec3 pos, float radius, int linger, int color) {
        if (mc.level == null) return;
        Random rnd = new Random(seed);
        float volume = ClientConfig.VOLUME.floatValue();
        if (volume > 0) {
            for (int i = 0; i < Math.min(3, count); i++) {
                String name = ClientConfig.STREAMER_SAFE.on() ? Sounds.PUFF : CHORUS[i];
                SoundEvent event = SoundEvent.createVariableRangeEvent(FartTogether.id("toot." + name));
                mc.level.playLocalSound(pos.x, pos.y, pos.z, event, SoundSource.PLAYERS, volume, 0.85f + 0.12f * i, false);
            }
        }
        if (TestHooks.enabled()) {
            FartTogether.LOGGER.info("test: combo count={} radius={} linger={} color={}", count, radius, linger, Integer.toHexString(color));
        }
        // one big merged cloud spanning the players: many large soft puffs filling a flattened dome around the centre,
        // rising slowly and fading over three to four seconds (several times the volume of a single toot)
        // the dome hugs the players: about the server's area, a little tighter so a camera next to them stays outside
        double r = Math.min(5.0, Math.max(2.0, (radius > 0 ? radius : 2.5 + count) * 0.7));
        // the players' colours, pulled towards the gas green so the shared cloud still reads as gas
        int gas = Colors.lerp(color, Style.DEFAULT_COLOR, 0.45f);
        int puffs = Math.round(Math.min(160, 30 + 22 * count) * ClientConfig.PARTICLES.floatValue());
        for (int i = 0; i < puffs; i++) {
            double x, y, z;
            do {
                x = rnd.nextDouble() * 2 - 1;
                y = rnd.nextDouble();
                z = rnd.nextDouble() * 2 - 1;
            } while (x * x + y * y + z * z > 1);
            int tint = Colors.lerp(gas, 0xFFFFFF, rnd.nextFloat() * 0.18f);
            bigPuff(mc, pos.x + x * r, pos.y - 0.3 + y * r * 0.6, pos.z + z * r,
                x * 0.01, 0.006 + rnd.nextDouble() * 0.01, z * 0.01, tint, 1.3f + rnd.nextFloat() * 0.7f, rnd,
                70 + rnd.nextInt(14) + Math.min(40, linger / 4));
        }
    }

    /** Clouds still lingering (tests). */
    public static int lingering() {
        return LINGERS.size();
    }

    /** Every client tick: clouds puff out their next particles; finished ones are dropped. */
    public static void tick(Minecraft mc) {
        TootPose.tick();
        if (mc.level == null || mc.isPaused()) {
            if (mc.level == null) {
                CLOUDS.clear();
                LINGERS.clear();
            }
            return;
        }
        for (Iterator<Linger> it = LINGERS.iterator(); it.hasNext(); ) {
            Linger l = it.next();
            if (l.left()[0]-- <= 0) {
                it.remove();
                continue;
            }
            if (l.left()[0] % 2 == 0) linger(mc, l);
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
        if ((c.flags() & JET_BLAST) != 0) {
            jet(mc, c);
            return;
        }
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
        ParticleOptions type = shape(s.shape());
        if ((c.flags() & Proto.WATER) != 0) {
            // under water (a few big bubbles below, then this one): bubbles that rise
            type = ParticleTypes.BUBBLE;
            dy = 0.04 + r.nextDouble() * 0.05;
            for (int i = 0; i < 2; i++) {
                Particle bubble = mc.particleEngine.createParticle(type, x + (r.nextDouble() - 0.5) * 0.3, y, z + (r.nextDouble() - 0.5) * 0.3,
                    dx * 0.5, dy, dz * 0.5);
                if (bubble != null) bubble.scale(2.2f * s.size());
            }
        }
        if (type == ParticleTypes.NOTE) {
            // the note particle takes its colour (0..1 along the note palette) from the x speed
            dx = r.nextDouble();
            dy = 0;
            dz = 0;
        }
        if (type == PUFF) {
            // a soft puff drifts slowly back and up and fades: no fast specks
            puff(mc, x, y, z, bx * 0.025 + (r.nextDouble() - 0.5) * 0.01 * s.spread(), 0.008 + r.nextDouble() * 0.01,
                bz * 0.025 + (r.nextDouble() - 0.5) * 0.01 * s.spread(), color(s, c.hue(), r), s.size() * c.size(), r);
            return;
        }
        Particle particle = mc.particleEngine.createParticle(type, x, y, z, dx, dy, dz);
        if (particle == null) return;
        if (type != ParticleTypes.NOTE && type != ParticleTypes.HEART) tint(particle, color(s, c.hue(), r));
        particle.scale(s.size() * c.size());
    }

    /** A combo puff: like {@link #puff} but big and with its own lifetime. */
    private static void bigPuff(Minecraft mc, double x, double y, double z, double dx, double dy, double dz, int rgb,
                                float size, Random r, int life) {
        if (mc.player != null && mc.options.getCameraType().isFirstPerson()
            && mc.player.getEyePosition().distanceToSqr(x, y, z) < 2.2 * 2.2) return;
        Particle particle = mc.particleEngine.createParticle(PUFF, x, y, z, dx, dy, dz);
        if (particle == null) return;
        tint(particle, rgb);
        particle.scale(size);
        particle.setLifetime(life);
    }

    /**
     * The default look of gas: the campfire's soft smoke puff, tinted with the style colour and shrunk from campfire
     * size, rising slowly and fading out over three seconds (the vanilla cloud particle read as pixel confetti).
     */
    private static void puff(Minecraft mc, double x, double y, double z, double dx, double dy, double dz, int rgb,
                             float size, Random r) {
        // a big soft puff right at the eyes fills the whole first-person view with one blurry blob: leave those out
        if (mc.player != null && mc.options.getCameraType().isFirstPerson()
            && mc.player.getEyePosition().distanceToSqr(x, y, z) < 1.7 * 1.7) return;
        Particle particle = mc.particleEngine.createParticle(PUFF, x, y, z, dx, dy, dz);
        if (particle == null) return;
        tint(particle, rgb);
        particle.scale(1.1f * Math.max(0.3f, size) * (0.8f + r.nextFloat() * 0.4f));
        particle.setLifetime(60 + r.nextInt(16));
    }

    /** The jet jump's blast: fast puffs straight down from the feet, spreading as they go. */
    private static void jet(Minecraft mc, Cloud c) {
        Player p = c.who();
        Random r = c.rnd();
        for (int i = 0; i < c.perTick(); i++) {
            double x = p.getX() + (r.nextDouble() - 0.5) * 0.3;
            double z = p.getZ() + (r.nextDouble() - 0.5) * 0.3;
            puff(mc, x, p.getY() + 0.1, z, (r.nextDouble() - 0.5) * 0.03, -0.08 - r.nextDouble() * 0.05,
                (r.nextDouble() - 0.5) * 0.03, color(c.style(), c.hue(), r), 1.1f * c.size(), r);
        }
    }

    /** A few slow particles somewhere inside the lingering cloud; thinner as it runs out. */
    private static void linger(Minecraft mc, Linger l) {
        Random r = l.rnd();
        Style s = l.style();
        double area = l.radius() * l.radius();
        int n = Math.max(1, (int) Math.round(area * 0.3 * Math.min(2f, s.amount()) * ClientConfig.PARTICLES.floatValue()));
        boolean soft = s.shape() == Style.Shape.PUFF || s.shape() == Style.Shape.NOTES || s.shape() == Style.Shape.HEARTS;
        // soft puffs live long and are big: far fewer of them keep the cloud as dense
        if (soft) n = r.nextInt(6) < Math.min(6, n) ? 1 : 0;
        if (l.left()[0] < 30) n = Math.max(1, n * l.left()[0] / 30);
        for (int i = 0; i < n; i++) {
            // a point in the sphere, flattened a little: gas sinks
            double x, y, z;
            do {
                x = r.nextDouble() * 2 - 1;
                y = r.nextDouble() * 2 - 1;
                z = r.nextDouble() * 2 - 1;
            } while (x * x + y * y + z * z > 1);
            // flattened and kept above the ground under the cloud: a particle inside a block is drawn black
            Vec3 p = l.pos().add(x * l.radius(), (y * 0.5 + 0.2) * l.radius() * 0.6, z * l.radius());
            if (soft) {
                puff(mc, p.x, p.y, p.z, (r.nextDouble() - 0.5) * 0.006, 0.003 + r.nextDouble() * 0.004,
                    (r.nextDouble() - 0.5) * 0.006, color(s, l.hue(), r), Math.max(1f, Math.min(2f, s.size())) * 0.7f, r);
                continue;
            }
            ParticleOptions type = shape(s.shape());
            Particle particle = mc.particleEngine.createParticle(type, p.x, p.y, p.z,
                (r.nextDouble() - 0.5) * 0.01, 0.002 + r.nextDouble() * 0.006, (r.nextDouble() - 0.5) * 0.01);
            if (particle == null) continue;
            tint(particle, color(s, l.hue(), r));
            particle.scale(Math.max(0.6f, Math.min(2f, s.size())));
        }
    }

    private static ParticleOptions shape(Style.Shape shape) {
        return switch (shape) {
            case PUFF -> PUFF;
            case BUBBLES -> ParticleTypes.BUBBLE_POP;
            case SPARKS -> ParticleTypes.ELECTRIC_SPARK;
            case LEAVES -> ParticleTypes.CHERRY_LEAVES;
            case NOTES -> ParticleTypes.NOTE;
            case HEARTS -> ParticleTypes.HEART;
            case SNOW -> ParticleTypes.SNOWFLAKE;
        };
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
