import org.gradle.api.file.CopySpec

/**
 * The mod's data files are written in the 1.21.2+ form (tools/gen_content.py). Older targets read other shapes:
 *  - before 1.21: plural folders (`recipes/`, `loot_tables/`);
 *  - before 1.21.2: recipe ingredients are objects (`{"item": "ns:id"}`), not bare id strings;
 *  - before 1.20.5: a recipe result and an advancement icon name their item with `item`, not `id`;
 *  - before 26.1: villager trades are code (Trades.java), so the `villager_trade` data and tags stay out.
 * This rewrites them while resources are copied, so one source tree serves every target.
 */
fun CopySpec.dataCompat(minecraft: String) {
    fun older(than: String): Boolean {
        val a = minecraft.split('.').map { it.toIntOrNull() ?: 0 }
        val b = than.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x < y
        }
        return false
    }
    val plural = older("1.21")
    val objectIngredients = older("1.21.2")
    val itemResult = older("1.20.5")
    val codeTrades = older("26.1")
    val bareId = Regex("""^(\s+)"(#?)([a-z0-9_.-]+:[a-z0-9_/.-]+)"(,?)\s*$""")
    eachFile {
        val p = path
        if (codeTrades && p.startsWith("data/") && p.contains("villager_trade/")) {
            exclude()
            return@eachFile
        }
        if (itemResult && p.startsWith("data/") && p.contains("/advancement/")) filter { it.replace("\"id\":", "\"item\":") }
        if (p.startsWith("data/") && p.contains("/recipe/")) {
            if (objectIngredients || itemResult) filter { line ->
                var out = line
                if (objectIngredients) {
                    val m = bareId.matchEntire(out)
                    if (m != null) {
                        val (indent, hash, id, comma) = m.destructured
                        out = if (hash.isEmpty()) "$indent{\"item\": \"$id\"}$comma" else "$indent{\"tag\": \"$id\"}$comma"
                    }
                }
                if (itemResult) out = out.replace("\"id\":", "\"item\":")
                out
            }
        }
        if (plural && p.startsWith("data/")) {
            path = p.replace("/recipe/", "/recipes/").replace("/loot_table/", "/loot_tables/")
                .replace("/tags/item/", "/tags/items/").replace("/tags/block/", "/tags/blocks/")
                .replace("/tags/entity_type/", "/tags/entity_types/").replace("/advancement/", "/advancements/")
        }
    }
}
