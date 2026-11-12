package online.farmsteadminecraft.farttogether.platform.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.ModLoaderAccess;
import online.farmsteadminecraft.farttogether.content.Content;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.Drops;
import online.farmsteadminecraft.farttogether.server.FartCommands;
import online.farmsteadminecraft.farttogether.server.Loot;
import online.farmsteadminecraft.farttogether.server.ServerConfig;
import online.farmsteadminecraft.farttogether.server.TootServer;
import online.farmsteadminecraft.farttogether.server.Trades;

@Mod(FartTogether.MOD_ID)
public class FartTogetherForge {
    public FartTogetherForge() {
        ModLoaderAccess.HOLDER.install(new ForgeLoaderAccess());
        Network.HOLDER.install(new ForgeNetwork());
        ForgeNetwork.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        // a dedicated server may leave the items out (vanilla clients can join then); read before registering
        if (ModLoaderAccess.get().isServer()) {
            ServerConfig.load();
            Content.setEnabled(ServerConfig.ITEMS.on());
        }
        modBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.BLOCK)) {
                Content.registerBlocks((name, block) -> event.register(Registries.BLOCK, FartTogether.id(name), () -> block));
            } else if (event.getRegistryKey().equals(Registries.ITEM)) {
                Content.registerItems((name, item) -> event.register(Registries.ITEM, FartTogether.id(name), () -> item));
            }
        });
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) Content.food().forEach(event::accept);
            else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) Content.seeds().forEach(event::accept);
        });
        MinecraftForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> event.addNodes(ForgeLoaderAccess.nodes()));
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> TootServer.start(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> TootServer.reset());
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) TootServer.tick(event.getServer());
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> event.getDispatcher().register(FartCommands.build()));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (BlockEvent.BreakEvent event) -> {
            if (!event.isCanceled() && event.getPlayer() instanceof ServerPlayer player) {
                Drops.broke(player.level(), player, event.getPos(), event.getState());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.village.VillagerTradesEvent event) -> {
            if (event.getType() != net.minecraft.world.entity.npc.VillagerProfession.FARMER) return;
            for (int level = 1; level <= 2; level++) event.getTrades().get(level).addAll(Trades.farmer(level));
        });
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.LootTableLoadEvent event) -> {
            if (Loot.wants(event.getName().toString())) event.getTable().addPool(Loot.pool());
        });
        FartTogether.init();
        if (ModLoaderAccess.get().isClient()) {
            ForgeClientHooks.register(modBus);
        }
    }
}
