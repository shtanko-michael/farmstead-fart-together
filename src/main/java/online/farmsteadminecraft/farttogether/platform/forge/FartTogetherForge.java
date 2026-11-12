package online.farmsteadminecraft.farttogether.platform.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.ServerConfig;
import online.farmsteadminecraft.farttogether.server.TootServer;

@Mod(FartTogether.MOD_ID)
public class FartTogetherForge {
    public FartTogetherForge() {
        ModLoaderAccess.HOLDER.install(new ForgeLoaderAccess());
        Network.HOLDER.install(new ForgeNetwork());
        ForgeNetwork.register();
        MinecraftForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> event.addNodes(ForgeLoaderAccess.nodes()));
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> ServerConfig.load());
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> TootServer.reset());
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            ForgeClientHooks.register(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}
