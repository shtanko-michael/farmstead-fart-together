package online.farmsteadminecraft.farttogether.net;

import net.minecraft.server.level.ServerPlayer;

/**
 * The loader's transport for {@link Proto} bytes: Fabric API networking, NeoForge payload handlers or a Forge
 * SimpleChannel ({@code platform/<loader>/...Network}). Received bytes go to {@code server.TootServer.receive} and
 * {@code client.ClientNet.receive}, always on the game thread.
 */
public interface Network {
    Holder HOLDER = new Holder();

    static Network get() {
        return HOLDER.network;
    }

    /** Client side: the server we are connected to has our channel (so it runs the mod). */
    boolean serverHasChannel();

    void toServer(byte[] data);

    void toPlayer(ServerPlayer player, byte[] data);

    final class Holder {
        private Network network;

        public void install(Network network) {
            this.network = network;
        }
    }
}
