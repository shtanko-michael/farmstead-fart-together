"""Farmer trades in game: farmers of level 1 and 2 are summoned, the player opens each one's trading screen (that is
when a villager rolls its offers; on 1.21.x reading the entity does it too) and the offers are read from the
server; the first farmer that offers ours is kept as a screenshot. Then items.beansInVillagerTrades is switched off
and new farmers are rolled again (on 26.x the trades are data, so the switch is expected to change nothing there).

    python -I ../farmstead-mod-tools/test-matrix/scene.py . 26.3-neoforge trades

Writes trades-*.png and trades-report.txt to build/scenes/<target>/.
"""
import re

Y = 100
SERVER_CFG = "farmstead_fart_together-server.toml"


def hook(ctx, name, text=""):
    d = ctx.client_dir / "testhooks"
    d.mkdir(parents=True, exist_ok=True)
    (d / name).write_text(text, encoding="utf-8")


def roll(ctx, tag, z, lines, shoot):
    """Ten farmers in a row at z, each opened by the player; returns how many offer ours."""
    p = ctx.player
    found = 0
    for i in range(10):
        level = 1 if i < 6 else 2
        x = -9 + 2 * i
        said = ctx.cmd(f"summon minecraft:villager {x + 0.5} {Y} {z + 0.5} {{NoAI:1b,Silent:1b,Rotation:[180f,0f],Tags:[\"{tag}{i}\"],"
                       f"VillagerData:{{profession:\"minecraft:farmer\",level:{level},type:\"minecraft:plains\"}}}}") or ""
        if i == 0:
            lines.append(f"  summon: {said.strip()[:160]}; villagers now: {(ctx.cmd('execute if entity @e[type=minecraft:villager]') or '').strip()[:60]}")
        ctx.cmd(f"tp {p} {x + 0.5} {Y} {z - 1.0} 0 10")
        ctx.wait(0.8)
        hook(ctx, "use", "2")
        ctx.wait(1.2)
        # by place, not by tag (26.x summons ignore the Tags field); the whole entity, Offers is in there
        text = ctx.cmd(f"data get entity @e[type=minecraft:villager,limit=1,sort=nearest,x={x + 0.5},y={Y},z={z + 0.5}]") or ""
        mine = sorted(set(re.findall(r"farmstead_fart_together:(\w+)", text)))
        if mine:
            found += 1
            if shoot and found == 1:
                ctx.shot("trades-screen")
        hook(ctx, "screen.close")
        ctx.wait(0.5)
        vanilla = len(re.findall(r"minecraft:emerald", text))
        lines.append(f"  farmer level {level} #{i}: {', '.join(mine) if mine else 'vanilla trades only'}"
                     + ("" if vanilla else f" (offers not read: {text.strip()[:80]})"))
    lines.append(f"  farmers offering our trades: {found} of 10")
    return found


def run(ctx):
    p = ctx.player
    lines = []
    ctx.cmds("gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false", "weather clear",
             "effect clear @a", f"gamemode creative {p}", f"tp {p} 0.5 {Y + 1} 0.5 0 10")
    ctx.wait(3)
    for c in (f"fill -12 {Y - 1} -12 12 {Y - 1} 12 grass_block", f"fill -12 {Y} -12 12 {Y + 6} 12 air",
              "kill @e[type=!player]"):
        lines.append(f"{c}: {(ctx.cmd(c) or '').strip()[:100]}")
    ctx.wait(1.5)
    lines.append("items.beansInVillagerTrades = true (default):")
    roll(ctx, "fa", 4, lines, True)

    cfg = ctx.client_dir.parent / "server" / "config" / SERVER_CFG
    if cfg.exists():
        text = cfg.read_text(encoding="utf-8")
        off = re.sub(r"(?m)^#?\s*beansInVillagerTrades = \w+$", "beansInVillagerTrades = false", text)
        cfg.write_text(off, encoding="utf-8")
        lines.append("fart reload: " + (ctx.cmd("fart reload") or "").strip()[:160])
        lines.append("items.beansInVillagerTrades = false:")
        roll(ctx, "fb", 8, lines, False)
        cfg.write_text(text, encoding="utf-8")
        ctx.cmd("fart reload")
    else:
        lines.append(f"server file not found: {cfg}")
    ctx.cmd(f"tp {p} 0.5 {Y} -1.5 0 10")
    ctx.wait(1.5)
    ctx.shot("trades-farmers")
    (ctx.out_dir / "trades-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    for line in lines:
        print(line)
