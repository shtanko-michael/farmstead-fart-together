package online.farmsteadminecraft.farttogether.server;

/**
 * Permission nodes ({@code farmstead_fart_together.<name>}, LuckPerms or any permission mod of the loader). Without
 * one, the default applies to everybody.
 */
public enum Perm {
    USE("use", true),
    STYLE_CUSTOM("style.custom", true),
    STYLE_RAINBOW("style.rainbow", false),
    STYLE_HUGE("style.huge", false),
    STYLE_SOUNDS_RARE("style.sounds.rare", false);

    public final String name;
    public final boolean def;

    Perm(String name, boolean def) {
        this.name = name;
        this.def = def;
    }

    /** The full node, e.g. {@code farmstead_fart_together.style.rainbow}. */
    public String node() {
        return online.farmsteadminecraft.farttogether.FartTogether.MOD_ID + "." + name;
    }
}
