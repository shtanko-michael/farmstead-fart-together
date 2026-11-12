package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
//? if <26.1
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.content.Content;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.Drops;
import online.farmsteadminecraft.farttogether.server.FartCommands;
import online.farmsteadminecraft.farttogether.server.Loot;
import online.farmsteadminecraft.farttogether.server.ServerConfig;
import online.farmsteadminecraft.farttogether.server.TootServer;
//? if <26.1
import online.farmsteadminecraft.farttogether.server.Trades;

@Mod(FartTogether.MOD_ID)
public class FartTogetherNeoForge {
    public FartTogetherNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModLoaderAccess.HOLDER.install(new NeoForgeLoaderAccess());
        Network.HOLDER.install(new NeoForgeNetwork());
        NeoForgeNetwork.register(modEventBus);
        // a dedicated server may leave the items out (vanilla clients can join then); read before registering
        if (ModLoaderAccess.get().isServer()) {
            ServerConfig.load();
            Content.setEnabled(ServerConfig.ITEMS.on());
        }
        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                Content.registerBlocks((name, block) -> event.register(Registries.BLOCK, FartTogether.id(name), () -> block));
            } else if (event.getRegistryKey().equals(Registries.ITEM)) {
                Content.registerItems((name, item) -> event.register(Registries.ITEM, FartTogether.id(name), () -> item));
            }
        });
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) Content.food().forEach(event::accept);
            else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) Content.seeds().forEach(event::accept);
        });
        NeoForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> event.addNodes(NeoForgeLoaderAccess.nodes()));
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> TootServer.start(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> TootServer.reset());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> TootServer.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(FartCommands.build()));
        //? if >=26.1 {
        /*NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, (net.neoforged.neoforge.event.level.block.BreakBlockEvent event) -> {
        *///?} else
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, (BlockEvent.BreakEvent event) -> {
            if (!event.isCanceled() && event.getPlayer() instanceof ServerPlayer player) {
                Drops.broke(player.level(), player, event.getPos(), event.getState());
            }
        });
        //? if <26.1 {
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.village.VillagerTradesEvent event) -> {
            //? if >=1.21.11 {
            /*if (!event.getType().equals(net.minecraft.world.entity.npc.villager.VillagerProfession.FARMER)) return;
            *///?} else
            if (!event.getType().equals(net.minecraft.world.entity.npc.VillagerProfession.FARMER)) return;
            for (int level = 1; level <= 2; level++) event.getTrades().get(level).addAll(Trades.farmer(level));
        });
        //?}
        //? if <26.3 {
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.LootTableLoadEvent event) -> {
            if (Loot.wants(event.getName().toString())) event.getTable().addPool(Loot.pool());
        });
        //?}
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            NeoForgeClientHooks.register(modContainer, modEventBus);
        }
    }
}
