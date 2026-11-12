package online.farmsteadminecraft.farttogether.content;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
//? if >=1.21.2 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import online.farmsteadminecraft.farttogether.FartTogether;
*///?}

/**
 * The mod's own items and blocks: beans (seed and food, like a potato), a bowl of beans (the strongest charge),
 * cabbage and cabbage seeds, and the two crops. Each loader calls {@link #registerBlocks} then {@link #registerItems}
 * from its own registration event; the fields are set as the objects are made.
 *
 * <p>A dedicated server can switch them off ({@code items.enabled = false} in the server file, read before
 * registration): then nothing of ours is in the registries, the safe choice for servers with vanilla players (they
 * can log in either way, but would meet unknown items and blocks otherwise). A client always registers them.</p>
 */
public final class Content {
    public static Block BEAN_CROP, CABBAGE_CROP;
    public static Item BEANS, BOWL_OF_BEANS, CABBAGE, CABBAGE_SEEDS;
    private static boolean enabled = true;

    private Content() {
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    /** Whether the items and blocks were registered (false on a server that switched them off). */
    public static boolean enabled() {
        return enabled && BEANS != null;
    }

    public static void registerBlocks(BiConsumer<String, Block> sink) {
        if (!enabled) return;
        BEAN_CROP = new GasCrop(crop("bean_crop"), () -> BEANS);
        sink.accept("bean_crop", BEAN_CROP);
        CABBAGE_CROP = new GasCrop(crop("cabbage_crop"), () -> CABBAGE_SEEDS);
        sink.accept("cabbage_crop", CABBAGE_CROP);
    }

    public static void registerItems(BiConsumer<String, Item> sink) {
        if (!enabled) return;
        BEANS = planting(BEAN_CROP, item("beans").food(food(2, 0.3f, false)));
        sink.accept("beans", BEANS);
        CABBAGE_SEEDS = planting(CABBAGE_CROP, item("cabbage_seeds"));
        sink.accept("cabbage_seeds", CABBAGE_SEEDS);
        CABBAGE = new Item(item("cabbage").food(food(3, 0.4f, false)));
        sink.accept("cabbage", CABBAGE);
        BOWL_OF_BEANS = bowl(item("bowl_of_beans").stacksTo(1).food(food(7, 0.6f, true)));
        sink.accept("bowl_of_beans", BOWL_OF_BEANS);
    }

    /** For the loaders' creative tabs: food tab and natural blocks tab. */
    public static List<Item> food() {
        return enabled() ? List.of(BEANS, CABBAGE, BOWL_OF_BEANS) : List.of();
    }

    public static List<Item> seeds() {
        return enabled() ? List.of(BEANS, CABBAGE_SEEDS) : List.of();
    }

    private static BlockBehaviour.Properties crop(String name) {
        //? if >=1.20.5 {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT);
        //?} else
        /*BlockBehaviour.Properties p = BlockBehaviour.Properties.copy(Blocks.WHEAT);*/
        //? if >=1.21.2 {
        /*p = p.setId(ResourceKey.create(Registries.BLOCK, FartTogether.id(name)));
        *///?}
        return p;
    }

    private static Item.Properties item(String name) {
        Item.Properties p = new Item.Properties();
        //? if >=1.21.2 {
        /*p = p.setId(ResourceKey.create(Registries.ITEM, FartTogether.id(name)));
        *///?}
        return p;
    }

    private static Item planting(Block crop, Item.Properties p) {
        //? if >=1.21.2 {
        /*return new net.minecraft.world.item.BlockItem(crop, p.useItemDescriptionPrefix());
        *///?} else
        return new net.minecraft.world.item.ItemNameBlockItem(crop, p);
    }

    private static Item bowl(Item.Properties p) {
        //? if >=1.21.2 {
        /*return new Item(p.usingConvertsTo(Items.BOWL));
        *///?} else if >=1.20.5 {
        return new Item(p);
        //?} else
        /*return new net.minecraft.world.item.BowlFoodItem(p);*/
    }

    private static FoodProperties food(int nutrition, float saturation, boolean bowl) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition);
        //? if >=1.20.5 {
        b = b.saturationModifier(saturation);
        //?} else
        /*b = b.saturationMod(saturation);*/
        //? if >=1.20.5 && <1.21.2 {
        if (bowl) b = b.usingConvertsTo(Items.BOWL);
        //?}
        return b.build();
    }
}
