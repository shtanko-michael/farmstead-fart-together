package online.farmsteadminecraft.farttogether.server;

import java.util.Set;

import online.farmsteadminecraft.farttogether.content.Content;
//? if <26.3 {
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
//?}

/**
 * Beans and cabbage seeds in chests: villages, dungeons, mineshafts and shipwreck supplies get one extra pool
 * ({@code items.beansInLoot}). Each loader adds {@link #pool} from its loot-table event when the tables load.
 * 26.3 rewrote the loot builders (number providers and conditions are holders now): there the chests stay as they are
 * for now.
 */
public final class Loot {
    private static final Set<String> TABLES = Set.of(
        "minecraft:chests/village/village_plains_house", "minecraft:chests/village/village_savanna_house",
        "minecraft:chests/village/village_snowy_house", "minecraft:chests/village/village_taiga_house",
        "minecraft:chests/village/village_desert_house", "minecraft:chests/simple_dungeon",
        "minecraft:chests/abandoned_mineshaft", "minecraft:chests/shipwreck_supply");

    private Loot() {
    }

    /** Whether the table {@code id} ({@code namespace:path}) gets beans. */
    public static boolean wants(String id) {
        return TABLES.contains(id) && Content.enabled() && ServerConfig.LOOT.on();
    }

    //? if <26.3 {
    /** Two in five chests: 2-5 beans or 1-3 cabbage seeds. */
    public static LootPool pool() {
        return LootPool.lootPool()
            .when(LootItemRandomChanceCondition.randomChance(0.4f))
            .add(LootItem.lootTableItem(Content.BEANS).setWeight(3)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
            .add(LootItem.lootTableItem(Content.CABBAGE_SEEDS).setWeight(2)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
            .build();
    }
    //?}
}
