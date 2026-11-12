package online.farmsteadminecraft.farttogether.client;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client-side {@code /farttogether} command, the same tree on every loader (each loader passes its own command
 * source type): {@code reload} rereads the client file, {@code config} opens the settings screen. The gameplay
 * command {@code /fart} will be a server command.
 */
public final class FartTogetherCommands {
    public static final String ROOT = "farttogether";

    private FartTogetherCommands() {
    }

    public static <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal(ROOT)
            .then(LiteralArgumentBuilder.<S>literal("reload").executes(c -> reload()))
            .then(LiteralArgumentBuilder.<S>literal("config").executes(c -> {
                FartTogetherClient.openConfigNextTick();
                return 1;
            }));
    }

    private static int reload() {
        List<String> problems = ClientConfig.load();
        if (problems.isEmpty()) {
            say(Component.translatable("farmstead_fart_together.command.reloaded").withStyle(ChatFormatting.GREEN));
            return 1;
        }
        say(Component.translatable("farmstead_fart_together.command.problems", problems.size()).withStyle(ChatFormatting.GOLD));
        for (String p : problems) say(Component.literal(" - " + p).withStyle(ChatFormatting.GRAY));
        return 0;
    }

    private static void say(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        //? if >=1.21.2 && <26.1 {
        /*mc.player.displayClientMessage(message, false);
        *///?} else
        mc.player.sendSystemMessage(message);
    }
}
