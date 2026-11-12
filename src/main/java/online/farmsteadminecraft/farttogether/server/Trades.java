package online.farmsteadminecraft.farttogether.server;

//? if <26.1 {
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import online.farmsteadminecraft.farttogether.content.Content;
//? if >=1.21.11 {
/*import net.minecraft.world.entity.npc.villager.VillagerTrades;
*///?} else
import net.minecraft.world.entity.npc.VillagerTrades;

/**
 * Farmer villagers buy beans (novice) and cabbage, and sell bowls of beans (apprentice). Each loader adds
 * {@link #farmer} to the farmer's pools; the listing checks {@code items.beansInVillagerTrades} and whether the items
 * exist when a villager levels up, and offers nothing otherwise. From 26.1 trades are data
 * ({@code data/farmstead_fart_together/villager_trade}, written by tools/gen_content.py) and this class is empty.
 */
public final class Trades {
    private Trades() {
    }

    public static List<VillagerTrades.ItemListing> farmer(int level) {
        return switch (level) {
            case 1 -> List.of(listing(() -> Content.BEANS, 20, () -> Items.EMERALD, 1, 16, 2));
            case 2 -> List.of(listing(() -> Content.CABBAGE, 12, () -> Items.EMERALD, 1, 16, 10),
                listing(() -> Items.EMERALD, 1, () -> Content.BOWL_OF_BEANS, 1, 12, 5));
            default -> List.of();
        };
    }

    private static VillagerTrades.ItemListing listing(java.util.function.Supplier<Item> wants, int wantCount,
                                                      java.util.function.Supplier<Item> gives, int giveCount, int uses, int xp) {
        //? if >=1.21.11 {
        /*return (level, entity, random) -> offer(wants, wantCount, gives, giveCount, uses, xp);
        *///?} else
        return (entity, random) -> offer(wants, wantCount, gives, giveCount, uses, xp);
    }

    private static MerchantOffer offer(java.util.function.Supplier<Item> wants, int wantCount,
                                       java.util.function.Supplier<Item> gives, int giveCount, int uses, int xp) {
        // null: vanilla skips the listing (switched off, or the server left the items out)
        if (!Content.enabled() || !ServerConfig.TRADES.on()) return null;
        //? if >=1.20.5 {
        return new MerchantOffer(new net.minecraft.world.item.trading.ItemCost(wants.get(), wantCount),
            new ItemStack(gives.get(), giveCount), uses, xp, 0.05f);
        //?} else
        /*return new MerchantOffer(new ItemStack(wants.get(), wantCount), new ItemStack(gives.get(), giveCount), uses, xp, 0.05f);*/
    }
}
//?}
