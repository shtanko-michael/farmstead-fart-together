package online.farmsteadminecraft.farttogether.client;

import net.minecraft.client.Minecraft;
//? if <26.1
import net.minecraft.client.gui.GuiGraphics;

/**
 * The gas meter above the food row: the gas icon, then a bar filled as gas grows, with a tick where a toot becomes
 * possible and a gold glow when the meter is full. While the toot key is held the part of the gas that will come out
 * lights up and grows (hold to charge); alone on a server without the mod the bar shows only the hold's strength.
 * Hidden while there is no gas and nothing is held, and with {@code general.gasMeter = false}. Drawn only with
 * {@code fill}, which every version from 1.20.1 to 26.x has (the icon too, pixel by pixel from {@link GasIconArt});
 * each loader calls {@link #render} from its own HUD hook.
 */
public final class GasHud {
    private static final int W = 71, H = 5;

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
        float hold = ClientNet.holdShare();
        boolean holding = hold >= 0;
        boolean meter = max > 0 && charge > 0;
        if (!meter && !holding) return;
        int width = mc.getWindow().getGuiScaledWidth(), height = mc.getWindow().getGuiScaledHeight();
        // the icon sits right of the hotbar's centre, the bar after it, both ending where the food row ends
        int iconX = width / 2 + 10;
        int x = iconX + GasIconArt.SIZE + 2;
        int y = height - 39 - 10 - H;
        // the air bubbles take the row above the food when under water
        if (mc.player.getAirSupply() < mc.player.getMaxAirSupply() || mc.player.isUnderWater()) y -= 10;
        if (mc.player.getVehicle() != null) y -= 10;

        boolean full = meter && charge >= max;
        boolean blink = (mc.player.tickCount / 4) % 2 == 0;
        icon(g, iconX, y + H / 2 - GasIconArt.SIZE / 2, full || (holding && hold >= 1f) ? GasIconArt.GAS_ICON_FULL : GasIconArt.GAS_ICON);

        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, 0xA0101010);
        if (meter) {
            float share = Math.min(1f, charge / (float) max);
            boolean ready = charge >= min;
            int fillW = Math.max(1, Math.round(W * share));
            // dimmed while charging: the bright part on top is what comes out
            int top = holding ? 0xFF55703A : full ? 0xFFFFD84A : ready ? 0xFF9BD45A : 0xFF6E8F4A;
            int bottom = holding ? 0xFF3E5428 : full ? 0xFFE0A020 : ready ? 0xFF5E9A2E : 0xFF4C6634;
            g.fill(x, y, x + fillW, y + 2, top);
            g.fill(x, y + 2, x + fillW, y + H, bottom);
            if (holding) {
                int outW = Math.max(1, Math.round(W * Math.min(1f, ClientNet.holdGas() / max)));
                boolean all = hold >= 1f;
                g.fill(x, y, x + outW, y + 2, all ? 0xFFFFF0A0 : 0xFFE8FFB0);
                g.fill(x, y + 2, x + outW, y + H, all ? 0xFFFFC830 : 0xFFB6E36A);
                // the leading edge pulses while it still grows
                if (!all && blink) g.fill(x + outW - 1, y - 1, x + outW, y + H + 1, 0xFFFFFFFF);
            }
            if (min > 0 && min < max) {
                int mx = x + Math.round(W * (min / (float) max));
                g.fill(mx, y - 1, mx + 1, y + H + 1, 0xC0FFFFFF);
            }
        } else {
            // no modded server: the bar is only the strength of the hold
            int outW = Math.max(1, Math.round(W * Math.max(0.08f, hold)));
            g.fill(x, y, x + outW, y + 2, 0xFFE8FFB0);
            g.fill(x, y + 2, x + outW, y + H, 0xFFB6E36A);
        }
        if ((full && !holding || holding && hold >= 1f) && blink) {
            g.fill(x - 1, y - 1, x + W + 1, y, 0xFFFFF0A0);
            g.fill(x - 1, y + H, x + W + 1, y + H + 1, 0xFFFFF0A0);
        }
    }

    //? if >=26.1 {
    /*private static void icon(net.minecraft.client.gui.GuiGraphicsExtractor g, int x, int y, int[] argb) {
    *///?} else
    private static void icon(GuiGraphics g, int x, int y, int[] argb) {
        int n = GasIconArt.SIZE;
        for (int i = 0; i < argb.length; i++) {
            if ((argb[i] >>> 24) == 0) continue;
            int px = x + i % n, py = y + i / n;
            g.fill(px, py, px + 1, py + 1, argb[i]);
        }
    }
}
