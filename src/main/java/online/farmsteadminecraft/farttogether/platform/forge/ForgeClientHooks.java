package online.farmsteadminecraft.farttogether.platform.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;
import online.farmsteadminecraft.farttogether.client.FartTogetherCommands;
import online.farmsteadminecraft.farttogether.client.GasHud;
import online.farmsteadminecraft.farttogether.client.Keys;

final class ForgeClientHooks {
    private ForgeClientHooks() {
    }

    static void register(net.minecraftforge.eventbus.api.IEventBus modBus) {
        FartTogetherClient.init();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> FartTogetherClient.configScreen(parent)));
        MinecraftForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) ->
            event.getDispatcher().register(FartTogetherCommands.<CommandSourceStack>build()));
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) FartTogetherClient.tick(Minecraft.getInstance());
        });
        modBus.addListener((net.minecraftforge.client.event.RegisterKeyMappingsEvent event) -> event.register(Keys.TOOT));
        modBus.addListener((RegisterGuiOverlaysEvent event) ->
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "gas_meter", (gui, g, partial, w, h) -> GasHud.render(g)));
    }
}
