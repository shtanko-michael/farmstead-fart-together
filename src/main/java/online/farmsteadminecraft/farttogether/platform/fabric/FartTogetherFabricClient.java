package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;
import online.farmsteadminecraft.farttogether.client.FartTogetherCommands;
import online.farmsteadminecraft.farttogether.client.Keys;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

/** Fabric client entry: the key, the client tick and {@code /farttogether} through Fabric API. */
public class FartTogetherFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientNetwork.register();
        //? if >=26.1 {
        /*KeyMappingHelper.registerKeyMapping(Keys.TOOT);
        *///?} else
        KeyBindingHelper.registerKeyBinding(Keys.TOOT);
        ClientTickEvents.END_CLIENT_TICK.register(FartTogetherClient::tick);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) ->
            dispatcher.register(FartTogetherCommands.<FabricClientCommandSource>build()));
        FartTogetherClient.init();
    }
}
