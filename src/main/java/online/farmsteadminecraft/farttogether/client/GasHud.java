package online.farmsteadminecraft.farttogether.client;

import net.minecraft.client.Minecraft;
//? if <26.1
import net.minecraft.client.gui.GuiGraphics;

/**
 * The gas meter: a thin bar above the food row, filled as gas grows, with a tick where a toot becomes possible and a
 * gold glow when the meter is full. Hidden while there is no gas, on servers without the mod and with
 * {@code general.gasMeter = false}. Drawn only with {@code fill}, which every version from 1.20.1 to 26.x has; each
 * loader calls {@link #render} from its own HUD hook.
 */
public final class GasHud {
    private static final int W = 81, H = 5;

    private GasHud() {
    }

    //? if >=26.1 {
    /*public static void render(net.minecraft.client.gui.GuiGraphicsExtractor g) {
    *///?} else
    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        //? if >=26.2 {
        /*boolean hidden = mc.gui.hud.isHidden();
        *///?} else
        boolean hidden = mc.options.hideGui;
        if (mc.player == null || hidden || mc.player.isSpectator() || !ClientConfig.GAS_METER.on()) return;
        int[] gas = ClientNet.gas();
        int charge = gas[0], max = gas[1], min = gas[2];
        if (max <= 0 || charge <= 0) return;
        int width = mc.getWindow().getGuiScaledWidth(), height = mc.getWindow().getGuiScaledHeight();
        int x = width / 2 + 10;
        int y = height - 39 - 10 - H;
        // the air bubbles take the row above the food when under water
        if (mc.player.getAirSupply() < mc.player.getMaxAirSupply() || mc.player.isUnderWater()) y -= 10;
        if (mc.player.getVehicle() != null) y -= 10;

        float share = Math.min(1f, charge / (float) max);
        boolean full = charge >= max;
        boolean ready = charge >= min;
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xA0101010);
        int fillW = Math.max(1, Math.round(W * share));
        int top = full ? 0xFFFFD84A : ready ? 0xFF9BD45A : 0xFF6E8F4A;
        int bottom = full ? 0xFFE0A020 : ready ? 0xFF5E9A2E : 0xFF4C6634;
        g.fill(x, y, x + fillW, y + 2, top);
        g.fill(x, y + 2, x + fillW, y + H, bottom);
        if (min > 0 && min < max) {
            int mx = x + Math.round(W * (min / (float) max));
            g.fill(mx, y - 1, mx + 1, y + H + 1, 0xC0FFFFFF);
        }
        if (full && (mc.player.tickCount / 4) % 2 == 0) {
            g.fill(x - 1, y - 1, x + W + 1, y, 0xFFFFF0A0);
            g.fill(x - 1, y + H, x + W + 1, y + H + 1, 0xFFFFF0A0);
        }
    }
}
