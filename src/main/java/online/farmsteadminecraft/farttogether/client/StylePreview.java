package online.farmsteadminecraft.farttogether.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.style.Colors;
import online.farmsteadminecraft.farttogether.style.Sounds;
import online.farmsteadminecraft.farttogether.style.Style;
//? if <26.1
import net.minecraft.client.gui.GuiGraphics;

/**
 * The "My style" preview: your own character, slowly turning, with a little cloud in your current (unsaved) style
 * puffing out behind it every few seconds; {@link #play} adds the pose and the sound. The cloud is drawn as flat
 * squares with {@code fill}, the model with the vanilla inventory helper; both exist on every version we ship.
 */
public final class StylePreview extends AbstractWidget {
    private static final long AUTO_PUFF_NANOS = 3_200_000_000L;

    private record Puff(float x, float y, float vx, float vy, float size, int color, long born, long life) {}

    private final List<Puff> puffs = new ArrayList<>();
    private final Random rnd = new Random();
    private long lastPuff;
    private final long start = System.nanoTime();

    public StylePreview(int x, int y, int w, int h) {
        super(x, y, w, h, Component.translatableWithFallback("config.farmstead_fart_together.preview", "Preview"));
        active = false;
    }

    /** "Play": the full toot of the current style, for your ears only. */
    public void play() {
        Minecraft mc = Minecraft.getInstance();
        Style s = ClientConfig.style();
        String sound = ClientConfig.STREAMER_SAFE.on() ? Sounds.PUFF : Sounds.choose(s.sound(), 1, rnd);
        SoundEvent event = SoundEvent.createVariableRangeEvent(FartTogether.id("toot." + sound));
        float volume = s.volume() * ClientConfig.VOLUME.floatValue();
        if (volume > 0) mc.getSoundManager().play(SimpleSoundInstance.forUI(event, s.pitch(), Math.min(1f, volume)));
        if (mc.player != null && s.pose() != Style.Pose.NONE) TootPose.start(mc.player, s.pose());
        puff(s, 2.2f);
    }

    private void puff(Style s, float strength) {
        long now = System.nanoTime();
        lastPuff = now;
        int n = Math.max(3, Math.min(60, Math.round(18 * s.amount() * strength)));
        float hue = rnd.nextFloat();
        // the model faces right of the viewer, so its back (and the cloud) is on the left
        int cx = getX() + width / 2 - width / 12, hip = getY() + height * 64 / 100;
        for (int i = 0; i < n; i++) {
            float speed = (30 + rnd.nextFloat() * 45) * s.spread();
            float dir = (float) Math.toRadians(150 + rnd.nextFloat() * 70);
            puffs.add(new Puff(cx + (rnd.nextFloat() - 0.5f) * 8, hip + (rnd.nextFloat() - 0.5f) * 8,
                (float) Math.cos(dir) * speed, (float) Math.sin(dir) * speed * 0.5f - 8,
                (3f + rnd.nextFloat() * 3f) * Math.max(0.5f, s.size()), color(s, hue),
                now + (long) (rnd.nextFloat() * 350_000_000L), (long) ((1.2f + rnd.nextFloat() * 1.0f) * s.duration() * 1e9)));
        }
    }

    private int color(Style s, float hue) {
        int base = switch (s.mode()) {
            case SOLID -> s.color();
            case GRADIENT -> Colors.lerp(s.color(), s.color2(), rnd.nextFloat());
            case RANDOM -> Colors.hue(hue);
            case RAINBOW -> Colors.hue(hue + rnd.nextFloat());
        };
        if (s.shape() == Style.Shape.HEARTS) base = 0xFF5577;
        if (s.shape() == Style.Shape.SNOW) base = Colors.lerp(base, 0xFFFFFF, 0.6f);
        return base & 0xFFFFFF;
    }

    //? if >=26.1 {
    /*@Override
    protected void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
    *///?} else {
    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partial) {
    //?}
        Minecraft mc = Minecraft.getInstance();
        int x0 = getX(), y0 = getY(), x1 = x0 + width, y1 = y0 + height;
        g.fill(x0, y0, x1, y1, 0x90101418);
        g.fill(x0, y1 - 1, x1, y1, 0x60FFFFFF);
        long now = System.nanoTime();
        Style s = ClientConfig.style();
        if (now - lastPuff > AUTO_PUFF_NANOS) puff(s, 1f);

        // the cloud behind the model: squares that drift out, rise a little and fade
        for (Iterator<Puff> it = puffs.iterator(); it.hasNext(); ) {
            Puff p = it.next();
            if (now < p.born()) continue;
            float t = (now - p.born()) / (float) p.life();
            if (t >= 1) {
                it.remove();
                continue;
            }
            float secs = (now - p.born()) / 1e9f;
            float drag = 1 - Math.min(0.85f, secs * 0.9f);
            float px = p.x() + p.vx() * secs * drag;
            float py = p.y() + p.vy() * secs * drag - secs * 4;
            float size = p.size() * (1 + t * 1.2f);
            int alpha = (int) (200 * (1 - t) * (1 - t)) + 20;
            int r = Math.max(1, Math.round(size));
            int ix = Math.round(px), iy = Math.round(py);
            if (ix - r < x0 || ix + r > x1 || iy - r < y0 || iy + r > y1) continue;
            int c = (alpha << 24) | p.color();
            // a rounded blob: a cross of two rectangles
            g.fill(ix - r, iy - r / 2, ix + r, iy + r / 2 + 1, c);
            g.fill(ix - r / 2, iy - r, ix + r / 2 + 1, iy + r, c);
        }

        if (mc.player != null) {
            float secs = (now - start) / 1e9f;
            // three-quarters, swinging a little: the helper turns the body by atan(offset) * 20 degrees
            float sway = (float) Math.tan(1.0 + 0.4 * Math.sin(secs * 0.8));
            int size = Math.max(20, Math.min(width * 50 / 100, height * 40 / 100));
            // the vanilla helper turns the model towards a "mouse": a point that swings left and right
            float mx = (x0 + x1) / 2f + sway * 40f, my = (y0 + y1) / 2f - 10f;
            //? if >=26.1 {
            /*InventoryScreen.extractEntityInInventoryFollowsMouse(g, x0, y0, x1, y1 - 6, size, 0.0625f, mx, my, mc.player);
            *///?} else if >=1.20.2 {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x0, y0, x1, y1 - 6, size, 0.0625f, mx, my, mc.player);
            //?} else
            /*InventoryScreen.renderEntityInInventoryFollowsMouse(g, x0 + width / 2, y1 - 12, size, -sway * 40f, 10f, mc.player);*/
        } else {
            Component hint = Component.translatableWithFallback("config.farmstead_fart_together.preview.noWorld", "Join a world to see yourself");
            int w = mc.font.width(hint);
            //? if >=26.1 {
            /*g.text(mc.font, hint, x0 + (width - w) / 2, y1 - 20, 0xFFB0B0B0);
            *///?} else
            g.drawString(mc.font, hint, x0 + (width - w) / 2, y1 - 20, 0xFFB0B0B0);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput out) {
        defaultButtonNarrationText(out);
    }
}
