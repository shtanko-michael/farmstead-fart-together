package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import online.farmsteadminecraft.farttogether.net.Network;
import online.farmsteadminecraft.farttogether.server.TootServer;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import online.farmsteadminecraft.farttogether.net.Payloads;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import online.farmsteadminecraft.farttogether.FartTogether;
import online.farmsteadminecraft.farttogether.net.Proto;
*///?}

/**
 * Fabric API networking: the two payload channels (1.20.5+) or two raw channels (1.20.1). The client half lives in
 * {@link FabricClientNetwork}, which a dedicated server never loads.
 */
final class FabricNetwork implements Network {
    //? if <1.20.5 {
    /*static final ResourceLocation TO_SERVER = FartTogether.id("to_server");
    static final ResourceLocation TO_CLIENT = FartTogether.id("to_client");
    *///?}

    static void register() {
        //? if >=26.1 {
        /*PayloadTypeRegistry.serverboundPlay().register(Payloads.ToServer.TYPE, Payloads.ToServer.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(Payloads.ToClient.TYPE, Payloads.ToClient.CODEC);
        *///?} else if >=1.20.5 {
        PayloadTypeRegistry.playC2S().register(Payloads.ToServer.TYPE, Payloads.ToServer.CODEC);
        PayloadTypeRegistry.playS2C().register(Payloads.ToClient.TYPE, Payloads.ToClient.CODEC);
        //?}
        //? if >=1.20.5 {
        // 1.20.5+: Fabric API calls play handlers on the server thread
        ServerPlayNetworking.registerGlobalReceiver(Payloads.ToServer.TYPE, (payload, ctx) -> TootServer.receive(ctx.player(), payload.data()));
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(TO_SERVER, (server, player, handler, buf, sender) -> {
            byte[] data = read(buf);
            server.execute(() -> TootServer.receive(player, data));
        });
        *///?}
    }

    //? if <1.20.5 {
    /*static byte[] read(FriendlyByteBuf buf) {
        int n = buf.readableBytes();
        if (n > Proto.MAX_BYTES) return new byte[0];
        byte[] out = new byte[n];
        buf.readBytes(out);
        return out;
    }

    static FriendlyByteBuf wrap(byte[] data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBytes(data);
        return buf;
    }
    *///?}

    @Override
    public boolean serverHasChannel() {
        return FabricClientNetwork.serverHasChannel();
    }

    @Override
    public void toServer(byte[] data) {
        FabricClientNetwork.toServer(data);
    }

    @Override
    public void toPlayer(ServerPlayer player, byte[] data) {
        //? if >=1.20.5 {
        ServerPlayNetworking.send(player, new Payloads.ToClient(data));
        //?} else
        /*ServerPlayNetworking.send(player, TO_CLIENT, wrap(data));*/
    }
}
