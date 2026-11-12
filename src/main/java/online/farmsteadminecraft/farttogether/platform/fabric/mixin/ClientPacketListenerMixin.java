package online.farmsteadminecraft.farttogether.platform.fabric.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import online.farmsteadminecraft.farttogether.platform.fabric.ClientCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <1.21.6
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side {@code /farttogether} without Fabric API: the command joins the server's command tree for
 * suggestions, and typing it runs it here instead of sending it to the server.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void farttogether$suggest(ClientboundCommandsPacket packet, CallbackInfo ci) {
        ClientCommands.addTo(((ClientPacketListener) (Object) this).getCommands());
    }

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void farttogether$run(String command, CallbackInfo ci) {
        if (ClientCommands.run(command)) ci.cancel();
    }

    //? if <1.21.6 {
    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void farttogether$runUnsigned(String command, CallbackInfoReturnable<Boolean> cir) {
        if (ClientCommands.run(command)) cir.setReturnValue(true);
    }
    //?}
}
