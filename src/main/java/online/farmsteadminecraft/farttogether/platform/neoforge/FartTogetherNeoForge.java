package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;

@Mod(FartTogether.MOD_ID)
public class FartTogetherNeoForge {
    public FartTogetherNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModLoaderAccess.HOLDER.install(new NeoForgeLoaderAccess());
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            NeoForgeClientHooks.register(modContainer);
        }
    }
}
