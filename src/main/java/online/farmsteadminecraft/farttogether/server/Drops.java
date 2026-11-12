package online.farmsteadminecraft.farttogether.server;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import online.farmsteadminecraft.farttogether.content.Content;

/**
 * Where beans and cabbage seeds come from: now and then from grass and ferns a player breaks, like wheat seeds.
 * Every loader calls {@link #broke} from its block-break event (after the break, server side).
 */
public final class Drops {
    private Drops() {
    }

    public static void broke(Level level, Player player, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !Content.enabled() || player.isCreative() || player.isSpectator()) return;
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        // short_grass is plain "grass" before 1.20.3
        if (!(path.equals("minecraft:short_grass") || path.equals("minecraft:grass") || path.equals("minecraft:tall_grass")
            || path.equals("minecraft:fern") || path.equals("minecraft:large_fern"))) return;
        double r = level.getRandom().nextDouble();
        double beans = ServerConfig.BEANS_FROM_GRASS.value();
        if (r < beans) Block.popResource(level, pos, new ItemStack(Content.BEANS));
        else if (r < beans + ServerConfig.CABBAGE_FROM_GRASS.value()) Block.popResource(level, pos, new ItemStack(Content.CABBAGE_SEEDS));
    }
}
