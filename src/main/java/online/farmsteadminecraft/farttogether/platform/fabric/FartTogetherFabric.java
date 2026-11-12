package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.ModInitializer;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;

public class FartTogetherFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModLoaderAccess.HOLDER.install(new FabricLoaderAccess());
        FartTogether.init();
    }
}
