package online.farmsteadminecraft.farttogether.server;

import net.minecraft.server.level.ServerPlayer;

/**
 * Permission nodes ({@code farmstead_fart_together.<name>}, LuckPerms or any permission mod of the loader). Without
 * one, the default applies: {@link #def} for everybody, or operators only when {@link #op} is set.
 */
public enum Perm {
    USE("use", true, false),
    STYLE_CUSTOM("style.custom", true, false),
    STYLE_RAINBOW("style.rainbow", false, false),
    STYLE_HUGE("style.huge", false, false),
    STYLE_SOUNDS_RARE("style.sounds.rare", false, false),
    /** Cloud effects (when the server sets any) do not reach this player. */
    CLOUD_BYPASS("cloud.bypass", false, false),
    COMMAND_INFO("command.info", true, false),
    COMMAND_RELOAD("command.reload", false, true),
    COMMAND_CHARGE("command.charge", false, true),
    COMMAND_TOGGLE("command.toggle", false, true);

    public final String name;
    public final boolean def;
    public final boolean op;

    Perm(String name, boolean def, boolean op) {
        this.name = name;
        this.def = def;
        this.op = op;
    }

    /** The full node, e.g. {@code farmstead_fart_together.style.rainbow}. */
    public String node() {
        return online.farmsteadminecraft.farttogether.FartTogether.MOD_ID + "." + name;
    }

    /** What a player gets when no permission mod says otherwise. */
    public boolean byDefault(ServerPlayer player) {
        return op ? player != null && isOp(player) : def;
    }

    /** Operator level 2 (game masters), the level vanilla asks for /gamemode and /give. */
    public static boolean isOp(ServerPlayer player) {
        //? if >=1.21.11 {
        /*return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        *///?} else
        return player.hasPermissions(2);
    }
}
