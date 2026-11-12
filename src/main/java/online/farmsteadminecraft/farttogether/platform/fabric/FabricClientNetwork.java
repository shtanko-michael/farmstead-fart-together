package online.farmsteadminecraft.farttogether.platform.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import online.farmsteadminecraft.farttogether.client.ClientNet;
//? if >=1.20.5
import online.farmsteadminecraft.farttogether.net.Payloads;

/** The client half of {@link FabricNetwork}. */
final class FabricClientNetwork {
    private FabricClientNetwork() {
    }

    static void register() {
        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(Payloads.ToClient.TYPE, (payload, ctx) -> ClientNet.receive(payload.data()));
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.TO_CLIENT, (client, handler, buf, sender) -> {
            byte[] data = FabricNetwork.read(buf);
            client.execute(() -> ClientNet.receive(data));
        });
        *///?}
    }

    static boolean serverHasChannel() {
        //? if >=1.20.5 {
        return ClientPlayNetworking.canSend(Payloads.ToServer.TYPE);
        //?} else
        /*return ClientPlayNetworking.canSend(FabricNetwork.TO_SERVER);*/
    }

    static void toServer(byte[] data) {
        //? if >=1.20.5 {
        ClientPlayNetworking.send(new Payloads.ToServer(data));
        //?} else
        /*ClientPlayNetworking.send(FabricNetwork.TO_SERVER, FabricNetwork.wrap(data));*/
    }
}
