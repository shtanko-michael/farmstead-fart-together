"""The `cosmetic` server preset in game: the toot only looks and sounds.

    python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge cosmetic

With `preset = "cosmetic"` in the server file: a toot needs no gas, mobs behind the player do not run, a toot in
mid-air does not push the player up. Writes cosmetic-*.png and cosmetic-report.txt to build/scenes/<target>/.
"""
import re
import time

Y = 100
SERVER_CFG = "farmstead_fart_together-server.toml"


def hook(ctx, name, text=""):
    d = ctx.client_dir / "testhooks"
    d.mkdir(parents=True, exist_ok=True)
    (d / name).write_text(text, encoding="utf-8")


def log(ctx):
    path = ctx.out_dir / "client.log"
    return path.read_text(encoding="utf-8", errors="replace") if path.exists() else ""


def pos(ctx, selector):
    m = re.search(r"\[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]", ctx.cmd(f"data get entity {selector} Pos") or "")
    return tuple(float(v) for v in m.groups()) if m else None


def run(ctx):
    p = ctx.player
    lines = []
    cfg = ctx.client_dir.parent / "server" / "config" / SERVER_CFG
    original = cfg.read_text(encoding="utf-8")
    text = re.sub(r'(?m)^preset = "\w+"$', 'preset = "cosmetic"', original)
    if 'preset = "cosmetic"' not in text:
        text = 'preset = "cosmetic"\n' + text
    cfg.write_text(text, encoding="utf-8")
    try:
        ctx.cmds("gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false", "weather clear",
                 "difficulty easy", "effect clear @a", f"gamemode survival {p}", f"tp {p} 0.5 {Y} 0.5 0 10")
        ctx.wait(2)
        ctx.cmds(f"fill -8 {Y - 1} -8 8 {Y - 1} 8 grass_block", f"fill -8 {Y} -8 8 {Y + 8} 8 air", "kill @e[type=!player]")
        lines.append("fart reload: " + (ctx.cmd("fart reload") or "").strip()[:120])
        lines.append("fart info: " + (ctx.cmd("fart info") or "").strip()[:200])
        ctx.wait(1.5)

        print("1. no gas needed")
        ctx.cmd(f"fart charge {p} 0")
        ctx.wait(1)
        before = len(log(ctx))
        hook(ctx, "toot", "800")
        ctx.wait(2)
        played = re.findall(r"test: toot by \w+ .* power=(\d)", log(ctx)[before:])
        lines.append(f"toot with 0 gas: {'played (power ' + played[-1] + ')' if played else 'REFUSED'}")
        ctx.wait(3.2)

        print("2. mobs stay")
        for kind, x, z in (("zombie", -0.5, -2.5), ("cow", 1.5, -2.5)):
            ctx.cmd(f"summon minecraft:{kind} {x} {Y} {z} {{PersistenceRequired:1b,Tags:[\"cz\"]}}")
        ctx.wait(1.5)
        before_pos = {k: pos(ctx, f"@e[type=minecraft:{k},tag=cz,limit=1]") for k in ("zombie", "cow")}
        hook(ctx, "camera", "back")
        hook(ctx, "toot", "1600")
        ctx.wait(3.5)
        ctx.shot("cosmetic-cloud-mobs-stay")
        after_pos = {k: pos(ctx, f"@e[type=minecraft:{k},tag=cz,limit=1]") for k in ("zombie", "cow")}
        for k in before_pos:
            b, a = before_pos[k], after_pos[k]
            moved = ((a[0] - b[0]) ** 2 + (a[2] - b[2]) ** 2) ** 0.5 if a and b else -1
            lines.append(f"{k} moved {moved:.2f} blocks after a full toot next to it (a scared mob runs 7+)")
        ctx.cmd("kill @e[tag=cz]")
        ctx.wait(3.2)

        print("3. no jet push")
        ctx.cmd(f"effect give {p} minecraft:slow_falling 3 0 true")
        before = len(log(ctx))
        hook(ctx, "toot", "1600")
        ctx.cmd(f"tp {p} 0.5 {Y + 9} 0.5 0 0")
        heights = []
        t0 = time.time()
        for _ in range(16):
            q = pos(ctx, p)
            heights.append(f"{time.time() - t0:.1f}s {q[1] - Y:.2f}" if q else "?")
            time.sleep(0.15)
        flags = re.findall(r"test: toot by \w+ .* flags=(\d+)", log(ctx)[before:])
        lines.append(f"toot in mid-air: flags {flags[-1] if flags else '?'} (1 would be a jet jump); heights: {', '.join(heights)}")
        ctx.cmd("effect clear @a")
    finally:
        cfg.write_text(original, encoding="utf-8")
        ctx.cmd("fart reload")
        ctx.cmd("difficulty peaceful")
    (ctx.out_dir / "cosmetic-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    for line in lines:
        print("  " + line)
