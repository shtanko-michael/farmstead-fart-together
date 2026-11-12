package online.farmsteadminecraft.farttogether.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import online.farmsteadminecraft.farttogether.style.Style;

/**
 * The toot pose: a short keyframed animation laid over the humanoid model after vanilla has posed it (see
 * {@code mixin.HumanoidModelMixin}). Timed in the entity's own ticks, so it is smooth with partial ticks and the
 * same on every screen.
 */
public final class TootPose {
    /** Length of the pose in ticks: lean in, hold with a little shake, straighten up. */
    public static final float DURATION = 24f;
    private static final long FORGET_NANOS = 3_000_000_000L;

    private record Entry(float startAge, Style.Pose pose, long startNanos) {}

    private static final Map<Integer, Entry> ACTIVE = new HashMap<>();

    private TootPose() {
    }

    static void start(Player who, Style.Pose pose) {
        ACTIVE.put(who.getId(), new Entry(who.tickCount, pose, System.nanoTime()));
    }

    static void tick() {
        long now = System.nanoTime();
        for (Iterator<Entry> it = ACTIVE.values().iterator(); it.hasNext(); ) {
            if (now - it.next().startNanos() > FORGET_NANOS) it.remove();
        }
    }

    /** Progress 0..1 of the pose of entity {@code id} at {@code ageInTicks}, or -1 when it is not tooting. */
    public static float progress(int id, float ageInTicks) {
        if (!ClientConfig.SHOW_POSES.on()) return -1;
        Entry e = ACTIVE.get(id);
        if (e == null) return -1;
        float t = (ageInTicks - e.startAge()) / DURATION;
        return t < 0 || t > 1 ? -1 : t;
    }

    /** The pose kind of entity {@code id} (only meaningful while {@link #progress} is not -1). */
    public static int pose(int id) {
        Entry e = ACTIVE.get(id);
        return e == null ? Style.Pose.NONE.ordinal() : e.pose().ordinal();
    }

    /** Lays the pose over the model. {@code crouching}: vanilla already lowered the body, so only the arms move. */
    public static void apply(HumanoidModel<?> m, float t, int poseOrdinal, boolean crouching, float age) {
        if (t < 0) return;
        Style.Pose pose = Style.Pose.values()[Math.max(0, Math.min(Style.Pose.values().length - 1, poseOrdinal))];
        if (pose == Style.Pose.NONE) return;
        float w = t < 0.18f ? ease(t / 0.18f) : t > 0.7f ? ease(1 - (t - 0.7f) / 0.3f) : 1f;
        float shake = t > 0.16f && t < 0.6f ? Mth.sin(age * 2.6f) * 0.06f * w : 0f;
        if (pose == Style.Pose.CLASSIC) {
            if (!crouching) {
                // lean forward the way sneaking does
                m.body.xRot = lerp(m.body.xRot, 0.5f, w);
                m.body.y = lerp(m.body.y, 3.2f, w);
                m.head.y = lerp(m.head.y, 4.2f, w);
                m.rightArm.y = lerp(m.rightArm.y, 5.2f, w);
                m.leftArm.y = lerp(m.leftArm.y, 5.2f, w);
                m.rightLeg.z = lerp(m.rightLeg.z, 4.0f, w);
                m.leftLeg.z = lerp(m.leftLeg.z, 4.0f, w);
            }
            // hands to the knees, slightly apart
            m.rightArm.xRot = lerp(m.rightArm.xRot, -0.45f, w);
            m.leftArm.xRot = lerp(m.leftArm.xRot, -0.45f, w);
            m.rightArm.zRot = lerp(m.rightArm.zRot, 0.12f, w);
            m.leftArm.zRot = lerp(m.leftArm.zRot, -0.12f, w);
            m.rightArm.yRot = lerp(m.rightArm.yRot, 0f, w);
            m.leftArm.yRot = lerp(m.leftArm.yRot, 0f, w);
            // a look back over the shoulder
            m.head.yRot = lerp(m.head.yRot, 0.55f, w * 0.8f);
        } else if (pose == Style.Pose.HERO) {
            // chest out, fists on the hips, chin up
            m.body.xRot = lerp(m.body.xRot, -0.12f, w);
            m.rightArm.xRot = lerp(m.rightArm.xRot, -0.15f, w);
            m.leftArm.xRot = lerp(m.leftArm.xRot, -0.15f, w);
            m.rightArm.zRot = lerp(m.rightArm.zRot, 0.75f, w);
            m.leftArm.zRot = lerp(m.leftArm.zRot, -0.75f, w);
            m.head.xRot = lerp(m.head.xRot, -0.3f, w);
        }
        m.body.zRot += shake;
        m.head.zRot += shake * 0.6f;
        m.rightLeg.zRot -= shake * 0.4f;
        m.leftLeg.zRot -= shake * 0.4f;
    }

    private static float lerp(float a, float b, float w) {
        return a + (b - a) * w;
    }

    private static float ease(float x) {
        float c = Math.max(0, Math.min(1, x));
        return c * c * (3 - 2 * c);
    }
}
