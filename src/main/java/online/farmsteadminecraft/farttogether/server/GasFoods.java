package online.farmsteadminecraft.farttogether.server;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * How much gas each food gives: {@code data/<namespace>/gas_foods/*.json} in the mod and in datapacks, e.g.
 * <pre>{"foods": {"minecraft:beetroot": 10, "#c:foods/vegetable": 4, "minecraft:milk_bucket": -100}}</pre>
 * Every file of every namespace is merged (in id order); a datapack overrides a file of ours by shipping the same
 * path. Negative weights take gas away. A food that no file lists gives {@code charge.unlistedFood}.
 *
 * <p>Read straight from the server's resource manager, and again whenever that manager changes (a {@code /reload}
 * makes a new one), so no reload listener is needed on any loader.</p>
 */
public final class GasFoods {
    private static final String DIR = "gas_foods";

    private static Object loadedFrom;
    private static final Map<Item, Integer> ITEMS = new HashMap<>();
    private static final List<Map.Entry<TagKey<Item>, Integer>> TAGS = new ArrayList<>();

    private GasFoods() {
    }

    /** Gas from eating {@code stack}, or null when it is no food and no file lists it. */
    public static Integer weight(MinecraftServer server, ItemStack stack, boolean food) {
        ensure(server);
        Integer w = ITEMS.get(stack.getItem());
        if (w != null) return w;
        for (Map.Entry<TagKey<Item>, Integer> e : TAGS) {
            if (stack.is(e.getKey())) return e.getValue();
        }
        return food ? ServerConfig.UNLISTED_FOOD.value() : null;
    }

    /** Forget the cache (the next meal reads the files again); {@code /fart reload}. */
    public static void invalidate() {
        loadedFrom = null;
    }

    public static int size(MinecraftServer server) {
        ensure(server);
        return ITEMS.size() + TAGS.size();
    }

    private static void ensure(MinecraftServer server) {
        ResourceManager rm = server.getResourceManager();
        if (rm == loadedFrom) return;
        loadedFrom = rm;
        ITEMS.clear();
        TAGS.clear();
        Map<ResourceLocation, Resource> files = new TreeMap<>(rm.listResources(DIR, id -> id.getPath().endsWith(".json")));
        for (Map.Entry<ResourceLocation, Resource> file : files.entrySet()) {
            try (Reader reader = file.getValue().openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject foods = root.getAsJsonObject("foods");
                if (foods == null) continue;
                for (Map.Entry<String, JsonElement> e : foods.entrySet()) add(file.getKey(), e.getKey(), e.getValue().getAsInt());
            } catch (Exception e) {
                FartTogether.LOGGER.warn("gas foods: cannot read {}: {}", file.getKey(), e.toString());
            }
        }
        FartTogether.LOGGER.info("gas foods: {} items and {} tags from {} files", ITEMS.size(), TAGS.size(), files.size());
    }

    private static void add(ResourceLocation file, String key, int weight) {
        boolean tag = key.startsWith("#");
        ResourceLocation id = FartTogether.parseId(tag ? key.substring(1) : key);
        if (id == null) {
            FartTogether.LOGGER.warn("gas foods {}: bad id {}", file, key);
            return;
        }
        if (tag) {
            TagKey<Item> t = TagKey.create(Registries.ITEM, id);
            TAGS.removeIf(e -> e.getKey().equals(t));
            TAGS.add(Map.entry(t, weight));
            return;
        }
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> ITEMS.put(item, weight));
    }
}
