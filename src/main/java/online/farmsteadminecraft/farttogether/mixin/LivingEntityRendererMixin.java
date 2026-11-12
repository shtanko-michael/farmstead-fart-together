package online.farmsteadminecraft.farttogether.mixin;

//? if >=1.21.2 {
/*import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import online.farmsteadminecraft.farttogether.client.TootPose;
import online.farmsteadminecraft.farttogether.client.TootRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/^* 1.21.2+: copies the entity's toot pose into its render state, which is all the model sees. ^/
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void farttogether$toot(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        float t = entity instanceof net.minecraft.world.entity.player.Player ? TootPose.progress(entity.getId(), state.ageInTicks) : -1;
        ((TootRenderState) state).farttogether$setToot(t, t < 0 ? 0 : TootPose.pose(entity.getId()));
    }
}
*///?}
