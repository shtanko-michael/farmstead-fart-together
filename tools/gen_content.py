"""Beans and cabbage: placeholder textures (procedural pixel art, only drawn where a texture is missing: the real ones come from tools/art.py)
and every JSON the items, crops and gas need.

    python -I tools/gen_content.py            # models, blockstates, loot, recipe, gas foods (+ missing textures)
    python -I tools/gen_content.py --textures # also redraw the placeholder textures

The JSON is written in the 1.21.2+ form. The build turns it into older forms per target (folder names and recipe
ingredients for 1.20.1 / 1.21.1, see buildSrc DataCompat.kt). Textures are only drawn when missing (or with
--textures), so final art dropped into textures/ is never overwritten.
"""
from __future__ import annotations

import json
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
NS = "farmstead_fart_together"
RES = ROOT / "src/main/resources"
ASSETS = RES / "assets" / NS
DATA = RES / "data" / NS

CROPS = {
    # block id: (seed item, produce item, stage count drawn, ages 0..7 -> stage)
    "bean_crop": ("beans", "beans", 4),
    "cabbage_crop": ("cabbage_seeds", "cabbage", 4),
}
STAGE_OF_AGE = [0, 0, 1, 1, 2, 2, 2, 3]
ITEMS = ["beans", "cabbage_seeds", "cabbage", "bowl_of_beans"]

# Vanilla and our own foods (data/<ns>/gas_foods/*.json). 100 is a full meter; a toot needs 10 by default.
GAS_VANILLA = {
    "minecraft:beetroot": 10, "minecraft:beetroot_soup": 35, "minecraft:mushroom_stew": 30,
    "minecraft:rabbit_stew": 30, "minecraft:suspicious_stew": 30, "minecraft:rotten_flesh": 20,
    "minecraft:spider_eye": 15, "minecraft:poisonous_potato": 15, "minecraft:potato": 5, "minecraft:baked_potato": 8,
    "minecraft:carrot": 4, "minecraft:golden_carrot": 8, "minecraft:apple": 4, "minecraft:golden_apple": 25,
    "minecraft:enchanted_golden_apple": 100, "minecraft:bread": 5, "minecraft:cookie": 4, "minecraft:pumpkin_pie": 10,
    "minecraft:sweet_berries": 3, "minecraft:glow_berries": 3, "minecraft:melon_slice": 3, "minecraft:chorus_fruit": 12,
    "minecraft:honey_bottle": 6, "minecraft:dried_kelp": 2, "minecraft:beef": 6, "minecraft:cooked_beef": 6,
    "minecraft:porkchop": 6, "minecraft:cooked_porkchop": 6, "minecraft:chicken": 5, "minecraft:cooked_chicken": 5,
    "minecraft:mutton": 5, "minecraft:cooked_mutton": 5, "minecraft:rabbit": 5, "minecraft:cooked_rabbit": 5,
    "minecraft:cod": 4, "minecraft:cooked_cod": 4, "minecraft:salmon": 4, "minecraft:cooked_salmon": 4,
    "minecraft:pufferfish": 20, "minecraft:tropical_fish": 4,
    # milk settles the stomach
    "minecraft:milk_bucket": -100,
}
GAS_OWN = {f"{NS}:beans": 15, f"{NS}:cabbage": 12, f"{NS}:bowl_of_beans": 60}


def write(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8", newline="\n")


# ---------------------------------------------------------------- placeholder textures


def px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), c)


def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)


def bean(img, x, y, c, rnd):
    """A 3x2 bean with a light top edge."""
    for dx, dy in ((0, 0), (1, 0), (2, 0), (0, 1), (1, 1), (2, 1)):
        px(img, x + dx, y + dy, shade(c, 0.85 + rnd.random() * 0.2))
    px(img, x + 1, y, shade(c, 1.25))
    px(img, x + 2, y + 1, shade(c, 0.65))


def stalk(img, rnd, height, leaves, pods, pod_color):
    green, dark = (86, 150, 52, 255), (52, 104, 34, 255)
    for x0 in (4, 8, 11):
        h = max(2, height - rnd.randint(0, 2))
        for y in range(15, 15 - h, -1):
            px(img, x0 + (1 if (y // 3) % 2 else 0), y, dark if y > 12 else green)
        for i in range(leaves):
            ly = 15 - rnd.randint(1, max(1, h - 1))
            side = -1 if i % 2 else 1
            px(img, x0 + side, ly, green)
            px(img, x0 + 2 * side, ly - 1, shade(green, 1.15))
        for i in range(pods):
            py = 15 - rnd.randint(2, max(2, h - 1))
            for dy in range(3):
                px(img, x0 + 2, py + dy, shade(pod_color, 1.0 - dy * 0.1))


def cabbage_head(img, size, rnd):
    leaf, light = (110, 170, 70, 255), (170, 214, 120, 255)
    cx, cy = 8, 15 - size
    for y in range(-size, size + 1):
        for x in range(-size - 1, size + 2):
            if x * x / ((size + 1) ** 2) + y * y / (size ** 2) <= 1.0:
                c = light if (x + y) % 3 == 0 else leaf
                px(img, cx + x, cy + y, shade(c, 0.9 + rnd.random() * 0.15))
    for x in range(-size - 2, size + 3, 2):
        px(img, cx + x, 15, (70, 120, 45, 255))


def draw_textures(force: bool) -> list[str]:
    made = []
    tex_block, tex_item = ASSETS / "textures/block", ASSETS / "textures/item"
    tex_block.mkdir(parents=True, exist_ok=True)
    tex_item.mkdir(parents=True, exist_ok=True)

    def save(img, path: Path):
        if path.exists() and not force:
            return
        img.save(path)
        made.append(str(path.relative_to(ROOT)))

    for stage in range(4):
        rnd = random.Random(100 + stage)
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        stalk(img, rnd, 4 + stage * 3, 1 + stage, 0 if stage < 3 else 3, (200, 120, 70, 255))
        save(img, tex_block / f"bean_crop_stage{stage}.png")
        rnd = random.Random(200 + stage)
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        if stage < 2:
            stalk(img, rnd, 3 + stage * 2, 1 + stage, 0, (0, 0, 0, 255))
        else:
            cabbage_head(img, 2 + stage, rnd)
        save(img, tex_block / f"cabbage_crop_stage{stage}.png")

    rnd = random.Random(7)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x, y in ((3, 4), (8, 3), (5, 8), (10, 8), (4, 12), (9, 12)):
        bean(img, x, y, (196, 112, 62, 255), rnd)
    save(img, tex_item / "beans.png")

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x, y in ((4, 5), (9, 4), (6, 9), (11, 10), (3, 11)):
        px(img, x, y, (150, 190, 90, 255))
        px(img, x + 1, y, (120, 160, 70, 255))
        px(img, x, y + 1, (100, 140, 60, 255))
    save(img, tex_item / "cabbage_seeds.png")

    rnd = random.Random(9)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(2, 15):
        for x in range(1, 15):
            dx, dy = (x - 7.5) / 6.5, (y - 8.5) / 6.5
            if dx * dx + dy * dy <= 1:
                c = (170, 214, 120, 255) if (x * 3 + y) % 5 == 0 else (112, 172, 72, 255)
                if dx * dx + dy * dy > 0.7:
                    c = (78, 130, 50, 255)
                px(img, x, y, shade(c, 0.9 + rnd.random() * 0.15))
    for y in range(4, 13):
        px(img, 7 + (y % 2), y, (190, 226, 150, 255))
    save(img, tex_item / "cabbage.png")

    rnd = random.Random(11)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    wood, dark = (150, 104, 62, 255), (104, 70, 40, 255)
    for x in range(2, 14):
        for y in range(8, 14):
            if (x - 7.5) ** 2 / 36 + (y - 8) ** 2 / 30 <= 1:
                px(img, x, y, wood if y < 12 else dark)
    for x in range(2, 14):
        px(img, x, 8, dark)
    for x, y in ((3, 6), (6, 5), (9, 6), (5, 7), (8, 7)):
        bean(img, x, y, (186, 96, 52, 255), rnd)
    save(img, tex_item / "bowl_of_beans.png")
    return made


# ---------------------------------------------------------------- JSON


def models_and_states() -> None:
    for crop, (seed, produce, _) in CROPS.items():
        for stage in range(4):
            write(ASSETS / f"models/block/{crop}_stage{stage}.json", {
                "parent": "minecraft:block/crop",
                # NeoForge/Forge read render_type; Fabric sets the layer in code; 26.x sees the transparency itself
                "render_type": "minecraft:cutout",
                "textures": {"crop": f"{NS}:block/{crop}_stage{stage}"},
            })
        write(ASSETS / f"blockstates/{crop}.json", {
            "variants": {f"age={age}": {"model": f"{NS}:block/{crop}_stage{STAGE_OF_AGE[age]}"} for age in range(8)}
        })
    for item in ITEMS:
        write(ASSETS / f"models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{item}"}})
        # 1.21.4+ item model definitions (older versions ignore the folder)
        write(ASSETS / f"items/{item}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{item}"}})


def crop_loot(crop: str, seed: str, produce: str) -> dict:
    ripe = {"condition": "minecraft:block_state_property", "block": f"{NS}:{crop}", "properties": {"age": "7"}}
    bonus = {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
             "formula": "minecraft:binomial_with_bonus_count", "parameters": {"extra": 2, "probability": 0.5714286}}
    pools = [{
        "rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{NS}:{produce}", "conditions": [ripe]},
            {"type": "minecraft:item", "name": f"{NS}:{seed}"},
        ]}],
    }, {
        "rolls": 1.0, "bonus_rolls": 0.0, "conditions": [ripe],
        "entries": [{"type": "minecraft:item", "name": f"{NS}:{seed}", "functions": [bonus]}],
    }]
    return {"type": "minecraft:block", "functions": [{"function": "minecraft:explosion_decay"}], "pools": pools,
            "random_sequence": f"{NS}:blocks/{crop}"}


def data() -> None:
    for crop, (seed, produce, _) in CROPS.items():
        write(DATA / f"loot_table/blocks/{crop}.json", crop_loot(crop, seed, produce))
    write(DATA / "recipe/bowl_of_beans.json", {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["minecraft:bowl", f"{NS}:beans", f"{NS}:beans", f"{NS}:beans"],
        "result": {"count": 1, "id": f"{NS}:bowl_of_beans"},
    })
    write(DATA / "gas_foods/vanilla.json", {"foods": GAS_VANILLA})
    write(DATA / "gas_foods/fart_together.json", {"foods": GAS_OWN})


def main() -> int:
    made = draw_textures("--textures" in sys.argv)
    models_and_states()
    data()
    print("textures drawn:", *made, sep="\n  ") if made else print("textures kept")
    return 0


if __name__ == "__main__":
    sys.exit(main())
