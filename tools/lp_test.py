"""LuckPerms live: the mod's permission nodes and meta with the real LuckPerms on a dev server and two clients.

    python -I tools/lp_test.py 1.21.1-neoforge build/thirdparty/luckperms/LuckPerms-NeoForge-5.4.140.jar
    python -I tools/lp_test.py 1.21.1-fabric   build/thirdparty/luckperms/LuckPerms-Fabric-5.4.140.jar

The LuckPerms jar (official, from Modrinth) is copied into run/<target>/server/mods for the run and removed after.
Everything is set from the server console through RCON (`lp user|group ...`), then checked in the clients' logs
(styles and gas the server sent, toots played) and in screenshots:

  1. farmstead_fart_together.use false: Tester's toot reaches nobody, the client is told it is switched off;
  2. style.custom false: Tester's style becomes the plain one for others;
  3. style.rainbow: a rainbow request is made solid without it and kept with it;
  4. style.huge: size 4 is cut to style.maxSize (2) without it and to 3 (x1.5) with it;
  5. command.reload: Tester2 (not an operator) cannot run /fart reload, then can;
  6. meta for the group default: charge.max 200, cooldown 10 s, cloud.radius 5.

Writes lp-*.png and lp-report.txt to build/scenes/<target>/lp/.
"""
from __future__ import annotations

import re
import shutil
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "farmstead-mod-tools" / "test-matrix"))
sys.path.insert(0, str(ROOT / "tools"))
from rcon import Rcon  # noqa: E402
from scene import CLIENT_OPTIONS, RCON_PASSWORD, Ctx, Proc, kill_tree, write_props  # noqa: E402
from duo import A, B, CFG, PORT, Y, client_file, hook  # noqa: E402

NS = "farmstead_fart_together"
STYLE = {"color": '"pumpkin"', "particle": '"puff"', "size": "1.2", "amount": "1.4", "sound": '"trumpet"', "pose": '"classic"'}


def main() -> int:
    target, jar = sys.argv[1], Path(sys.argv[2]).resolve()
    run = ROOT / "run" / target
    server_dir, dir_a, dir_b = run / "server", run / "client", run / "client2"
    out = ROOT / "build" / "scenes" / target / "lp"
    out.mkdir(parents=True, exist_ok=True)
    rcon_port = PORT + 10
    mods = server_dir / "mods"
    mods.mkdir(parents=True, exist_ok=True)
    lp_jar = mods / jar.name
    shutil.copyfile(jar, lp_jar)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    write_props(server_dir / "server.properties", {
        "server-port": str(PORT), "online-mode": "false", "white-list": "false", "enforce-whitelist": "false",
        "enable-rcon": "true", "rcon.port": str(rcon_port), "rcon.password": RCON_PASSWORD, "difficulty": "peaceful",
        "spawn-protection": "0", "max-tick-time": "-1", "enforce-secure-profile": "false", "max-players": "4",
    })
    options = dict(CLIENT_OPTIONS, showSubtitles="true", soundCategory_master="0.02", renderDistance="6")
    for d in (dir_a, dir_b):
        write_props(d / "options.txt", options)
        (d / "config").mkdir(parents=True, exist_ok=True)
        (d / "config" / CFG).write_text(client_file(STYLE), encoding="utf-8")
    procs = []
    lines = []
    try:
        server = Proc(ROOT, f":{target}:runServer", out / "server.log", [])
        procs.append(server)
        if not server.wait_for(r"Done \(\d", 900):
            print("  ! server did not start")
            return 1
        rcon = Rcon(port=rcon_port, password=RCON_PASSWORD)
        lines.append("LuckPerms loaded: " + ("yes" if re.search(r"LuckPerms|luckperms", "".join(server.lines)) else "NO"))

        def client(name: str, extra: list[str]) -> Proc:
            p = Proc(ROOT, f":{target}:runClient", out / f"client-{name}.log",
                     [f"-PquickPlay=127.0.0.1:{PORT}", f"-PmcUser={name}", "-PtestHooks", "-PclientSize=1280x720", *extra])
            procs.append(p)
            if not server.wait_for(rf"{name} joined the game", 900):
                raise RuntimeError(f"{name} did not join")
            return p

        ca = client(A, [])
        cb = client(B, ["-PclientDir=client2"])
        time.sleep(8)
        for c in ("gamerule doMobSpawning false", "time set noon", "gamerule doDaylightCycle false", "weather clear",
                  f"fill -6 {Y - 1} -6 6 {Y - 1} 6 grass_block", f"fill -6 {Y} -6 6 {Y + 5} 6 air", "kill @e[type=!player]",
                  f"gamemode creative {A}", f"gamemode creative {B}", f"deop {A}", f"deop {B}",
                  f"lp user {A} permission clear", f"lp user {B} permission clear", "lp group default meta clear"):
            rcon.cmd(c)
        rcon.cmd(f"tp {A} 0.5 {Y} 0.5 0 0")
        rcon.cmd(f"tp {B} 4.5 {Y} -0.5 90 10")
        hook(dir_a, "camera", "back")
        hook(dir_b, "camera", "first")
        time.sleep(3)
        sa, sb = Ctx(rcon, dir_a, out, None), Ctx(rcon, dir_b, out, None)

        def lp(cmd: str) -> str:
            text = (rcon.cmd("lp " + cmd) or "").strip()
            time.sleep(1.2)
            return re.sub(r"§.", "", text)[:160]

        def mark() -> tuple[int, int]:
            return len("".join(ca.lines)), len("".join(cb.lines))

        def since(proc: Proc, at: int) -> str:
            return "".join(proc.lines)[at:]

        def toot(hold: int = 400) -> None:
            rcon.cmd(f"fart charge {A} full")
            time.sleep(0.6)
            hook(dir_a, "toot", str(hold))
            time.sleep(hold / 1000 + 1.2)

        def style_of_a_seen_by_b(at: int) -> str:
            found = re.findall(r"test: style of \S+ = (Style\[[^\]]*\])", since(cb, at))
            return found[-1] if found else "(no style update)"

        def resend_style(style: dict) -> None:
            (dir_a / "config" / CFG).write_text(client_file(style), encoding="utf-8")
            hook(dir_a, "config.reload")
            time.sleep(3.5)  # the client resends at most every 2 s

        # 1. use
        print("1. use")
        lines.append("1. " + lp(f"user {A} permission set {NS}.use false"))
        ma, mb = mark()
        toot()
        sa.shot("lp-1-use-false")
        heard = len(re.findall(rf"test: toot by {A} ", since(cb, mb)))
        told = re.findall(r"test: charge (\d+/\d+) min (\d+) enabled (\w+)", since(ca, ma))
        lines.append(f"   use=false: toots of {A} heard by {B}: {heard}; gas message to {A}: {told[-1] if told else 'none'}")
        lines.append("   " + lp(f"user {A} permission unset {NS}.use"))
        time.sleep(3.2)
        ma, mb = mark()
        toot()
        lines.append(f"   use unset: toots of {A} heard by {B}: {len(re.findall(rf'test: toot by {A} ', since(cb, mb)))}")
        time.sleep(3)

        # 2. style.custom
        print("2. style.custom")
        lines.append("2. " + lp(f"user {A} permission set {NS}.style.custom false"))
        ma, mb = mark()
        resend_style(STYLE)
        lines.append(f"   custom=false: {B} has {style_of_a_seen_by_b(mb)}")
        lines.append("   " + lp(f"user {A} permission unset {NS}.style.custom"))
        ma, mb = mark()
        resend_style(STYLE)
        lines.append(f"   custom unset: {B} has {style_of_a_seen_by_b(mb)}")

        # 3. rainbow
        print("3. style.rainbow")
        ma, mb = mark()
        resend_style(dict(STYLE, color='"rainbow"'))
        lines.append(f"3. rainbow without the node: {B} has {style_of_a_seen_by_b(mb)}")
        lines.append("   " + lp(f"user {A} permission set {NS}.style.rainbow true"))
        ma, mb = mark()
        resend_style(dict(STYLE, color='"rainbow"'))
        lines.append(f"   rainbow with the node: {B} has {style_of_a_seen_by_b(mb)}")
        toot(1600)
        sb.shot("lp-3-rainbow-granted-seen-by-b")
        time.sleep(3)

        # 4. huge
        print("4. style.huge")
        ma, mb = mark()
        resend_style(dict(STYLE, size="4.0", duration="4.0"))
        lines.append(f"4. size 4 without style.huge: {B} has {style_of_a_seen_by_b(mb)}")
        lines.append("   " + lp(f"user {A} permission set {NS}.style.huge true"))
        ma, mb = mark()
        resend_style(dict(STYLE, size="4.0", duration="4.0"))
        lines.append(f"   size 4 with style.huge: {B} has {style_of_a_seen_by_b(mb)}")
        toot(1600)
        sb.shot("lp-4-huge-seen-by-b")
        time.sleep(3)
        resend_style(STYLE)

        # 5. command.reload
        print("5. command.reload")
        ma, mb = mark()
        hook(dir_b, "command.run", "fart reload")
        time.sleep(2.5)
        denied = since(cb, mb)
        lines.append(f"5. {B} /fart reload without the node: "
                     + ("reloaded (WRONG)" if "reloaded" in denied else "refused (" + (re.findall(r"\[CHAT\] (.*)", denied) or ["no chat"])[-1][:90] + ")"))
        lines.append("   " + lp(f"user {B} permission set {NS}.command.reload true"))
        ma, mb = mark()
        hook(dir_b, "command.run", "fart reload")
        time.sleep(2.5)
        chat = re.findall(r"\[CHAT\] (.*)", since(cb, mb))
        lines.append(f"   with the node: {chat[-1][:110] if chat else 'no chat line'}")
        sb.shot("lp-5-reload-allowed")

        # 6. meta
        print("6. meta")
        lines.append("6. " + lp(f"group default meta set {NS}.charge.max 200"))
        lines.append("   " + lp(f"group default meta set {NS}.cooldown 10"))
        lines.append("   " + lp(f"group default meta set {NS}.cloud.radius 5"))
        ma, mb = mark()
        rcon.cmd("fart reload")  # resends the cooldown to everybody
        time.sleep(1.5)
        rcon.cmd(f"fart charge {A} full")
        time.sleep(1)
        hello = re.findall(r"cooldown (\d+) ms", since(ca, ma))
        gas = re.findall(r"test: charge (\d+/\d+)", since(ca, ma))
        lines.append(f"   {A} told: cooldown {hello[-1] if hello else '?'} ms, gas {gas[-1] if gas else '?'}")
        info = (rcon.cmd(f"execute as {A} run fart info") or "").strip()
        lines.append(f"   /fart info as {A}: {re.sub(r'§.', '', info)[:200]}")
        ma, mb = mark()
        hook(dir_a, "toot", "1700")
        time.sleep(3)
        sb.shot("lp-6-meta-radius-5")
        radius = re.findall(rf"test: toot by {A} .*?radius=([\d.]+)", since(cb, mb))
        lines.append(f"   full toot cloud radius seen by {B}: {radius[-1] if radius else '?'} (5 expected)")
        rcon.cmd(f"fart charge {A} full")
        time.sleep(0.5)
        ma, mb = mark()
        hook(dir_a, "toot", "300")
        time.sleep(1.5)
        lines.append(f"   second toot 3.5 s later (cooldown 10 s): heard by {B}: {len(re.findall(rf'test: toot by {A} ', since(cb, mb)))}")
        sa.shot("lp-6-meta-gas-200")
        for c in (f"user {A} permission clear", f"user {B} permission clear", "group default meta clear"):
            lp(c)
        errors = [l.strip() for l in "".join(ca.lines + cb.lines + server.lines).splitlines()
                  if ("/ERROR]" in l or "Exception" in l) and ("fart" in l.lower() or "mixin" in l.lower())]
        lines.append(f"mod errors in logs: {len(errors)}")
        lines += ["  " + e[:300] for e in errors[:10]]
        (out / "lp-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
        print("\n".join(lines))
        return 0
    finally:
        for p in reversed(procs):
            kill_tree(p.proc)
        time.sleep(3)
        try:
            lp_jar.unlink()
        except OSError as e:
            print(f"  ! could not remove {lp_jar}: {e}")


if __name__ == "__main__":
    sys.exit(main())
