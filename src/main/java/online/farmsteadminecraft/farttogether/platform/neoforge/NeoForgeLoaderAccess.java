package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.server.Meta;
import online.farmsteadminecraft.farttogether.server.Perm;

final class NeoForgeLoaderAccess implements ModLoaderAccess {
    private static Dist dist() {
        //? if >=1.21.9 {
        /*return FMLEnvironment.getDist();
        *///?} else
        return FMLEnvironment.dist;
    }

    @Override
    public boolean isClient() {
        return dist().isClient();
    }

    @Override
    public boolean isServer() {
        return dist().isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public java.nio.file.Path configDir() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    private static final java.util.Map<Perm, PermissionNode<Boolean>> NODES = new java.util.EnumMap<>(Perm.class);
    private static final java.util.Map<Meta, PermissionNode<String>> META = new java.util.EnumMap<>(Meta.class);

    static {
        for (Perm perm : Perm.values()) {
            NODES.put(perm, new PermissionNode<>(FartTogether.MOD_ID, perm.name, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> perm.byDefault(player)));
        }
        for (Meta meta : Meta.values()) {
            META.put(meta, new PermissionNode<>(FartTogether.MOD_ID, meta.name, PermissionTypes.STRING,
                (player, uuid, context) -> ""));
        }
    }

    /** Registered in PermissionGatherEvent.Nodes; LuckPerms then lists them as farmstead_fart_together.*. */
    static java.util.List<PermissionNode<?>> nodes() {
        java.util.List<PermissionNode<?>> out = new java.util.ArrayList<>(NODES.values());
        out.addAll(META.values());
        return out;
    }

    @Override
    public boolean permission(ServerPlayer player, Perm perm) {
        return PermissionAPI.getPermission(player, NODES.get(perm));
    }

    @Override
    public String meta(ServerPlayer player, Meta meta) {
        try {
            String v = PermissionAPI.getPermission(player, META.get(meta));
            return v == null || v.isBlank() ? null : v;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String loaderName() {
        return "neoforge";
    }
}
