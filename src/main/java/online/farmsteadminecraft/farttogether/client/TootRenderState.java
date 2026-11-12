package online.farmsteadminecraft.farttogether.client;

/**
 * Added to {@code LivingEntityRenderState} (Minecraft 1.21.2+, where models are posed from a render state instead of
 * the entity): the toot pose's progress (-1 = none) and kind, filled when the state is extracted.
 */
public interface TootRenderState {
    float farttogether$toot();

    int farttogether$pose();

    void farttogether$setToot(float progress, int pose);
}
