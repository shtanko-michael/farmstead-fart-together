package online.farmsteadminecraft.farttogether.platform.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.server.Perm;

final class ForgeLoaderAccess implements ModLoaderAccess {
    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public boolean isServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public java.nio.file.Path configDir() {
        return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    private static final java.util.Map<Perm, PermissionNode<Boolean>> NODES = new java.util.EnumMap<>(Perm.class);

    static {
        for (Perm perm : Perm.values()) {
            NODES.put(perm, new PermissionNode<>(FartTogether.MOD_ID, perm.name, PermissionTypes.BOOLEAN,
                (player, uuid, context) -> perm.def));
        }
    }

    /** Registered in PermissionGatherEvent.Nodes; LuckPerms then lists them as farmstead_fart_together.*. */
    static java.util.List<PermissionNode<?>> nodes() {
        return new java.util.ArrayList<>(NODES.values());
    }

    @Override
    public boolean permission(ServerPlayer player, Perm perm) {
        return PermissionAPI.getPermission(player, NODES.get(perm));
    }

    @Override
    public String loaderName() {
        return "forge";
    }
}
