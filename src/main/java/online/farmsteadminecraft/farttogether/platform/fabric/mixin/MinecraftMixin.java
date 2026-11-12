package online.farmsteadminecraft.farttogether.platform.fabric.mixin;

import net.minecraft.client.Minecraft;
import online.farmsteadminecraft.farttogether.client.FartTogetherClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** End of every client tick (Fabric without Fabric API: this is all the tick event did). */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void farttogether$tick(CallbackInfo ci) {
        FartTogetherClient.tick((Minecraft) (Object) this);
    }
}
