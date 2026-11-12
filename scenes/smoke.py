"""Start-up smoke check of one target: the branded start-up line, the generated client file, the client command,
a local puff seen from behind, and the settings screen.

    python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge smoke

Writes smoke-*.png and smoke-report.txt to build/scenes/<target>/.
"""
import re

Y = 100
CFG = "farmstead_fart_together-client.toml"


def hook(ctx, name, text=""):
    d = ctx.client_dir / "testhooks"
    d.mkdir(parents=True, exist_ok=True)
    (d / name).write_text(text, encoding="utf-8")


def log(ctx, side):
    path = ctx.out_dir / f"{side}.log"
    return path.read_text(encoding="utf-8", errors="replace") if path.exists() else ""


def run(ctx):
    p = ctx.player
    ctx.cmds("gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false", "weather clear",
             f"gamemode creative {p}", f"tp {p} 0.5 {Y + 1} 0.5 0 10")
    ctx.wait(3)
    ctx.cmds(f"fill -8 {Y - 1} -8 8 {Y - 1} 8 grass_block", f"fill -8 {Y} -8 8 {Y + 6} 8 air",
             "kill @e[type=!player]", f"tp {p} 0.5 {Y} 0.5 0 10")
    ctx.wait(2)

    hook(ctx, "command.run", "farttogether reload")
    ctx.wait(1.5)
    hook(ctx, "camera", "back")
    ctx.wait(1)
    ctx.shot("smoke-world")
    hook(ctx, "try.it")
    ctx.wait(0.35)
    ctx.shot("smoke-puff")
    # the real toot: key path -> server (hello, cooldown) -> broadcast back -> pose, cloud, sound
    ctx.wait(1.5)
    ctx.cmd(f"tp {p} 0.5 {Y} 0.5 90 10")
    hook(ctx, "camera", "front")
    ctx.wait(1)
    hook(ctx, "toot")
    ctx.wait(0.4)
    ctx.shot("smoke-toot")
    hook(ctx, "camera", "first")
    hook(ctx, "config.open")
    ctx.wait(1.5)
    ctx.shot("smoke-settings")

    client, server = log(ctx, "client"), log(ctx, "server")
    started = re.compile(r"\[Farmstead\] Fart Together \S+ started \((\w+), Minecraft ([^)]+)\)")
    cfg = ctx.client_dir / "config" / CFG
    lines = [
        f"client start-up line: {started.search(client).group(0) if started.search(client) else 'MISSING'}",
        f"server start-up line: {started.search(server).group(0) if started.search(server) else 'MISSING'}",
        f"client config file {CFG}: {'present' if cfg.exists() else 'MISSING'}",
        f"client command /farttogether reload: {'ok' if 'Fart Together settings reloaded' in client else 'no chat line'}",
        f"handshake (server answered hello): {'yes' if 'server runs Fart Together protocol' in client else 'NO'}",
        f"toot played from the server's broadcast: {'yes' if re.search(r'test: toot by \w+ .* own=true', client) else 'NO'}",
    ]
    errors = [l.strip() for l in (client + server).splitlines()
              if ("/ERROR]" in l or "Exception" in l) and ("fart" in l.lower() or "mixin" in l.lower())]
    lines.append(f"mod errors in logs: {len(errors)}")
    lines += ["  " + e[:300] for e in errors[:10]]
    (ctx.out_dir / "smoke-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    for line in lines:
        print("  " + line)
