package online.farmsteadminecraft.farttogether.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import online.farmsteadminecraft.farttogether.FartTogether;

/**
 * Every player's gas (charge) and whether an admin switched their toots off ({@code /fart toggle}). Kept by UUID for
 * everyone, modded or not, and saved in the world folder ({@code farmstead_fart_together-players.txt}) so gas
 * survives a relog and a restart. Server thread only.
 */
public final class Gas {
    private static final String FILE = FartTogether.MOD_ID + "-players.txt";

    private static final Map<UUID, Double> CHARGE = new HashMap<>();
    private static final Set<UUID> DISABLED = new HashSet<>();
    private static Path file;
    private static boolean dirty;

    private Gas() {
    }

    public static double charge(UUID player) {
        return CHARGE.getOrDefault(player, 0.0);
    }

    /** Sets the charge clamped to 0..max; true when the whole number shown to the player changed. */
    public static boolean set(ServerPlayer player, double value) {
        double max = max(player);
        double before = charge(player.getUUID());
        double now = Math.max(0, Math.min(max, value));
        if (now == before) return false;
        CHARGE.put(player.getUUID(), now);
        dirty = true;
        return (int) before != (int) now;
    }

    public static int max(ServerPlayer player) {
        return (int) Math.round(TootServer.meta(player, Meta.CHARGE_MAX));
    }

    public static boolean disabled(UUID player) {
        return DISABLED.contains(player);
    }

    /** Switches a player's toots off or on; returns the new state (true = may toot). */
    public static boolean toggle(UUID player) {
        dirty = true;
        if (DISABLED.remove(player)) return true;
        DISABLED.add(player);
        return false;
    }

    public static void load(MinecraftServer server) {
        CHARGE.clear();
        DISABLED.clear();
        dirty = false;
        file = server.getWorldPath(LevelResource.ROOT).resolve(FILE);
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.trim().split("\\s+");
                if (p.length < 2 || p[0].startsWith("#")) continue;
                try {
                    UUID id = UUID.fromString(p[0]);
                    double c = Double.parseDouble(p[1]);
                    if (Double.isFinite(c) && c > 0) CHARGE.put(id, c);
                    if (p.length > 2 && p[2].equals("off")) DISABLED.add(id);
                } catch (IllegalArgumentException e) {
                    // a hand-edited line we cannot read: skip it
                }
            }
        } catch (IOException e) {
            FartTogether.LOGGER.warn("cannot read {}: {}", file, e.toString());
        }
    }

    public static void save() {
        if (file == null || !dirty) return;
        dirty = false;
        Set<UUID> ids = new HashSet<>(CHARGE.keySet());
        ids.addAll(DISABLED);
        List<String> lines = new ArrayList<>();
        lines.add("# Fart Together: player gas and admin switches (uuid charge [off])");
        for (UUID id : ids) {
            double c = charge(id);
            if (c <= 0 && !DISABLED.contains(id)) continue;
            lines.add(id + " " + String.format(java.util.Locale.ROOT, "%.2f", c) + (DISABLED.contains(id) ? " off" : ""));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            FartTogether.LOGGER.warn("cannot write {}: {}", file, e.toString());
        }
    }

    public static void reset() {
        save();
        CHARGE.clear();
        DISABLED.clear();
        file = null;
    }
}
