package online.farmsteadminecraft.farttogether.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * The mod's advancements ({@code data/farmstead_fart_together/advancement/*.json}, in the Husbandry tab). Each has
 * one {@code minecraft:impossible} criterion named {@code done}: the server grants it here when the toot happens.
 * Icons are vanilla items, so the advancements load even on a server that switched the mod's items off.
 */
public enum Awards {
    FIRST("first_toot"),
    LEGENDARY("legendary_toot"),
    JET("jet_jump"),
    GOLDEN("golden_toot"),
    BUBBLES("bubbles"),
    WITHER("wither_toot"),
    TOGETHER_2("together_2"),
    TOGETHER_4("together_4"),
    TOGETHER_8("together_8");

    public final String path;

    Awards(String path) {
        this.path = path;
    }

    public void grant(ServerPlayer player) {
        MinecraftServer server = TootServer.server(player);
        ResourceLocation id = FartTogether.id(path);
        //? if >=1.20.2 {
        var advancement = server.getAdvancements().get(id);
        //?} else
        /*var advancement = server.getAdvancements().getAdvancement(id);*/
        if (advancement == null) return;
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, "done");
        }
    }
}
