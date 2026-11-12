"""Textures and the mod icon drawn by Codex CLI, projected to the pixel grid here, chosen by the owner.

    python -I tools/art.py generate <group> [n ...]       # one Codex image per variant -> art/textures/candidates/<group>/raw-<n>.png
    python -I tools/art.py project [group ...]            # raw-<n>.png -> candidates/<group>/<n>/<name>.png (pixel grid, palette)
    python -I tools/art.py sheet [group ...]              # candidates/<group>/compare.png: every variant side by side, enlarged
    python -I tools/art.py install                        # art/textures/slots.json -> the mod's textures (+ GasIconArt.java)

Groups: `items` (beans, cabbage seeds, cabbage, bowl of beans, the gas meter icon empty and full), `crops` (bean and
cabbage crop, stages 0-3), `icon` (the mod icon).

How (after the monorepo's SkinGen, ai-proxy/tools/SkinGen): Codex draws one sheet per group, the sprites in a grid
on a flat magenta backdrop. The code cuts the sheet into its cells, keys the backdrop out, measures each sprite and
resamples it to the texture's pixel grid: every texture pixel takes the colour most of its source pixels vote for
from a small palette of the sprite (k-means), so nothing is anti-aliased or blurred and no pixel is placed by a
language model. Crops share one scale per row (the ripe stage fills the texture, young stages stay small) and stand
on the bottom row. The icon is a picture, not a texture: it is only downscaled (512 for the catalogues, 256 in the
jar), like Footprints'. (A first try painted on a template with one checker square per pixel, SkinGen's best
approach for skins; Codex restyled the whole page with a dark glow instead, see candidates/items/rejected-1.png.)

Budget: the Codex subscription allows ~12 image generations per ~5 hours. One call draws a whole sheet, so a full
round is 3 groups x 3 variants = 9 calls. `generate` never redraws a raw that exists (delete it to redo).

Swapping a group's variant is one line in art/textures/slots.json (`"items": 2`) followed by `install`.
"""
from __future__ import annotations

import json
import os
import shutil
import subprocess
import sys
import time
from pathlib import Path

import numpy as np
from PIL import Image
from scipy import ndimage as ndi

ROOT = Path(__file__).resolve().parent.parent
NS = "farmstead_fart_together"
ASSETS = ROOT / "src/main/resources/assets" / NS
ART = ROOT / "art" / "textures"
CAND = ART / "candidates"
SLOTS = ART / "slots.json"
BUILD = ROOT / "build" / "art"
JAVA_ICON = ROOT / "src/main/java/online/farmsteadminecraft/farttogether/client/GasIconArt.java"
PALETTE = 12
SAMPLE = os.environ.get("ART_SAMPLE", "vote")  # vote | mean (mean lets backdrop-tinted edge pixels in)

CODEX = os.environ.get("CODEX_EXE") or str(Path(os.environ.get("LOCALAPPDATA", "")) / "Programs/OpenAI/Codex/bin/codex.exe")
ALL_OFF = ["shell_tool", "unified_exec", "unified_exec_tty", "image_generation", "view_image", "multi_agent", "apps",
           "plugins", "browser_use", "browser_use_external", "computer_use", "goals", "sleep_tool", "skill_search",
           "tool_suggest", "in_app_browser", "code_mode_host", "hooks", "shell_snapshot", "realtime_conversation",
           "remote_plugin", "mentions_v2", "skill_mcp_dependency_install", "tool_call_mcp_elicitation"]

# A sprite: name, texture size in pixels, where it goes in the mod. Cells left to right, top to bottom.
GROUPS: dict[str, dict] = {
    "items": {
        "cols": 3, "rows": 2,
        "sprites": [
            ("beans", 16, "textures/item/beans.png"),
            ("cabbage_seeds", 16, "textures/item/cabbage_seeds.png"),
            ("cabbage", 16, "textures/item/cabbage.png"),
            ("bowl_of_beans", 16, "textures/item/bowl_of_beans.png"),
            ("gas_icon", 9, "textures/gui/gas_icon.png"),
            ("gas_icon_full", 9, "textures/gui/gas_icon_full.png"),
        ],
    },
    "crops": {
        "cols": 4, "rows": 2,
        # crops stand on the bottom row of their texture
        "bottom": True,
        "sprites": [(f"{c}_crop_stage{s}", 16, f"textures/block/{c}_crop_stage{s}.png") for c in ("bean", "cabbage") for s in range(4)],
    },
    "icon": {"picture": True},
}
# the variants differ only in the art direction line, so the owner sees three real choices
VARIANT_NOTE = {
    1: "Art direction: exactly like vanilla Minecraft 1.21 item and crop textures (same saturation, same shading style, dark outline pixels on the edge). Keep the background flat magenta.",
    2: "Art direction: a little brighter and more cheerful than vanilla, cartoon-like, still a Minecraft texture with a dark outline. Keep the background flat magenta.",
    3: "Art direction: soft and natural, muted earthy colours, like a classic Minecraft farming mod (Farmer's Delight style). Keep the background flat magenta.",
}


# ---------------------------------------------------------------- Codex

ICON_NOTE = {
    1: "Scene: the three friends seen from behind on a grassy hill at night.",
    2: "Scene: the three friends around a small campfire, seen from the side, the cloud glowing over the fire.",
    3: "Scene: a close-up, the three friends' heads and shoulders peeking out from the bottom edge, the big green cloud above them with a happy cartoon face drawn into the cloud.",
}


def ask_image(tag: str, system: str, user: str, images: list[Path]) -> Path:
    """One stateless `codex exec` with only the image tool on; returns the generated picture (copied next to the call)."""
    call = BUILD / "calls" / tag
    call.mkdir(parents=True, exist_ok=True)
    (call / "user.txt").write_text(user, encoding="utf-8")
    sys_file = call / "system.txt"
    sys_file.write_text(system, encoding="utf-8")
    off = [f for f in ALL_OFF if f not in ("image_generation", "code_mode_host")]
    args = [CODEX, "exec", "-m", "gpt-6-luna", "-c", "model_reasoning_effort=low",
            "-c", f'model_instructions_file="{sys_file.as_posix()}"', "-c", 'web_search="disabled"',
            "-c", "include_apply_patch_tool=false", "--ignore-user-config", "--ephemeral", "--skip-git-repo-check",
            "-s", "read-only", "-C", str(call), "--json", "-o", str(call / "reply.txt")]
    for f in off:
        args += ["--disable", f]
    for i in images:
        args += ["-i", str(i)]
    args.append("-")
    t0 = time.time()
    r = subprocess.run(args, input=user, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=900)
    (call / "events.jsonl").write_text(r.stdout, encoding="utf-8")
    events = []
    for line in r.stdout.splitlines():
        try:
            events.append(json.loads(line))
        except ValueError:
            pass
    thread = next((e["thread_id"] for e in events if "thread_id" in e), None)
    (call / "meta.json").write_text(json.dumps({"tag": tag, "s": round(time.time() - t0), "thread": thread,
                                                "rc": r.returncode, "stderr": r.stderr[-2000:]}, indent=1), encoding="utf-8")
    gdir = Path.home() / ".codex" / "generated_images" / (thread or "-")
    pics = sorted(gdir.glob("*.png")) if gdir.exists() else []
    if not pics:
        tail = (r.stderr or "")[-600:] + "\n" + "\n".join(json.dumps(e)[:300] for e in events[-4:])
        raise RuntimeError(f"{tag}: no image generated (rc {r.returncode})\n{tail}")
    dst = call / "image.png"
    shutil.copyfile(pics[-1], dst)
    return dst


# The drawing model rewrites a prompt in its own words unless told not to, and the first two sheets came back on a
# dark glowing backdrop instead of the flat one asked for: the request goes to the image tool word for word.
VERBATIM = (" Pass the user's whole message to the image tool as the prompt, word for word: do not shorten, rephrase "
            "or add style words.")
SYSTEM = ("You draw pixel-art texture sheets for a Minecraft mod. Use your image generation tool once, then reply "
          "\"done\"." + VERBATIM + " Never add a dark background or a glow: the sheet's backdrop is flat magenta.")


def prompt(group: str, variant: int) -> str:
    text = (ART / f"{group}.prompt.txt").read_text(encoding="utf-8").strip()
    return f"{text}\n\n{VARIANT_NOTE[variant]}"


def generate(group: str, variants: list[int]) -> None:
    out_dir = CAND / group
    out_dir.mkdir(parents=True, exist_ok=True)
    for v in variants:
        raw = out_dir / f"raw-{v}.png"
        if raw.exists():
            print(f"{group} {v}: kept {raw.relative_to(ROOT)}")
            continue
        images: list[Path] = []
        system = SYSTEM if not GROUPS[group].get("picture") else "You draw square app icons for Minecraft mods. Use your image generation tool once, then reply \"done\"." + VERBATIM
        t0 = time.time()
        pic = ask_image(f"{group}-{v}", system, prompt(group, v), images)
        shutil.copyfile(pic, raw)
        print(f"{group} {v}: {raw.relative_to(ROOT)} ({Image.open(raw).size[0]}x{Image.open(raw).size[1]}, {time.time() - t0:.0f} s)")


# ---------------------------------------------------------------- projection


def background(img: np.ndarray) -> np.ndarray:
    """The flat backdrop colour: per-channel median of the four corner patches."""
    k = 24
    patches = [img[:k, :k], img[:k, -k:], img[-k:, :k], img[-k:, -k:]]
    return np.median(np.concatenate([p.reshape(-1, 3) for p in patches]), axis=0)


def ink_masks(img: np.ndarray, bg: np.ndarray, cols: int, rows: int) -> list[np.ndarray]:
    """Painted pixels of every cell (left to right, top to bottom), as full-image masks. A sprite may cross its cell's
    border (a ripe bush wider than its cell), so each connected piece goes to the cell that holds its centre (the
    pieces are not merged further: neighbouring sprites can be only a few pixels apart). Backdrop enclosed by a sprite stays backdrop: the gaps between stems are transparent."""
    d = np.abs(img.astype(np.float32) - bg[None, None, :]).sum(-1)
    ink = ndi.binary_opening(d > 90, np.ones((3, 3)))
    groups, n = ndi.label(ndi.binary_dilation(ink, np.ones((5, 5))))
    hgt, wid = ink.shape
    masks = [np.zeros_like(ink) for _ in range(cols * rows)]
    for i, sl in enumerate(ndi.find_objects(groups), start=1):
        part = (groups == i) & ink
        if part.sum() < 60:
            continue
        ys, xs = np.where(part)
        c = min(cols - 1, int(xs.mean() / (wid / cols)))
        r = min(rows - 1, int(ys.mean() / (hgt / rows)))
        masks[r * cols + c] |= part
    return masks


def kmeans(px: np.ndarray, k: int, iters: int = 12) -> np.ndarray:
    px = px.astype(np.float32)
    if len(px) <= k:
        return px
    rng = np.random.default_rng(1)
    centres = px[rng.choice(len(px), k, replace=False)]
    for _ in range(iters):
        d = ((px[:, None, :] - centres[None, :, :]) ** 2).sum(-1)
        lab = d.argmin(1)
        for j in range(k):
            m = lab == j
            if m.any():
                centres[j] = px[m].mean(0)
    return centres


def project_cell(cell: np.ndarray, mask: np.ndarray, scale: float, n: int, bottom: bool) -> np.ndarray:
    """The painted sprite of one cell as an n x n RGBA texture; `scale` = source px per texture pixel.

    Image models draw "pixel art" whose own pixels are a few source px off a regular grid: every texture pixel takes
    the palette colour most of its inner source pixels vote for (the palette is k-means over the sprite), and is
    transparent when most of it is backdrop."""
    out = np.zeros((n, n, 4), np.uint8)
    ys, xs = np.where(mask)
    if len(xs) == 0:
        return out
    x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
    tw, th = min(n, max(1, round((x1 - x0) / scale))), min(n, max(1, round((y1 - y0) / scale)))
    sx, sy = (x1 - x0) / tw, (y1 - y0) / th
    ink = cell[mask]
    pal = kmeans(ink[:: max(1, len(ink) // 6000)], PALETTE)
    ox = (n - tw) // 2
    oy = n - th if bottom else (n - th) // 2
    for ty in range(th):
        for tx in range(tw):
            ax, bx = x0 + tx * sx, x0 + (tx + 1) * sx
            ay, by = y0 + ty * sy, y0 + (ty + 1) * sy
            mx, my = (bx - ax) * 0.2, (by - ay) * 0.2
            xa, xb = int(ax + mx), max(int(ax + mx) + 1, int(bx - mx))
            ya, yb = int(ay + my), max(int(ay + my) + 1, int(by - my))
            m = mask[ya:yb, xa:xb]
            if m.mean() < 0.5:
                continue
            p = cell[ya:yb, xa:xb][m].astype(np.float32)
            lab = ((p[:, None, :] - pal[None]) ** 2).sum(-1).argmin(1)
            if SAMPLE == "mean":
                # the area mean snapped to the palette: smoother than a vote when one texel covers 1.5 drawn pixels
                c = pal[((p.mean(0)[None] - pal) ** 2).sum(-1).argmin()]
            else:
                c = pal[np.bincount(lab).argmax()]
            out[oy + ty, ox + tx, :3] = np.clip(c, 0, 255)
            out[oy + ty, ox + tx, 3] = 255
    return out


def icon_backdrop(pic: Image.Image) -> Image.Image:
    """The icon on the dark night backdrop its prompt asks for. Codex sometimes ignores it and returns a transparent
    picture or a flat chroma-key colour: those get the backdrop painted in (radial #2a3320 -> #14180f)."""
    rgba = np.asarray(pic.convert("RGBA")).astype(np.float32)
    rgb, alpha = rgba[..., :3], rgba[..., 3] / 255
    corners = np.concatenate([rgb[:16, :16].reshape(-1, 3), rgb[-16:, -16:].reshape(-1, 3)])
    flat = corners.std(0).max() < 6
    key = corners.mean(0)
    if alpha.min() < 0.99:
        pass  # transparent already
    elif flat and (key[1] < 60 and key[0] > 180 and key[2] > 180):
        # magenta: soft key and despill (the green cloud must not keep a pink rim)
        d = np.abs(rgb - key).sum(-1)
        alpha = np.clip((d - 60) / 120, 0, 1)
        edge = ndi.binary_dilation(alpha < 0.98, np.ones((5, 5)))
        rgb[..., 0] = np.where(edge, np.minimum(rgb[..., 0], rgb[..., 1] + 60), rgb[..., 0])
        rgb[..., 2] = np.where(edge, np.minimum(rgb[..., 2], rgb[..., 1] + 60), rgb[..., 2])
    else:
        return pic.convert("RGBA")
    h, w = alpha.shape
    yy, xx = np.mgrid[0:h, 0:w]
    r = np.clip(np.hypot(xx - w / 2, yy - h / 2) / (0.72 * w), 0, 1)[..., None]
    back = np.array([42, 51, 32]) * (1 - r) + np.array([20, 24, 15]) * r
    out = rgb * alpha[..., None] + back * (1 - alpha[..., None])
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def project(group: str) -> list[Path]:
    g = GROUPS[group]
    made = []
    for raw in sorted((CAND / group).glob("raw-*.png")):
        v = raw.stem.split("-")[1]
        out_dir = CAND / group / v
        out_dir.mkdir(parents=True, exist_ok=True)
        if g.get("picture"):
            pic = icon_backdrop(Image.open(raw))
            side = min(pic.size)
            pic = pic.crop(((pic.width - side) // 2, (pic.height - side) // 2, (pic.width + side) // 2, (pic.height + side) // 2))
            for s in (512, 256):
                pic.resize((s, s), Image.LANCZOS).save(out_dir / f"icon-{s}.png")
            made.append(out_dir)
            continue
        img = np.asarray(Image.open(raw).convert("RGB"))
        masks = ink_masks(img, background(img), g["cols"], g["rows"])
        parts = []
        for i, (name, n, _) in enumerate(g["sprites"]):
            mask = masks[i]
            ys, xs = np.where(mask)
            size = max(xs.max() - xs.min() + 1, ys.max() - ys.min() + 1) if len(xs) else 1
            parts.append((name, n, i // g["cols"], img, mask, size))
        for name, n, r, cell, mask, size in parts:
            if g.get("bottom"):
                # crops share one scale per row (the ripe stage fills the texture), so young stages stay small
                scale = max(p[5] / p[1] for p in parts if p[2] == r)
            else:
                scale = size / n
            spr = project_cell(cell, mask, scale, n, g.get("bottom", False))
            Image.fromarray(spr, "RGBA").save(out_dir / f"{name}.png")
        made.append(out_dir)
    return made


# ---------------------------------------------------------------- comparison sheet and install


def sheet(group: str) -> Path | None:
    g = GROUPS[group]
    variants = sorted(p for p in (CAND / group).iterdir() if p.is_dir()) if (CAND / group).exists() else []
    if not variants:
        return None
    if g.get("picture"):
        tiles = [Image.open(v / "icon-256.png").convert("RGBA") for v in variants]
        smalls = [Image.open(v / "icon-256.png").convert("RGBA").resize((64, 64), Image.LANCZOS) for v in variants]
        out = Image.new("RGBA", (len(tiles) * 276 + 20, 276 + 84), (40, 40, 40, 255))
        for i, (t, s) in enumerate(zip(tiles, smalls)):
            out.paste(t, (20 + i * 276, 20), t)
            out.paste(s, (20 + i * 276, 286), s)
        path = CAND / group / "compare.png"
        out.save(path)
        return path
    names = [s[0] for s in g["sprites"]]
    scale, pad = 8, 12
    # each row: one variant; each column: one sprite at 8x, plus the vanilla-like backdrop the game shows it on
    tile = 16 * scale
    out = Image.new("RGBA", (pad + len(names) * (tile + pad), pad + len(variants) * (tile + pad)), (139, 139, 139, 255))
    for r, v in enumerate(variants):
        for c, name in enumerate(names):
            spr = Image.open(v / f"{name}.png").convert("RGBA")
            big = spr.resize((spr.width * scale * 16 // spr.width, spr.height * scale * 16 // spr.width), Image.NEAREST)
            x, y = pad + c * (tile + pad), pad + r * (tile + pad)
            backdrop = Image.new("RGBA", (tile, tile), (198, 198, 198, 255) if c % 2 == 0 else (178, 178, 178, 255))
            out.paste(backdrop, (x, y))
            out.paste(big, (x, y), big)
    path = CAND / group / "compare.png"
    out.save(path)
    return path


def slots() -> dict:
    return {k: v for k, v in json.loads(SLOTS.read_text(encoding="utf-8")).items() if not k.startswith("_")}


def install() -> None:
    for group, v in slots().items():
        g, src = GROUPS[group], CAND / group / str(v)
        if g.get("picture"):
            shutil.copyfile(src / "icon-256.png", ASSETS / "icon.png")
            media = ROOT / "docs" / "media"
            media.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(src / "icon-512.png", media / "icon-512.png")
            print(f"icon: variant {v}")
            continue
        for name, _, dest in g["sprites"]:
            (ASSETS / dest).parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(src / f"{name}.png", ASSETS / dest)
        print(f"{group}: variant {v}")
    write_icon_java()


def write_icon_java() -> None:
    """The HUD draws the gas icon pixel by pixel with fill (the only drawing call every version 1.20.1..26.x shares),
    so the installed icon is also baked into a Java table."""
    rows = []
    for name in ("gas_icon", "gas_icon_full"):
        img = Image.open(ASSETS / f"textures/gui/{name}.png").convert("RGBA")
        vals = [f"0x{a:02X}{r:02X}{g:02X}{b:02X}" for (r, g, b, a) in img.getdata()]
        lines = ",\n            ".join(", ".join(vals[i:i + img.width]) for i in range(0, len(vals), img.width))
        rows.append(f"    /** {name}.png, ARGB, row by row. */\n    static final int[] {name.upper()} = {{\n            {lines}\n    }};")
    size = Image.open(ASSETS / "textures/gui/gas_icon.png").width
    JAVA_ICON.write_text(
        "package online.farmsteadminecraft.farttogether.client;\n\n"
        "/** Generated by tools/art.py install from textures/gui/gas_icon*.png: do not edit. */\n"
        "final class GasIconArt {\n"
        f"    static final int SIZE = {size};\n\n" + "\n\n".join(rows) + "\n\n"
        "    private GasIconArt() {\n    }\n}\n", encoding="utf-8", newline="\n")
    print(f"wrote {JAVA_ICON.relative_to(ROOT)}")


def main(argv: list[str]) -> None:
    if not argv:
        print(__doc__)
        return
    cmd, rest = argv[0], argv[1:]
    if cmd == "generate":
        generate(rest[0], [int(v) for v in rest[1:]] or [1, 2, 3])
    elif cmd == "project":
        for gname in rest or list(GROUPS):
            for d in project(gname):
                print(d.relative_to(ROOT))
    elif cmd == "sheet":
        for gname in rest or list(GROUPS):
            p = sheet(gname)
            if p:
                print(p.relative_to(ROOT))
    elif cmd == "install":
        install()
    else:
        sys.exit(f"unknown command {cmd}")


if __name__ == "__main__":
    main(sys.argv[1:])
