package online.farmsteadminecraft.farttogether.platform.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;

/** Settings button in Mod Menu. Mod Menu is optional: without it this class is never loaded. */
public class FartTogetherModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FartTogetherClient::configScreen;
    }
}
