package online.farmsteadminecraft.farttogether.mixin;

//? if >=1.21.2 {
/*import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import online.farmsteadminecraft.farttogether.client.TootRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/^* 1.21.2+: the toot pose carried from the entity to its model. ^/
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements TootRenderState {
    @Unique
    private float farttogether$toot = -1;
    @Unique
    private int farttogether$pose;

    @Override
    public float farttogether$toot() {
        return farttogether$toot;
    }

    @Override
    public int farttogether$pose() {
        return farttogether$pose;
    }

    @Override
    public void farttogether$setToot(float progress, int pose) {
        farttogether$toot = progress;
        farttogether$pose = pose;
    }
}
*///?}
