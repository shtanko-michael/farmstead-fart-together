package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.ServerConfig;
import online.farmsteadminecraft.farttogether.server.TootServer;

@Mod(FartTogether.MOD_ID)
public class FartTogetherNeoForge {
    public FartTogetherNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModLoaderAccess.HOLDER.install(new NeoForgeLoaderAccess());
        Network.HOLDER.install(new NeoForgeNetwork());
        NeoForgeNetwork.register(modEventBus);
        NeoForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> event.addNodes(NeoForgeLoaderAccess.nodes()));
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> ServerConfig.load());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> TootServer.reset());
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            NeoForgeClientHooks.register(modContainer, modEventBus);
        }
    }
}
