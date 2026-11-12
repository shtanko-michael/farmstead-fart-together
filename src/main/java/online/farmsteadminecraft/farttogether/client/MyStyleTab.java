package online.farmsteadminecraft.farttogether.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import online.farmsteadminecraft.farttogether.config.ConfigScreen;
import online.farmsteadminecraft.farttogether.style.Colors;
import online.farmsteadminecraft.farttogether.style.Sounds;

/**
 * The "My style" tab of the settings: a live preview on the left ({@link StylePreview}) and the four choices people
 * change most on the right (colour, particles, sound, pose), each a button that steps to the next option, plus
 * "Play". Every option is also in the Style tab with the fine sliders; "Done" saves both.
 */
public final class MyStyleTab implements ConfigScreen.TabContent {
    private static final String C = "config.farmstead_fart_together.";
    private static StylePreview last;

    @Override
    public void init(ConfigScreen screen, Consumer<AbstractWidget> add, int left, int top, int width, int height) {
        int previewW = Math.max(110, Math.min(200, width * 45 / 100));
        int controlW = Math.min(200, width - previewW - 20);
        int gap = Math.max(10, (width - previewW - controlW) / 3);
        int px = left + gap, cx = px + previewW + gap;
        StylePreview preview = new StylePreview(px, top, previewW, height);
        last = preview;
        add.accept(preview);

        int y = top + 4;
        add.accept(label(screen, cx, y, controlW, Component.translatableWithFallback(C + "mystyle.hint",
            "Your toot as friends see it")));
        y += 16;
        add.accept(step(cx, y, controlW, () -> labelled("style.color", "Colour", colourName()), MyStyleTab::nextColour,
            C + "style.color.tooltip"));
        y += 24;
        add.accept(step(cx, y, controlW, () -> labelled("style.particle", "Particles", option("style.particle", ClientConfig.PARTICLE.get())),
            () -> ClientConfig.PARTICLE.set(ClientConfig.PARTICLE.next()), C + "style.particle.tooltip"));
        y += 24;
        add.accept(step(cx, y, controlW, () -> labelled("style.sound", "Sound", soundName()), MyStyleTab::nextSound,
            C + "style.sound.tooltip"));
        y += 24;
        add.accept(step(cx, y, controlW, () -> labelled("style.pose", "Pose", option("style.pose", ClientConfig.POSE.get())),
            () -> ClientConfig.POSE.set(ClientConfig.POSE.next()), C + "style.pose.tooltip"));
        y += 30;
        Button play = Button.builder(Component.translatableWithFallback(C + "mystyle.play", "Play"), b -> preview.play())
            .bounds(cx, y, controlW, 20).build();
        play.setTooltip(Tooltip.create(Component.translatableWithFallback(C + "mystyle.play.tooltip",
            "Plays your toot here, for you only. Others see it after Done.")));
        add.accept(play);
    }

    /** For test hooks: press "Play" on the open preview. */
    public static void playOpen() {
        if (last != null) last.play();
    }

    private static Component labelled(String key, String fallback, Component value) {
        return Component.translatableWithFallback(C + key, fallback).copy().append(": ").append(value);
    }

    private static Component option(String path, Enum<?> e) {
        String name = e.name().toLowerCase(Locale.ROOT);
        return Component.translatableWithFallback(C + path + "." + name, Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    /** The colour presets, then random and rainbow (the server may turn rainbow into a plain colour). */
    private static List<String> colours() {
        List<String> out = new ArrayList<>(Colors.PRESETS.keySet());
        out.add("random");
        out.add("rainbow");
        return out;
    }

    private static Component colourName() {
        String c = ClientConfig.COLOR.get().trim().toLowerCase(Locale.ROOT);
        if (colours().contains(c)) return Component.translatableWithFallback(C + "color." + c, c);
        return Component.literal(c);
    }

    private static void nextColour() {
        List<String> all = colours();
        String c = ClientConfig.COLOR.get().trim().toLowerCase(Locale.ROOT);
        ClientConfig.COLOR.set(all.get((all.indexOf(c) + 1) % all.size()));
    }

    private static Component soundName() {
        String s = ClientConfig.SOUND.get().trim().toLowerCase(Locale.ROOT);
        if (Sounds.RANDOM.equals(s)) return Component.translatableWithFallback(C + "sound.random", "Random");
        return Component.translatableWithFallback("subtitles.farmstead_fart_together.toot." + s, s);
    }

    private static void nextSound() {
        List<String> all = new ArrayList<>();
        all.add(Sounds.RANDOM);
        all.addAll(Sounds.ALL);
        String s = ClientConfig.SOUND.get().trim().toLowerCase(Locale.ROOT);
        ClientConfig.SOUND.set(all.get((all.indexOf(s) + 1) % all.size()));
    }

    private static Button step(int x, int y, int w, Supplier<Component> label, Runnable next, String tooltipKey) {
        Button[] self = new Button[1];
        self[0] = Button.builder(label.get(), b -> {
            next.run();
            self[0].setMessage(label.get());
            if (last != null) last.play();
        }).bounds(x, y, w, 20).build();
        self[0].setTooltip(Tooltip.create(Component.translatable(tooltipKey)));
        return self[0];
    }

    private static StringWidget label(ConfigScreen screen, int x, int y, int w, Component text) {
        int tw = Math.min(w, screen.font().width(text));
        return new StringWidget(x + (w - tw) / 2, y, tw, 12, text, screen.font());
    }
}
