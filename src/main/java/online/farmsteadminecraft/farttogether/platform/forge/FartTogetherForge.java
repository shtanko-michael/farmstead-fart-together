package online.farmsteadminecraft.farttogether.platform.forge;

import net.minecraftforge.fml.common.Mod;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;

@Mod(FartTogether.MOD_ID)
public class FartTogetherForge {
    public FartTogetherForge() {
        ModLoaderAccess.HOLDER.install(new ForgeLoaderAccess());
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            ForgeClientHooks.register();
        }
    }
}
