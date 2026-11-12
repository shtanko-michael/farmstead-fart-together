"""Brand media from the chosen icon and recorded game frames (no other tools needed):

    python -I tools/brand.py logo                 # docs/media/logo.png: icon + pixel wordmark + "by Farmstead"
    python -I tools/brand.py card                 # docs/media/social-card.png 1280x640 (GitHub social preview)
    python -I tools/brand.py banner <clip> [...]  # docs/media/banner.gif 800x300 + banner.webp 1280x480 from
                                                  # run/<target>/client*/recordings/<clip>/ frames, logo on top
    python -I tools/brand.py shots <target>       # docs/media/gallery/*.jpg from the two-client scene's screenshots
    python -I tools/brand.py gallery              # build/curseforge-gallery: listing/modrinth.json's gallery,
                                                  # numbered in upload order, with CAPTIONS.md

The wordmark is drawn here with a 5x7 pixel font (Minecraft-like, no font files), in the icon's lime green with a
dark outline.
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
MEDIA = ROOT / "docs" / "media"
ICON = MEDIA / "icon-512.png"
LIME, LIME_DARK, OUTLINE, CREAM = (182, 227, 106), (111, 168, 52), (20, 24, 15), (240, 236, 214)

FONT = {
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "B": ["11110", "10001", "10001", "11110", "10001", "10001", "11110"],
    "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
    "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    "F": ["11111", "10000", "10000", "11110", "10000", "10000", "10000"],
    "G": ["01111", "10000", "10000", "10011", "10001", "10001", "01111"],
    "H": ["10001", "10001", "10001", "11111", "10001", "10001", "10001"],
    "M": ["10001", "11011", "10101", "10101", "10001", "10001", "10001"],
    "O": ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
    "R": ["11110", "10001", "10001", "11110", "10100", "10010", "10001"],
    "S": ["01111", "10000", "10000", "01110", "00001", "00001", "11110"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
    "Y": ["10001", "10001", "01010", "00100", "00100", "00100", "00100"],
    " ": ["000", "000", "000", "000", "000", "000", "000"],
}


def text_image(text: str, px: int, fill, shade=None) -> Image.Image:
    """Pixel text, each font pixel `px` wide, a 1-pixel dark outline and an optional darker lower half."""
    cols = sum(len(FONT[c][0]) + 1 for c in text) + 1
    grid = Image.new("RGBA", (cols + 2, 7 + 2 + 1), (0, 0, 0, 0))
    x = 1
    for c in text:
        rows = FONT[c]
        for y, row in enumerate(rows):
            for dx, bit in enumerate(row):
                if bit == "1":
                    grid.putpixel((x + dx, y + 1), fill if shade is None or y < 4 else shade)
        x += len(rows[0]) + 1
    # outline: every empty pixel next to a letter pixel
    out = grid.copy()
    for yy in range(grid.height):
        for xx in range(grid.width):
            if grid.getpixel((xx, yy))[3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (0, 2)):
                nx, ny = xx - dx, yy - dy
                if 0 <= nx < grid.width and 0 <= ny < grid.height and grid.getpixel((nx, ny))[3]:
                    out.putpixel((xx, yy), OUTLINE + (255,))
                    break
    return out.resize((out.width * px, out.height * px), Image.NEAREST)


def logo() -> Image.Image:
    icon = Image.open(ICON).convert("RGBA").resize((220, 220), Image.LANCZOS)
    mask = Image.new("L", icon.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, 219, 219), 36, fill=255)
    title = text_image("FART TOGETHER", 7, LIME + (255,), LIME_DARK + (255,))
    by = text_image("BY FARMSTEAD", 3, CREAM + (255,))
    w = 220 + 30 + max(title.width, by.width)
    img = Image.new("RGBA", (w + 20, 240), (0, 0, 0, 0))
    img.paste(icon, (10, 10), mask)
    img.alpha_composite(title, (260, 40))
    img.alpha_composite(by, (264, 60 + title.height))
    MEDIA.mkdir(parents=True, exist_ok=True)
    img.save(MEDIA / "logo.png")
    return img


def card() -> None:
    lg = logo()
    img = Image.new("RGBA", (1280, 640), (20, 24, 15, 255))
    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse((240, 60, 1040, 580), fill=(60, 80, 30, 255))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(120)))
    scale = 1100 / lg.width
    lg = lg.resize((int(lg.width * scale), int(lg.height * scale)), Image.NEAREST)
    img.alpha_composite(lg, ((1280 - lg.width) // 2, (640 - lg.height) // 2))
    img.convert("RGB").save(MEDIA / "social-card.png")


def frames(clip: str) -> list[Path]:
    for d in sorted((ROOT / "run").glob(f"*/client*/recordings/{clip}")):
        f = sorted(d.glob("*.png"))
        if f:
            return f
    raise SystemExit(f"no frames for {clip}")


def banner(clips: list[str]) -> None:
    lg = logo()
    seq = []
    for c in clips:
        seq += frames(c)
    seq = seq[::2]  # 10 fps is plenty for a banner
    big, small = [], []
    for f in seq:
        fr = Image.open(f).convert("RGBA")
        for size, out in (((1280, 480), big), ((800, 300), small)):
            w, h = size
            k = max(w / fr.width, h / fr.height)
            im = fr.resize((int(fr.width * k), int(fr.height * k)), Image.LANCZOS)
            im = im.crop(((im.width - w) // 2, (im.height - h) // 2, (im.width + w) // 2, (im.height + h) // 2))
            shade = Image.new("RGBA", size, (0, 0, 0, 0))
            ImageDraw.Draw(shade).rectangle((0, 0, int(w * 0.55), h), fill=(10, 12, 8, 120))
            im.alpha_composite(shade.filter(ImageFilter.GaussianBlur(40)))
            s = (w * 0.5) / lg.width
            l2 = lg.resize((int(lg.width * s), int(lg.height * s)), Image.LANCZOS)
            im.alpha_composite(l2, (int(w * 0.03), (h - l2.height) // 2))
            out.append(im.convert("RGB"))
    big[0].save(MEDIA / "banner.webp", save_all=True, append_images=big[1:], duration=100, loop=0, quality=80)
    # CurseForge: keep the GIF near 4 MB (every other frame at 5 fps, a smaller palette)
    pal = [im.quantize(colors=96, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE) for im in small[::2]]
    pal[0].save(MEDIA / "banner.gif", save_all=True, append_images=pal[1:], duration=200, loop=0, optimize=True)
    for name in ("banner.webp", "banner.gif"):
        print(name, (MEDIA / name).stat().st_size // 1024, "KB")


# gallery picture <- screenshot of tools/duo.py (build/scenes/<target>/duo/<name>.png)
SHOTS = {
    "01-together.jpg": "16-b-together", "02-toot.jpg": "1-a-toots-seen-by-b", "03-scare.jpg": "7-b-mobs-run",
    "04-golden.jpg": "14-b-golden", "05-hold.jpg": "11-a-hold-full", "06-crops.jpg": "8-b-crops",
    "07-my-style.jpg": "10-a-my-style", "08-bubbles.jpg": "15-a-bubbles",
}


def shots(target: str) -> None:
    out = MEDIA / "gallery"
    out.mkdir(parents=True, exist_ok=True)
    for name, shot in SHOTS.items():
        Image.open(ROOT / "build" / "scenes" / target / "duo" / f"{shot}.png").convert("RGB").save(out / name, quality=90)
        print(out / name)


def gallery() -> None:
    import json
    import shutil
    listing = json.loads((ROOT / "listing" / "modrinth.json").read_text(encoding="utf-8"))
    out = ROOT / "build" / "curseforge-gallery"
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)
    rows = ["# CurseForge gallery: upload in this order (Images -> Upload)", "", "| # | File | Title | Description |",
            "| --- | --- | --- | --- |"]
    for i, g in enumerate(listing["gallery"], 1):
        src = ROOT / g["file"]
        name = f"{i:02d}-{src.name}"
        shutil.copyfile(src, out / name)
        rows.append(f"| {i} | {name} | {g['title']} | {g['description']} |")
    (out / "CAPTIONS.md").write_text("\n".join(rows) + "\n", encoding="utf-8")
    print(out)


def main(argv: list[str]) -> None:
    if not argv:
        print(__doc__)
    elif argv[0] == "logo":
        logo()
    elif argv[0] == "card":
        card()
    elif argv[0] == "banner":
        banner(argv[1:])
    elif argv[0] == "shots":
        shots(argv[1] if len(argv) > 1 else "1.21.1-neoforge")
    elif argv[0] == "gallery":
        gallery()


if __name__ == "__main__":
    main(sys.argv[1:])
