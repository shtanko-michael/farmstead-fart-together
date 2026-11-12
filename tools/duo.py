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
M2 (gas, cloud, items, commands, My style):
  5. with no gas Tester's toot is refused (action bar hint), nothing reaches Tester2;
  6. Tester eats a bowl of beans (use key held): the gas meter appears;
  7. cows, a pig and a zombie stand behind Tester; a full-meter toot leaves a cloud and they run (positions
     before and after, from the server);
  8. a row of bean and cabbage crops at every stage;
  9. /fart toggle Tester2: Tester2's toot is refused; toggled back;
 10. Tester's settings open on the My style tab: preview, then "Play".
M3 (hold to charge, jet jump, golden gas, bubbles, Fart Together, advancements, trades and loot):
 11. Tester holds the key on a full meter: the HUD build-up half way and at full strength, then what is left;
 12. a tap lets out only charge.minToToot (60 -> 50);
 13. jet jump: Tester toots in mid-air and rises (heights from the server), the blast seen by Tester2;
 14. an enchanted golden apple, then a golden toot;
 15. under water: bubbles;
 16. both toot together: the combo (both clients), the chorus and the shared cloud seen by Tester2;
 17. advancements announced in the server log;
 18. farmer villagers offer bean trades; dungeon and village chests roll beans.

Screenshots and duo-report.txt land in build/scenes/<target>/duo/. With --record the listing's clips are recorded
too (every tick's frame in run/<target>/client*/recordings/<clip>/; media/gifs_from_frames.py makes the GIFs). Subtitles are on and the game volume is very low
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


RECORD = "--record" in sys.argv


def rec(client_dir: Path, clip: str, seconds: float) -> None:
    """Starts recording a listing clip on that client (only with --record)."""
    if RECORD:
        hook(client_dir, "record", f"{clip} {seconds}")


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
    # tools/vanilla.py may have left the items switched off: this scene needs them
    server_cfg = server_dir / "config" / "farmstead_fart_together-server.toml"
    if server_cfg.exists():
        server_cfg.write_text(re.sub(r"(?m)^enabled = false$", "enabled = true", server_cfg.read_text(encoding="utf-8")),
                              encoding="utf-8")
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
            # a quick tap would be a small toot now (hold to charge): hold long enough for full strength
            hook(who, "toot", "1700")
            time.sleep(1.7)
            time.sleep(0.45)
            for ctx, name in shooters:
                ctx.shot(f"{tag}-{name}")

        def full(name: str) -> None:
            rcon.cmd(f"fart charge {name} full")
            time.sleep(0.3)

        full(A)
        full(B)
        print("1. Tester toots")
        rec(dir_b, "toot", 3.5)
        toot_and_shoot(dir_a, "1-a-toots", [(sb, "seen-by-b"), (sa, "seen-by-a")])
        time.sleep(3.5)  # server cooldown is 3 s

        full(A)
        print("2. Tester2 toots")
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 -90 10")  # A turns to look at B's side
        rcon.cmd(f"tp {B} 4.5 {Y} 0.5 0 0")
        hook(dir_a, "camera", "first")
        time.sleep(1.5)
        toot_and_shoot(dir_b, "2-b-toots", [(sa, "seen-by-a")])
        time.sleep(3.5)

        full(B)
        print("3. Tester asks for rainbow (no permission: the server makes it solid)")
        (dir_a / "config" / CFG).write_text(client_file(dict(STYLE_A, color='"rainbow"')), encoding="utf-8")
        hook(dir_a, "config.reload")
        time.sleep(3.5)  # the client resends at most every 2 s
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
        rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
        time.sleep(1.5)
        full(A)
        toot_and_shoot(dir_a, "3-a-rainbow-clamped", [(sb, "seen-by-b")])
        time.sleep(3.5)
        full(A)

        print("4. Tester2 sets style.others = simple")
        (dir_b / "config" / CFG).write_text(client_file(STYLE_B, others="simple"), encoding="utf-8")
        hook(dir_b, "config.reload")
        time.sleep(2)
        toot_and_shoot(dir_a, "4-a-toots-b-simple", [(sb, "seen-by-b")])
        time.sleep(3.5)
        m2(rcon, dir_a, dir_b, sa, sb, out)
        hook(dir_a, "screen.close")
        time.sleep(1)
        m3(rcon, dir_a, dir_b, sa, sb, out, server)

        report(out, ca, cb, server)
        return 0
    finally:
        if not keep:
            for p in reversed(procs):
                kill_tree(p.proc)


def pos(rcon, selector: str):
    """Server-side position of one entity, or None."""
    text = rcon.cmd(f"data get entity {selector} Pos") or ""
    m = re.search(r"\[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]", text)
    return tuple(float(v) for v in m.groups()) if m else None


def m2(rcon, dir_a: Path, dir_b: Path, sa, sb, out: Path) -> None:
    lines = []
    print("5. no gas: refused")
    rcon.cmd(f"fart charge {A} 0")
    rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
    rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
    hook(dir_a, "camera", "back")
    time.sleep(1.5)
    hook(dir_a, "toot")
    time.sleep(0.5)
    sa.shot("5-a-no-gas")
    time.sleep(1)

    print("6. Tester eats a bowl of beans")
    rcon.cmd(f"item replace entity {A} hotbar.0 with farmstead_fart_together:bowl_of_beans")
    hook(dir_a, "camera", "first")
    time.sleep(1)
    hook(dir_a, "use", "45")
    time.sleep(3.5)
    sa.shot("6-a-gas-meter")
    lines.append("after the bowl of beans: see 'test: charge' lines in client-Tester.log")

    print("7. scare: mobs behind Tester run from the cloud")
    rcon.cmd("difficulty easy")
    rcon.cmd(f"fill -6 {Y} -6 6 {Y + 5} 6 air")
    for i, (kind, x, z) in enumerate((("cow", -1.0, -2.5), ("cow", 1.5, -3.0), ("pig", 0.5, -1.8), ("zombie", -0.5, -3.5),
                                       ("sheep", 2.0, -2.0))):
        rcon.cmd(f"summon minecraft:{kind} {x} {Y} {z} {{NoAI:0b,PersistenceRequired:1b,Tags:[\"ft\"]}}")
    time.sleep(1.5)
    before = {k: pos(rcon, f"@e[type=minecraft:{k},tag=ft,limit=1,sort=nearest]") for k in ("cow", "pig", "zombie", "sheep")}
    rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
    rcon.cmd(f"tp {B} 7.5 {Y + 1} -1.5 90 15")
    hook(dir_b, "camera", "first")
    rcon.cmd(f"fart charge {A} full")
    time.sleep(1)
    rec(dir_b, "scare", 6)
    hook(dir_a, "toot", "1700")
    time.sleep(2.3)
    sb.shot("7-b-cloud")
    time.sleep(1.6)
    sb.shot("7-b-mobs-run")
    time.sleep(1.5)
    after = {k: pos(rcon, f"@e[type=minecraft:{k},tag=ft,limit=1,sort=nearest]") for k in before}
    cloud = (0.5, Y + 0.75, -0.1)
    for k in before:
        b, a = before[k], after[k]
        if b and a:
            db = ((b[0] - cloud[0]) ** 2 + (b[2] - cloud[2]) ** 2) ** 0.5
            da = ((a[0] - cloud[0]) ** 2 + (a[2] - cloud[2]) ** 2) ** 0.5
            lines.append(f"{k}: {db:.1f} -> {da:.1f} blocks from the cloud")
        else:
            lines.append(f"{k}: position unknown ({b} -> {a})")
    rcon.cmd("kill @e[tag=ft]")
    rcon.cmd("difficulty peaceful")

    print("8. crops at every stage")
    rcon.cmd(f"fill -6 {Y - 1} 3 6 {Y - 1} 4 farmland")
    for i, age in enumerate((0, 2, 4, 5, 6, 7)):
        rcon.cmd(f"setblock {i - 3} {Y} 3 farmstead_fart_together:bean_crop[age={age}]")
        rcon.cmd(f"setblock {i - 3} {Y} 4 farmstead_fart_together:cabbage_crop[age={age}]")
    rcon.cmd(f"tp {B} 0.5 {Y + 1.5} 0.5 0 30")
    rcon.cmd(f"tp {A} -8.5 {Y} -8.5 0 0")
    time.sleep(2.5)
    sb.shot("8-b-crops")

    print("9. /fart toggle Tester2")
    lines.append("toggle: " + (rcon.cmd(f"fart toggle {B}") or "").strip())
    rcon.cmd(f"fart charge {B} full")
    time.sleep(0.5)
    hook(dir_b, "toot")
    time.sleep(0.6)
    sb.shot("9-b-toggled-off")
    lines.append("toggle back: " + (rcon.cmd(f"fart toggle {B}") or "").strip())
    lines.append("info (console): " + (rcon.cmd("fart info") or "").strip())

    print("10. My style tab")
    rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
    hook(dir_a, "camera", "first")
    hook(dir_a, "config.open")
    time.sleep(1.5)
    hook(dir_a, "config.tab", "my_style")
    time.sleep(1.5)
    sa.shot("10-a-my-style")
    rec(dir_a, "mystyle", 3)
    hook(dir_a, "mystyle.play")
    time.sleep(0.5)
    sa.shot("10-a-my-style-play")
    (out / "m2-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))


def m3(rcon, dir_a: Path, dir_b: Path, sa, sb, out: Path, server) -> None:
    lines = []
    rcon.cmd(f"fill -12 {Y} -12 12 {Y + 12} 12 air")
    rcon.cmd(f"fill -12 {Y - 1} -12 12 {Y - 1} 12 grass_block")
    rcon.cmd("kill @e[type=!player]")

    print("11. hold to charge: the HUD build-up")
    rcon.cmd(f"tp {A} 0.5 {Y} 0.5 180 15")
    hook(dir_a, "camera", "back")
    rcon.cmd(f"fart charge {A} full")
    time.sleep(1)
    rec(dir_a, "hold", 4.5)
    hook(dir_a, "toot", "2600")
    time.sleep(0.75)
    sa.shot("11-a-hold-half")
    time.sleep(1.0)
    sa.shot("11-a-hold-full")
    time.sleep(1.6)
    sa.shot("11-a-after-release")
    time.sleep(3.2)

    print("12. a tap: only the minimum comes out")
    rcon.cmd(f"fart charge {A} 60")
    time.sleep(0.5)
    hook(dir_a, "toot")
    time.sleep(1)
    sa.shot("12-a-after-tap")
    lines.append("tap: see the last 'test: charge' lines of client-Tester.log (60 -> 50 expected)")
    time.sleep(3)

    print("13. jet jump")
    rcon.cmd(f"gamemode survival {A}")
    rcon.cmd(f"tp {B} 7.5 {Y + 3} 0.5 90 10")
    hook(dir_b, "camera", "first")
    hook(dir_a, "camera", "back")
    rcon.cmd(f"fart charge {A} full")
    time.sleep(1)
    # a full hold takes 1.6 s: slow falling keeps Tester in the air that long (it wears off before the toot)
    rcon.cmd(f"effect give {A} minecraft:slow_falling 3")
    rcon.cmd(f"tp {A} 0.5 {Y + 9} 0.5 0 0")
    rcon.cmd(f"tp {B} 8.5 {Y} 0.5 90 -22")
    rec(dir_b, "jet", 4)
    hook(dir_a, "toot", "1600")
    heights = []
    t0 = time.time()
    for i in range(16):
        q = pos(rcon, A)
        heights.append(f"{time.time() - t0:.1f}s y={q[1] - Y:.2f}" if q else "?")
        if i == 9:
            sb.shot("13-b-jet")
        time.sleep(0.15)
    lines.append("jet jump (A's height above the ground, toot after 1.6 s): " + ", ".join(heights))
    time.sleep(2)
    rcon.cmd(f"gamemode creative {A}")

    print("14. golden gas")
    rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
    rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
    rcon.cmd(f"gamemode survival {A}")
    rcon.cmd(f"effect give {A} minecraft:hunger 3 255")
    time.sleep(3.2)
    rcon.cmd(f"item replace entity {A} hotbar.0 with minecraft:enchanted_golden_apple")
    hook(dir_a, "camera", "first")
    time.sleep(0.5)
    hook(dir_a, "use", "45")
    time.sleep(3.2)
    rcon.cmd(f"gamemode creative {A}")
    hook(dir_a, "camera", "back")
    rcon.cmd(f"fart charge {A} full")
    time.sleep(1)
    rec(dir_b, "golden", 4)
    hook(dir_a, "toot", "1700")
    time.sleep(2.1)
    sb.shot("14-b-golden")
    time.sleep(3)

    print("15. under water")
    rcon.cmd(f"fill 7 {Y - 1} -5 17 {Y + 5} 5 glass")
    rcon.cmd(f"fill 8 {Y} -4 16 {Y + 4} 4 water")
    rcon.cmd(f"tp {A} 12.5 {Y} 0.5 0 0")
    rcon.cmd(f"fart charge {A} full")
    time.sleep(1.5)
    rec(dir_a, "bubbles", 4)
    hook(dir_a, "toot", "1700")
    time.sleep(2.0)
    sa.shot("15-a-bubbles")
    time.sleep(3)
    rcon.cmd(f"fill 7 {Y} -5 17 {Y + 5} 5 air")
    rcon.cmd(f"fill 7 {Y - 1} -5 17 {Y - 1} 5 grass_block")

    print("16. Fart Together: both at once")
    rcon.cmd(f"tp {A} -1.0 {Y} 0.5 0 0")
    rcon.cmd(f"tp {B} 2.0 {Y} 0.5 0 0")
    rcon.cmd(f"fart charge {A} full")
    rcon.cmd(f"fart charge {B} full")
    hook(dir_a, "camera", "back")
    hook(dir_b, "camera", "back")
    time.sleep(1.5)
    hook(dir_a, "toot", "900")
    rec(dir_b, "together", 5)
    hook(dir_b, "toot", "1000")
    time.sleep(1.4)
    sb.shot("16-b-together")
    sa.shot("16-a-together")
    time.sleep(1.5)
    sb.shot("16-b-together-cloud")
    time.sleep(2)

    print("17-18. advancements, trades, loot")
    log = "".join(server.lines)
    made = sorted(set(re.findall(r"has (?:made the advancement|completed the challenge|reached the goal) \[([^\]]+)\]", log)))
    lines.append("advancements announced: " + ", ".join(made))
    found = 0
    for i in range(10):
        level = 1 if i < 6 else 2
        rcon.cmd(f"summon minecraft:villager {-8 + i} {Y} 8 {{NoAI:1b,Tags:[\"ft{i}\"],VillagerData:{{profession:\"minecraft:farmer\",level:{level},type:\"minecraft:plains\"}}}}")
        text = rcon.cmd(f"data get entity @e[tag=ft{i},limit=1] Offers") or ""
        mine = re.findall(r"farmstead_fart_together:(\w+)", text)
        if mine:
            found += 1
        lines.append(f"farmer level {level} #{i}: " + (", ".join(sorted(set(mine))) if mine else "vanilla trades only"))
    lines.append(f"farmers offering our trades: {found} of 10")
    rcon.cmd(f"clear {B}")
    for table in ("minecraft:chests/simple_dungeon", "minecraft:chests/village/village_plains_house"):
        for _ in range(15):
            rcon.cmd(f"loot give {B} loot {table}")
        beans = (rcon.cmd(f"clear {B} farmstead_fart_together:beans 0") or "").strip()
        seeds = (rcon.cmd(f"clear {B} farmstead_fart_together:cabbage_seeds 0") or "").strip()
        lines.append(f"15 x {table}: beans: {beans} | cabbage seeds: {seeds}")
        rcon.cmd(f"clear {B}")
    (out / "m3-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))


def report(out: Path, ca: Proc, cb: Proc, server: Proc) -> None:
    la, lb, ls = "".join(ca.lines), "".join(cb.lines), "".join(server.lines)
    toot = r"test: toot by (\w+) sound=(\w+) shape=(\w+) color=(\w+) own=(\w+) power=(\d) radius=([\d.]+) linger=(\d+) flags=(\d+)"
    heard_b = re.findall(toot, lb)
    heard_a = re.findall(toot, la)
    charges_a = re.findall(r"test: charge (\d+/\d+) min (\d+) enabled (\w+)", la)
    styles_b = re.findall(r"test: style of \S+ = (Style\[[^\]]*\])", lb)
    lines = [
        f"A hello answered: {'yes' if 'server runs Fart Together protocol' in la else 'NO'}",
        f"B hello answered: {'yes' if 'server runs Fart Together protocol' in lb else 'NO'}",
        "toots played on B's client (who, sound, shape, colour, own):",
        *[f"  {h}" for h in heard_b],
        "toots played on A's client:",
        *[f"  {h}" for h in heard_a],
        "gas A was told (charge/max, min, enabled):",
        *[f"  {c}" for c in charges_a],
        "combos seen (A, B): " + str(la.count("test: combo count=")) + ", " + str(lb.count("test: combo count=")),
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
