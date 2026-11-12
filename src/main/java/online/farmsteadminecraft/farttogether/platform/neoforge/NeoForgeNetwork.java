package online.farmsteadminecraft.farttogether.platform.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import online.farmsteadminecraft.farttogether.client.ClientNet;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.net.Payloads;
import online.farmsteadminecraft.farttogether.net.Proto;
import online.farmsteadminecraft.farttogether.server.TootServer;

/**
 * NeoForge's own payload registration, no dependencies. The registrar is optional, so a vanilla server or client
 * (or one without this mod) connects normally; whether the other side has the channel is asked before use.
 * Handlers run on the game thread.
 */
final class NeoForgeNetwork implements Network {
    static void register(IEventBus modBus) {
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(String.valueOf(Proto.VERSION)).optional();
            registrar.playToServer(Payloads.ToServer.TYPE, Payloads.ToServer.CODEC,
                (payload, ctx) -> TootServer.receive((ServerPlayer) ctx.player(), payload.data()));
            registrar.playToClient(Payloads.ToClient.TYPE, Payloads.ToClient.CODEC,
                (payload, ctx) -> ClientNet.receive(payload.data()));
        });
    }

    @Override
    public boolean serverHasChannel() {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null && connection.hasChannel(Payloads.ToServer.TYPE);
    }

    @Override
    public void toServer(byte[] data) {
        //? if >=1.21.6 {
        /*net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Payloads.ToServer(data));
        *///?} else
        PacketDistributor.sendToServer(new Payloads.ToServer(data));
    }

    @Override
    public void toPlayer(ServerPlayer player, byte[] data) {
        if (player.connection.hasChannel(Payloads.ToClient.TYPE)) PacketDistributor.sendToPlayer(player, new Payloads.ToClient(data));
    }
}
