package online.farmsteadminecraft.farttogether.mixin;

import net.minecraft.client.model.HumanoidModel;
import online.farmsteadminecraft.farttogether.client.TootPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The toot pose goes on top of vanilla's: at the end of {@code setupAnim}, so walking, sneaking and swinging still
 * apply and the pose only bends them. The descriptor is spelled out so the generic bridge method is not hit twice.
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    //? if >=1.21.2 {
    /*@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void farttogether$pose(net.minecraft.client.renderer.entity.state.HumanoidRenderState state, CallbackInfo ci) {
        if (!(state instanceof online.farmsteadminecraft.farttogether.client.TootRenderState toot)) return;
        float t = toot.farttogether$toot();
        if (t < 0) return;
        TootPose.apply((HumanoidModel<?>) (Object) this, t, toot.farttogether$pose(), state.isCrouching, state.ageInTicks);
    }
    *///?} else {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void farttogether$pose(net.minecraft.world.entity.LivingEntity entity, float limbSwing, float limbSwingAmount,
                                   float ageInTicks, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) return;
        float t = TootPose.progress(entity.getId(), ageInTicks);
        if (t < 0) return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        TootPose.apply(model, t, TootPose.pose(entity.getId()), model.crouching, ageInTicks);
        model.hat.copyFrom(model.head); // before 1.21.2 the hat is a separate part that copied the head earlier
    }
    //?}
}
