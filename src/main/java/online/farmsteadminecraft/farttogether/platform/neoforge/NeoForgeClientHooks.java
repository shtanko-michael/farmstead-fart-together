package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;
import online.farmsteadminecraft.farttogether.client.FartTogetherCommands;
import online.farmsteadminecraft.farttogether.client.Keys;

final class NeoForgeClientHooks {
    private NeoForgeClientHooks() {
    }

    static void register(ModContainer container, net.neoforged.bus.api.IEventBus modBus) {
        FartTogetherClient.init();
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (IConfigScreenFactory) (mod, parent) -> FartTogetherClient.configScreen(parent));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> FartTogetherClient.tick(Minecraft.getInstance()));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) ->
            event.getDispatcher().register(FartTogetherCommands.<CommandSourceStack>build()));
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) -> event.register(Keys.TOOT));
    }
}
