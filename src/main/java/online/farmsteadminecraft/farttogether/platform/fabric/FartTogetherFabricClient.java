package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;
import online.farmsteadminecraft.farttogether.client.FartTogetherCommands;
import online.farmsteadminecraft.farttogether.client.GasHud;
import online.farmsteadminecraft.farttogether.client.Keys;
import online.farmsteadminecraft.farttogether.content.Content;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//? if >=1.21.6 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import online.farmsteadminecraft.farttogether.FartTogether;
*///?} else
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/** Fabric client entry: the key, the client tick, the gas meter and {@code /farttogether} through Fabric API. */
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
        //? if >=1.21.6 {
        /*HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, FartTogether.id("gas_meter"), (g, delta) -> GasHud.render(g));
        *///?} else
        HudRenderCallback.EVENT.register((g, delta) -> GasHud.render(g));
        cutout();
        FartTogetherClient.init();
    }

    /** Crops have see-through pixels; 26.x works that out from the texture itself. */
    private static void cutout() {
        if (!Content.enabled()) return;
        //? if >=1.21.6 && <26.1 {
        /*net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks(
            net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT, Content.BEAN_CROP, Content.CABBAGE_CROP);
        *///?} else if <1.21.6 {
        net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlocks(
            net.minecraft.client.renderer.RenderType.cutout(), Content.BEAN_CROP, Content.CABBAGE_CROP);
        //?}
    }
}
