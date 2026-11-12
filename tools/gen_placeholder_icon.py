"""Placeholder mod icon until the real one is drawn (PLAN.md section 8): a pixel-art green cloud on a dark tile.

    python -I tools/gen_placeholder_icon.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/farmstead_fart_together/icon.png"

G = 16  # drawn on a 16x16 grid, scaled 8x without smoothing
img = Image.new("RGBA", (G, G), (34, 40, 30, 255))
d = ImageDraw.Draw(img)
puffs = [(5, 9, 3), (9, 8, 4), (12, 10, 2), (7, 6, 2)]
for x, y, r in puffs:
    d.ellipse([x - r, y - r, x + r, y + r], fill=(127, 191, 63, 255))
for x, y, r in puffs:
    d.ellipse([x - r + 1, y - r, x + r - 2, y - 1], fill=(170, 220, 100, 255))
d.rectangle([0, 0, G - 1, G - 1], outline=(20, 24, 18, 255))
OUT.parent.mkdir(parents=True, exist_ok=True)
img.resize((128, 128), Image.NEAREST).save(OUT)
print(OUT)
