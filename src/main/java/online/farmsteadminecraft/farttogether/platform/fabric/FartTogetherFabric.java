package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
*///?} else
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.content.Content;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.Drops;
import online.farmsteadminecraft.farttogether.server.FartCommands;
import online.farmsteadminecraft.farttogether.server.ServerConfig;
import online.farmsteadminecraft.farttogether.server.TootServer;

/** Fabric entry (needs Fabric API: networking, keys, commands, events, the mod's resources and registry sync). */
public class FartTogetherFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModLoaderAccess.HOLDER.install(new FabricLoaderAccess());
        Network.HOLDER.install(new FabricNetwork());
        FabricNetwork.register();
        // a dedicated server may leave the items out (vanilla clients can join then); read before registering
        if (ModLoaderAccess.get().isServer()) {
            ServerConfig.load();
            Content.setEnabled(ServerConfig.ITEMS.on());
        }
        Content.registerBlocks((name, block) -> Registry.register(BuiltInRegistries.BLOCK, FartTogether.id(name), block));
        Content.registerItems((name, item) -> Registry.register(BuiltInRegistries.ITEM, FartTogether.id(name), item));
        //? if >=26.1 {
        /*CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(out -> Content.food().forEach(out::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(out -> Content.seeds().forEach(out::accept));
        *///?} else {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> Content.food().forEach(entries::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> Content.seeds().forEach(entries::accept));
        //?}
        ServerLifecycleEvents.SERVER_STARTING.register(TootServer::start);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TootServer.reset());
        ServerTickEvents.END_SERVER_TICK.register(TootServer::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, selection) -> dispatcher.register(FartCommands.build()));
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> Drops.broke(level, player, pos, state));
        FartTogether.init();
    }
}
