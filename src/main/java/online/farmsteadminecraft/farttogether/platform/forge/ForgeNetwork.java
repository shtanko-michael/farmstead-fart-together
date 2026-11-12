package online.farmsteadminecraft.farttogether.platform.forge;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.client.ClientNet;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.net.Proto;
import online.farmsteadminecraft.farttogether.server.TootServer;

/**
 * Forge 1.20.1: one SimpleChannel, no dependencies. Both sides accept a peer without the channel (vanilla or no
 * mod), so nobody is refused at login; whether the server has it is asked before sending.
 */
final class ForgeNetwork implements Network {
    private static final String VERSION = String.valueOf(Proto.VERSION);
    static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(FartTogether.id("main"), () -> VERSION,
        NetworkRegistry.acceptMissingOr(VERSION::equals), NetworkRegistry.acceptMissingOr(VERSION::equals));

    record ToServer(byte[] data) {}

    record ToClient(byte[] data) {}

    static void register() {
        CHANNEL.registerMessage(0, ToServer.class, (m, buf) -> buf.writeByteArray(m.data()),
            buf -> new ToServer(buf.readByteArray(Proto.MAX_BYTES)), ForgeNetwork::onServer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1, ToClient.class, (m, buf) -> buf.writeByteArray(m.data()),
            buf -> new ToClient(buf.readByteArray(Proto.MAX_BYTES)), ForgeNetwork::onClient, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static void onServer(ToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) ctx.get().enqueueWork(() -> TootServer.receive(sender, msg.data()));
        ctx.get().setPacketHandled(true);
    }

    private static void onClient(ToClient msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientNet.receive(msg.data()));
        ctx.get().setPacketHandled(true);
    }

    @Override
    public boolean serverHasChannel() {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null && CHANNEL.isRemotePresent(connection.getConnection());
    }

    @Override
    public void toServer(byte[] data) {
        CHANNEL.sendToServer(new ToServer(data));
    }

    @Override
    public void toPlayer(ServerPlayer player, byte[] data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ToClient(data));
    }
}
