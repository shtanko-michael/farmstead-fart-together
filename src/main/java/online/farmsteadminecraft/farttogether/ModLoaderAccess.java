package online.farmsteadminecraft.farttogether;

/**
 * Loader-specific services. Each loader entrypoint installs its implementation
 * into {@link #INSTANCE} before any common code runs.
 */
public interface ModLoaderAccess {
    Holder HOLDER = new Holder();

    static ModLoaderAccess get() {
        return HOLDER.access;
    }

    boolean isClient();

    boolean isServer();

    boolean isModLoaded(String id);

    String loaderName();

    /**
     * The player's permission node (LuckPerms or another permission mod of the loader); without one, the node's
     * default. NeoForge/Forge use their PermissionAPI, Fabric the fabric-permissions-api if a mod provides it.
     */
    boolean permission(net.minecraft.server.level.ServerPlayer player, online.farmsteadminecraft.farttogether.server.Perm perm);

    /** The game's config folder ({@code .minecraft/config}). */
    java.nio.file.Path configDir();

    final class Holder {
        private ModLoaderAccess access;

        public void install(ModLoaderAccess access) {
            if (this.access != null) throw new IllegalStateException("ModLoaderAccess already installed");
            this.access = access;
        }
    }
}
