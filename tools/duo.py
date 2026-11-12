"""Two players on one dedicated server: does the second player see and hear the first one's styled toot?

    python -I tools/duo.py 1.21.1-neoforge [--keep]

Starts the target's server and two dev clients (Tester in run/<target>/client, Tester2 in run/<target>/client2),
each with its own style in its client file, and drives them through the mod's TestHooks:

  1. Tester toots; Tester2 (side view) and Tester (third person) take screenshots: pose, Tester's cloud colour,
     the subtitle of the sound on Tester2's screen;
  2. Tester2 toots with its own style; Tester takes a screenshot;
  3. Tester asks for a rainbow (needs farmstead_fart_together.style.rainbow, which nobody has): the server must
     clamp it to a solid colour for everyone;
  4. Tester2 switches style.others to "simple": Tester's next toot is the plain green puff on Tester2's screen.

Screenshots and duo-report.txt land in build/scenes/<target>/duo/. Subtitles are on and the game volume is very low
so the subtitle line proves the sound was played on the listener's side without filling the room with noise.
"""
from __future__ import annotations

import re
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "farmstead-mod-tools" / "test-matrix"))
from rcon import Rcon  # noqa: E402
from scene import CLIENT_OPTIONS, RCON_PASSWORD, Ctx, Proc, kill_tree, write_props  # noqa: E402

PORT = 25640
Y = 100
A, B = "Tester", "Tester2"
CFG = "farmstead_fart_together-client.toml"

STYLE_A = {"color": '"pumpkin"', "particle": '"puff"', "size": "1.4", "amount": "1.6", "sound": '"trumpet"', "pose": '"classic"'}
STYLE_B = {"color": '"lilac"', "particle": '"snow"', "size": "1.0", "amount": "1.4", "sound": '"squeaky"', "pose": '"hero"'}


def client_file(style: dict, others: str = "theirs") -> str:
    lines = ["configVersion = 1", 'preset = "default"', "", "[style]", f'others = "{others}"']
    lines += [f"{k} = {v}" for k, v in style.items()]
    return "\n".join(lines) + "\n"


def hook(client_dir: Path, name: str, text: str = "") -> None:
    d = client_dir / "testhooks"
    d.mkdir(parents=True, exist_ok=True)
    (d / name).write_text(text, encoding="utf-8")


def main() -> int:
    target = sys.argv[1] if len(sys.argv) > 1 else "1.21.1-neoforge"
    keep = "--keep" in sys.argv
    run = ROOT / "run" / target
    server_dir, dir_a, dir_b = run / "server", run / "client", run / "client2"
    out = ROOT / "build" / "scenes" / target / "duo"
    out.mkdir(parents=True, exist_ok=True)
    rcon_port = PORT + 10

    server_dir.mkdir(parents=True, exist_ok=True)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    write_props(server_dir / "server.properties", {
        "server-port": str(PORT), "online-mode": "false", "white-list": "false", "enforce-whitelist": "false",
        "enable-rcon": "true", "rcon.port": str(rcon_port), "rcon.password": RCON_PASSWORD, "difficulty": "peaceful",
        "spawn-protection": "0", "max-tick-time": "-1", "enforce-secure-profile": "false", "max-players": "4",
    })
    options = dict(CLIENT_OPTIONS, showSubtitles="true", soundCategory_master="0.02", renderDistance="6")
    for d, style in ((dir_a, STYLE_A), (dir_b, STYLE_B)):
        write_props(d / "options.txt", options)
        (d / "config").mkdir(parents=True, exist_ok=True)
        (d / "config" / CFG).write_text(client_file(style), encoding="utf-8")

    forge = target.endswith("-forge")
    procs = []
    try:
        print(f"[{target}] server :{PORT}")
        server = Proc(ROOT, f":{target}:runServer", out / "server.log", [])
        procs.append(server)
        if not server.wait_for(r"Done \(\d", 900):
            print("  ! server did not start")
            return 1
        rcon = Rcon(port=rcon_port, password=RCON_PASSWORD)

        def client(name: str, d: Path, extra: list[str]) -> Proc:
            join = f"-PtestJoin=127.0.0.1:{PORT}" if forge else f"-PquickPlay=127.0.0.1:{PORT}"
            p = Proc(ROOT, f":{target}:runClient", out / f"client-{name}.log",
                     [join, f"-PmcUser={name}", "-PtestHooks", "-PclientSize=1280x720", *extra])
            procs.append(p)
            if not server.wait_for(rf"{name} joined the game", 900):
                raise RuntimeError(f"{name} did not join")
            return p

        print(f"[{target}] client {A}")
        ca = client(A, dir_a, [])
        print(f"[{target}] client {B}")
        cb = client(B, dir_b, ["-PclientDir=client2"])
        time.sleep(8)

        for c in ("gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false", "weather clear",
                  f"fill -6 {Y - 1} -6 6 {Y - 1} 6 grass_block", f"fill -6 {Y} -6 6 {Y + 5} 6 air", "kill @e[type=!player]",
                  f"gamemode creative {A}", f"gamemode creative {B}"):
            rcon.cmd(c)
        # A faces +Z (back to -Z, where the cloud goes); B stands to the side looking at A
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
        rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
        hook(dir_a, "camera", "back")
        hook(dir_b, "camera", "first")
        time.sleep(4)

        sa = Ctx(rcon, dir_a, out, None)
        sb = Ctx(rcon, dir_b, out, None)

        def toot_and_shoot(who: Path, tag: str, shooters) -> None:
            hook(who, "toot")
            time.sleep(0.45)
            for ctx, name in shooters:
                ctx.shot(f"{tag}-{name}")

        print("1. Tester toots")
        toot_and_shoot(dir_a, "1-a-toots", [(sb, "seen-by-b"), (sa, "seen-by-a")])
        time.sleep(3.5)  # server cooldown is 3 s

        print("2. Tester2 toots")
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 -90 10")  # A turns to look at B's side
        rcon.cmd(f"tp {B} 4.5 {Y} 0.5 0 0")
        hook(dir_a, "camera", "first")
        time.sleep(1.5)
        toot_and_shoot(dir_b, "2-b-toots", [(sa, "seen-by-a")])
        time.sleep(3.5)

        print("3. Tester asks for rainbow (no permission: the server makes it solid)")
        (dir_a / "config" / CFG).write_text(client_file(dict(STYLE_A, color='"rainbow"')), encoding="utf-8")
        hook(dir_a, "config.reload")
        time.sleep(3.5)  # the client resends at most every 2 s
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
        rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
        time.sleep(1.5)
        toot_and_shoot(dir_a, "3-a-rainbow-clamped", [(sb, "seen-by-b")])
        time.sleep(3.5)

        print("4. Tester2 sets style.others = simple")
        (dir_b / "config" / CFG).write_text(client_file(STYLE_B, others="simple"), encoding="utf-8")
        hook(dir_b, "config.reload")
        time.sleep(2)
        toot_and_shoot(dir_a, "4-a-toots-b-simple", [(sb, "seen-by-b")])
        time.sleep(2)

        report(out, ca, cb, server)
        return 0
    finally:
        if not keep:
            for p in reversed(procs):
                kill_tree(p.proc)


def report(out: Path, ca: Proc, cb: Proc, server: Proc) -> None:
    la, lb, ls = "".join(ca.lines), "".join(cb.lines), "".join(server.lines)
    heard_b = re.findall(r"test: toot by (\w+) sound=(\w+) shape=(\w+) color=(\w+) own=(\w+)", lb)
    heard_a = re.findall(r"test: toot by (\w+) sound=(\w+) shape=(\w+) color=(\w+) own=(\w+)", la)
    styles_b = re.findall(r"test: style of \S+ = (Style\[[^\]]*\])", lb)
    lines = [
        f"A hello answered: {'yes' if 'server runs Fart Together protocol' in la else 'NO'}",
        f"B hello answered: {'yes' if 'server runs Fart Together protocol' in lb else 'NO'}",
        "toots played on B's client (who, sound, shape, colour, own):",
        *[f"  {h}" for h in heard_b],
        "toots played on A's client:",
        *[f"  {h}" for h in heard_a],
        "styles B received:",
        *[f"  {s[:220]}" for s in styles_b],
    ]
    errors = [l.strip() for l in (la + lb + ls).splitlines()
              if ("/ERROR]" in l or "Exception" in l) and ("fart" in l.lower() or "mixin" in l.lower())]
    lines.append(f"mod errors in logs: {len(errors)}")
    lines += ["  " + e[:300] for e in errors[:10]]
    (out / "duo-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))


if __name__ == "__main__":
    sys.exit(main())
