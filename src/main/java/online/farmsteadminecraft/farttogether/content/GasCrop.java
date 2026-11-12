package online.farmsteadminecraft.farttogether.content;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** A crop of the mod (beans, cabbage): vanilla wheat growth, its own seed for pick-block and bone meal. */
public class GasCrop extends CropBlock {
    private final Supplier<Item> seed;

    public GasCrop(Properties properties, Supplier<Item> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
