"""Fart Together with more players: a server and N real clients (Tester, Tester2, ...) toot at once; then the same N
plus stand-in participants (`/fart testcombo`, test rig only) make a combo of 8.

    python -I tools/crowd.py 1.21.1-neoforge [--clients 4] [--xmx 1400m]

Each client runs with a capped heap and render distance 4; check free memory first (about 2 GB per client). The
stand-ins are not players: they count towards the combo's N (the shared cloud, the "xN" and the advancements of the
real players) but get no messages or glow themselves. The report says which part was which.

Writes crowd-*.png and crowd-report.txt to build/scenes/<target>/crowd/.
"""
from __future__ import annotations

import re
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "farmstead-mod-tools" / "test-matrix"))
sys.path.insert(0, str(ROOT / "tools"))
from rcon import Rcon  # noqa: E402
from scene import CLIENT_OPTIONS, RCON_PASSWORD, Ctx, Proc, kill_tree, write_props  # noqa: E402
from duo import CFG, PORT, Y, client_file, hook  # noqa: E402

COLOURS = ['"pumpkin"', '"lilac"', '"sky"', '"rose"', '"gold"', '"pine"', '"bean"', '"swamp"']


def arg(name: str, default: str) -> str:
    return sys.argv[sys.argv.index(name) + 1] if name in sys.argv else default


def main() -> int:
    target = sys.argv[1]
    n = int(arg("--clients", "4"))
    xmx = arg("--xmx", "1400m")
    run = ROOT / "run" / target
    server_dir = run / "server"
    names = ["Tester"] + [f"Tester{i}" for i in range(2, n + 1)]
    dirs = [run / ("client" if i == 0 else f"client{i + 1}") for i in range(n)]
    out = ROOT / "build" / "scenes" / target / "crowd"
    out.mkdir(parents=True, exist_ok=True)
    rcon_port = PORT + 10
    (server_dir / "eula.txt").parent.mkdir(parents=True, exist_ok=True)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    write_props(server_dir / "server.properties", {
        "server-port": str(PORT), "online-mode": "false", "white-list": "false", "enforce-whitelist": "false",
        "enable-rcon": "true", "rcon.port": str(rcon_port), "rcon.password": RCON_PASSWORD, "difficulty": "peaceful",
        "spawn-protection": "0", "max-tick-time": "-1", "enforce-secure-profile": "false", "max-players": "10",
        "view-distance": "4", "simulation-distance": "4",
    })
    options = dict(CLIENT_OPTIONS, renderDistance="4", simulationDistance="5", soundCategory_master="0.0")
    for i, d in enumerate(dirs):
        write_props(d / "options.txt", options)
        (d / "config").mkdir(parents=True, exist_ok=True)
        style = {"color": COLOURS[i % len(COLOURS)], "particle": '"puff"', "size": "1.2", "amount": "1.4", "pose": '"classic"'}
        (d / "config" / CFG).write_text(client_file(style), encoding="utf-8")
    procs, clients, lines = [], [], []
    try:
        server = Proc(ROOT, f":{target}:runServer", out / "server.log", ["-PtestHooks"])
        procs.append(server)
        if not server.wait_for(r"Done \(\d", 900):
            print("  ! server did not start")
            return 1
        rcon = Rcon(port=rcon_port, password=RCON_PASSWORD)
        for i, name in enumerate(names):
            extra = [] if i == 0 else [f"-PclientDir={dirs[i].name}"]
            size = "1280x720" if i < 2 else "640x360"
            p = Proc(ROOT, f":{target}:runClient", out / f"client-{name}.log",
                     [f"-PquickPlay=127.0.0.1:{PORT}", f"-PmcUser={name}", "-PtestHooks", f"-PclientSize={size}",
                      f"-PclientXmx={xmx}", *extra])
            procs.append(p)
            clients.append(p)
            if not server.wait_for(rf"{name} joined the game", 900):
                raise RuntimeError(f"{name} did not join")
            print(f"  {name} joined")
        time.sleep(8)
        for c in ("effect clear @a", "gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false",
                  "weather clear", f"fill -8 {Y - 1} -8 8 {Y - 1} 8 grass_block", f"fill -8 {Y} -8 8 {Y + 6} 8 air",
                  "kill @e[type=!player]", *[f"gamemode creative {m}" for m in names]):
            rcon.cmd(c)
        # a row facing +Z; the first player's front camera looks back at all of them
        for i, name in enumerate(names):
            rcon.cmd(f"tp {name} {(i - (n - 1) / 2) * 1.3 + 0.5:.2f} {Y} 0.5 0 0")
        hook(dirs[0], "camera", "front")
        for d in dirs[1:]:
            hook(d, "camera", "back")
        time.sleep(3)
        ctx = Ctx(rcon, dirs[0], out, None)

        def everyone_toots(hold: int) -> None:
            for m in names:
                rcon.cmd(f"fart charge {m} full")
            time.sleep(0.8)
            for d in dirs:
                hook(d, "toot", str(hold))

        def step_back(done: float = 0.0) -> None:
            """After the toots, Tester steps 9 blocks back (the shared cloud stays) and turns to film it."""
            time.sleep(max(0.0, 1.8 - done))
            hook(dirs[0], "camera", "first")
            rcon.cmd(f"tp {names[0]} 0.5 {Y + 1} 10.5 180 8")
            time.sleep(0.8)

        def step_in() -> None:
            rcon.cmd(f"tp {names[0]} {(0 - (n - 1) / 2) * 1.3 + 0.5:.2f} {Y} 0.5 0 0")
            hook(dirs[0], "camera", "front")

        print(f"1. {n} real players toot together")
        mark = [len("".join(c.lines)) for c in clients]
        everyone_toots(1500)
        step_back()
        ctx.shot(f"crowd-{n}-real")
        time.sleep(1.2)
        ctx.shot(f"crowd-{n}-real-cloud")
        combos = re.findall(r"test: combo count=(\d+)", "".join(clients[0].lines)[mark[0]:])
        lines.append(f"{n} real clients: combos on {names[0]}'s screen: {', '.join(combos) or 'none'} (the last is the full combo)")
        seen = [max(map(int, re.findall(r"test: combo count=(\d+)", "".join(c.lines)[m:]) or ["0"])) for c, m in zip(clients, mark)]
        lines.append(f"  largest combo each client saw: {seen}")
        time.sleep(6)

        step_in()
        extra = 8 - n
        print(f"2. {n} real players + {extra} stand-ins = 8")
        mark = [len("".join(c.lines)) for c in clients]
        everyone_toots(1500)
        # the stand-ins "toot" while the real players still hold the key: inside the 1.5 s combo window
        time.sleep(1.1)
        lines.append("  testcombo: " + (rcon.cmd(f"fart testcombo {names[0]} {extra}") or "").strip()[:140])
        step_back(1.1)
        ctx.shot("crowd-8-with-stand-ins")
        time.sleep(1.2)
        ctx.shot("crowd-8-with-stand-ins-cloud")
        combos = re.findall(r"test: combo count=(\d+) radius=([\d.]+)", "".join(clients[0].lines)[mark[0]:])
        lines.append(f"{n} real + {extra} stand-ins: combos on {names[0]}'s screen: "
                     + ", ".join(f"x{c} (radius {r})" for c, r in combos))
        time.sleep(2)
        log = "".join(server.lines)
        made = sorted(set(re.findall(r"(\w+) has (?:made the advancement|completed the challenge|reached the goal) \[([^\]]+)\]", log)))
        lines.append("advancements announced: " + "; ".join(f"{who}: {what}" for who, what in made))
        errors = [l.strip() for l in "".join(sum((c.lines for c in clients), []) + server.lines).splitlines()
                  if ("/ERROR]" in l or "Exception" in l) and ("fart" in l.lower() or "mixin" in l.lower())]
        lines.append(f"mod errors in logs: {len(errors)}")
        lines += ["  " + e[:300] for e in errors[:10]]
        (out / "crowd-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
        print("\n".join(lines))
        return 0
    finally:
        for p in reversed(procs):
            kill_tree(p.proc)


if __name__ == "__main__":
    sys.exit(main())
