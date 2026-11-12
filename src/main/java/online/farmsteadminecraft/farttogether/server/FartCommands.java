package online.farmsteadminecraft.farttogether.server;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import online.farmsteadminecraft.farttogether.content.Content;

/**
 * {@code /fart}: {@code info} for everyone, {@code reload}, {@code charge} and {@code toggle} for admins. Each
 * branch asks its own permission node ({@code farmstead_fart_together.command.*}); the console may do everything.
 */
public final class FartCommands {
    private static final String K = "command.farmstead_fart_together.";

    private FartCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("fart")
            .then(Commands.literal("info").requires(s -> can(s, Perm.COMMAND_INFO)).executes(FartCommands::info))
            .then(Commands.literal("reload").requires(s -> can(s, Perm.COMMAND_RELOAD)).executes(FartCommands::reload))
            .then(Commands.literal("charge").requires(s -> can(s, Perm.COMMAND_CHARGE))
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.literal("full").executes(c -> charge(c, -1)))
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 10000))
                        .executes(c -> charge(c, IntegerArgumentType.getInteger(c, "amount"))))))
            .then(Commands.literal("toggle").requires(s -> can(s, Perm.COMMAND_TOGGLE))
                .then(Commands.argument("targets", EntityArgument.players()).executes(FartCommands::toggle)));
    }

    private static boolean can(CommandSourceStack source, Perm perm) {
        return !(source.getEntity() instanceof ServerPlayer player) || TootServer.allowed(player, perm);
    }

    private static int info(CommandContext<CommandSourceStack> c) {
        CommandSourceStack s = c.getSource();
        if (s.getEntity() instanceof ServerPlayer p) {
            int charge = (int) Gas.charge(p.getUUID());
            boolean may = TootServer.allowed(p, Perm.USE) && !Gas.disabled(p.getUUID());
            ok(s, tr("info.gas", "Your gas: %s / %s (a toot needs %s)", charge, Gas.max(p),
                ServerConfig.CHARGE_REQUIRED.on() ? ServerConfig.CHARGE_MIN.value() : 0));
            ok(s, tr("info.player", "Cooldown %s s, cloud radius %s, toots %s, your client %s",
                fmt(TootServer.meta(p, Meta.COOLDOWN)), fmt(TootServer.meta(p, Meta.CLOUD_RADIUS)),
                tr(may ? "info.on" : "info.off", may ? "on" : "off"),
                tr(TootServer.isModded(p.getUUID()) ? "info.modded" : "info.vanilla",
                    TootServer.isModded(p.getUUID()) ? "has the mod" : "has no mod (nothing to see)")));
        }
        ok(s, tr("info.server", "Server: gas %s, cloud %s s, scares mobs %s, effects %s, items %s",
            ServerConfig.CHARGE_REQUIRED.on() ? "required" : "free", fmt(ServerConfig.CLOUD_SECONDS.value()),
            ServerConfig.SCARE.on() ? "yes" : "no",
            ServerConfig.EFFECTS.get().isEmpty() ? "none" : String.join(", ", ServerConfig.EFFECTS.get()),
            Content.enabled() ? "yes" : "no"));
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        CommandSourceStack s = c.getSource();
        List<String> problems = ServerConfig.load();
        GasFoods.invalidate();
        int foods = GasFoods.size(s.getServer());
        TootServer.resendAll(s.getServer());
        ok(s, tr("reload", "Fart Together server settings reloaded: %s problems, %s food entries", problems.size(), foods));
        for (String p : problems) s.sendFailure(Component.literal(p));
        return 1;
    }

    private static int charge(CommandContext<CommandSourceStack> c, int amount) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(c, "targets");
        for (ServerPlayer p : targets) {
            Gas.set(p, amount < 0 ? Gas.max(p) : amount);
            TootServer.sendCharge(p);
        }
        String who = targets.size() == 1 ? targets.iterator().next().getName().getString() : String.valueOf(targets.size());
        ok(c.getSource(), tr("charge", "Gas of %s set to %s", who, amount < 0 ? "full" : amount));
        return targets.size();
    }

    private static int toggle(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(c, "targets");
        for (ServerPlayer p : targets) {
            boolean on = Gas.toggle(p.getUUID());
            TootServer.sendCharge(p);
            ok(c.getSource(), tr(on ? "toggle.on" : "toggle.off", on ? "%s may toot again" : "%s can no longer toot",
                p.getName().getString()));
        }
        return targets.size();
    }

    private static void ok(CommandSourceStack s, Component text) {
        s.sendSuccess(() -> text, false);
    }

    private static Component tr(String key, String fallback, Object... args) {
        return Component.translatableWithFallback(K + key, fallback, args);
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.ROOT, "%.1f", v);
    }
}
