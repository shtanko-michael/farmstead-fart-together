package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;

/**
 * Fabric client entry. The scaffold has no Fabric API: the tick, the client command and the translations are hooked in
 * by the mixins in {@code platform.fabric.mixin} (farmstead_fart_together.fabric.mixins.json). See PLAN.md "Networking
 * and dependencies" for why gameplay will need Fabric API.
 */
public class FartTogetherFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FartTogetherClient.init();
    }
}
