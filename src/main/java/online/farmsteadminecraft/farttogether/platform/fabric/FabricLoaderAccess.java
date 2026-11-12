package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;

final class FabricLoaderAccess implements ModLoaderAccess {
    private final FabricLoader loader = FabricLoader.getInstance();

    @Override
    public boolean isClient() {
        return loader.getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServer() {
        return loader.getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public boolean isModLoaded(String id) {
        return loader.isModLoaded(id);
    }

    @Override
    public java.nio.file.Path configDir() {
        return loader.getConfigDir();
    }

    /**
     * fabric-permissions-api (bundled by LuckPerms and most permission mods) if one is installed, found by reflection
     * so the mod does not need it; otherwise the node's default.
     */
    @Override
    public boolean permission(net.minecraft.server.level.ServerPlayer player, online.farmsteadminecraft.farttogether.server.Perm perm) {
        boolean def = perm.byDefault(player);
        if (CHECK == null) return def;
        try {
            return (Boolean) CHECK.invoke(null, player, perm.node(), def);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return def;
        }
    }

    /** fabric-permissions-api's Options (LuckPerms meta), by reflection like the permission check. */
    @Override
    public String meta(net.minecraft.server.level.ServerPlayer player, online.farmsteadminecraft.farttogether.server.Meta meta) {
        if (OPTION == null) return null;
        try {
            Object v = OPTION.invoke(null, player, meta.node());
            return v instanceof java.util.Optional<?> o ? (String) o.orElse(null) : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static final java.lang.reflect.Method OPTION = findOption();

    private static java.lang.reflect.Method findOption() {
        try {
            Class<?> c = Class.forName("me.lucko.fabric.api.permissions.v0.Options");
            return c.getMethod("get", net.minecraft.world.entity.Entity.class, String.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static final java.lang.reflect.Method CHECK = findCheck();

    private static java.lang.reflect.Method findCheck() {
        try {
            Class<?> c = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
            return c.getMethod("check", net.minecraft.world.entity.Entity.class, String.class, boolean.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    @Override
    public String loaderName() {
        return "fabric";
    }
}
