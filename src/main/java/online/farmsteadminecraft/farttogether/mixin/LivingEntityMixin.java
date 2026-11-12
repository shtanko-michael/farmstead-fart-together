package online.farmsteadminecraft.farttogether.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import online.farmsteadminecraft.farttogether.server.TootServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A server player finishes eating or drinking: gas from the food tables. One hook for every loader and version
 * ({@code completeUsingItem} has kept its name and shape from 1.20.1 to 26.x), read before the stack is used up.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void farmstead_fart_together$ate(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        ItemStack use = player.getUseItem();
        if (use.isEmpty() || !player.isUsingItem() || use != player.getItemInHand(player.getUsedItemHand())) return;
        //? if >=1.20.5 {
        boolean food = use.has(net.minecraft.core.component.DataComponents.FOOD);
        //?} else
        /*boolean food = use.isEdible();*/
        TootServer.ate(player, use.copy(), food);
    }
}
