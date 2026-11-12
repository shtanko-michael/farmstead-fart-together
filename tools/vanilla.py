"""The two cases the dev rig cannot cover with two modded clients:

    python -I tools/vanilla.py modded-client 1.21.1-neoforge   # our client on Mojang's own 1.21.1 server
    python -I tools/vanilla.py vanilla-client 1.21.1-neoforge  # a vanilla login on our server (items on, then off)

modded-client: runs the official server jar (run/vanilla-<mc>/server/server.jar, downloaded once from
piston-data.mojang.com, checked against Mojang's sha1) and joins it with the dev client. The client must stay in
"only for yourself" mode: its toot plays locally, no hello is answered, no gas meter, no errors.

vanilla-client: there is no unmodded dev client, so a minimal vanilla login is spoken here (protocol 767 = 1.21.1:
handshake, login, configuration with known packs, until the play-state login packet or a disconnect). With the
mod's items registered the loader is expected to turn it away; with items.enabled = false it must get in.

Reports land in build/scenes/<target>/vanilla/.
"""
from __future__ import annotations

import io
import json
import re
import socket
import struct
import subprocess
import sys
import time
import uuid
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT.parent / "farmstead-mod-tools" / "test-matrix"))
from rcon import Rcon  # noqa: E402
from scene import CLIENT_OPTIONS, RCON_PASSWORD, Ctx, Proc, kill_tree, write_props  # noqa: E402

PROTOCOL = {"1.21.1": 767}
PORT = 25650
JAVA = "C:/Users/admin/.jdks/jdk-25.0.5+8/bin/java.exe"


# ---------------------------------------------------------------- a minimal vanilla login


def varint(n: int) -> bytes:
    out = b""
    n &= 0xFFFFFFFF
    while True:
        b = n & 0x7F
        n >>= 7
        out += bytes([b | (0x80 if n else 0)])
        if not n:
            return out


def read_varint(f) -> int:
    n = shift = 0
    while True:
        b = f.read(1)
        if not b:
            raise EOFError
        n |= (b[0] & 0x7F) << shift
        shift += 7
        if not b[0] & 0x80:
            return n - (1 << 32) if n & (1 << 31) else n


def string(s: str) -> bytes:
    b = s.encode("utf-8")
    return varint(len(b)) + b


class Conn:
    def __init__(self, port: int):
        self.s = socket.create_connection(("127.0.0.1", port), timeout=20)
        self.f = self.s.makefile("rb")
        self.threshold = -1

    def send(self, pid: int, body: bytes = b"") -> None:
        data = varint(pid) + body
        if self.threshold >= 0:
            if len(data) >= self.threshold:
                z = zlib.compress(data)
                data = varint(len(data)) + z
            else:
                data = varint(0) + data
        self.s.sendall(varint(len(data)) + data)

    def recv(self) -> tuple[int, bytes]:
        length = read_varint(self.f)
        data = self.f.read(length)
        if self.threshold >= 0:
            b = io.BytesIO(data)
            size = read_varint(b)
            data = zlib.decompress(b.read()) if size else b.read()
        b = io.BytesIO(data)
        pid = read_varint(b)
        return pid, b.read()


def text_of(raw: bytes) -> str:
    """A disconnect reason (JSON in login, NBT later): the readable bits are enough."""
    return " ".join(t.decode("utf-8", "replace") for t in re.findall(rb"[\x20-\x7e]{4,}", raw)[:12])


def vanilla_login(port: int, name: str, protocol: int) -> str:
    c = Conn(port)
    c.send(0x00, varint(protocol) + string("127.0.0.1") + struct.pack(">H", port) + varint(2))
    c.send(0x00, string(name) + uuid.uuid3(uuid.NAMESPACE_OID, "OfflinePlayer:" + name).bytes)
    state = "login"
    deadline = time.time() + 40
    while time.time() < deadline:
        pid, body = c.recv()
        if state == "login":
            if pid == 0x00:
                return "KICKED at login: " + text_of(body)
            if pid == 0x03:
                c.threshold = read_varint(io.BytesIO(body))
            elif pid == 0x02:
                c.send(0x03)  # login acknowledged
                state = "config"
                # client information: en_us, view 8, chat on, colours, skin parts, right hand, no filtering, listed
                c.send(0x00, string("en_us") + bytes([8]) + varint(0) + b"\x01" + bytes([0x7F]) + varint(1) + b"\x00\x01")
            elif pid == 0x04:
                # a login plugin request: vanilla answers "not understood"
                c.send(0x02, varint(read_varint(io.BytesIO(body))) + b"\x00")
        elif state == "config":
            if pid == 0x02:
                return "KICKED in configuration: " + text_of(body)
            if pid == 0x0E:
                # known packs: answer with exactly what the server offered (vanilla core)
                c.send(0x07, body)
            elif pid == 0x04:
                c.send(0x04, body)  # keep alive
            elif pid == 0x05:
                c.send(0x05, body)  # ping -> pong
            elif pid == 0x03:
                c.send(0x03)  # finish configuration acknowledged
                state = "play"
        elif state == "play":
            if pid == 0x1D:
                return "KICKED in play: " + text_of(body)
            if pid == 0x2B:
                # play login: we are in the world; stay a moment to see whether a kick follows
                end = time.time() + 6
                c.s.settimeout(2)
                while time.time() < end:
                    try:
                        pid, body = c.recv()
                    except (socket.timeout, TimeoutError):
                        continue
                    if pid == 0x1D:
                        return "JOINED, then KICKED: " + text_of(body)
                    if pid == 0x26:
                        c.send(0x18, body)  # keep alive
                return "JOINED (stayed 6 s in the world)"
    return "no answer"


# ---------------------------------------------------------------- the two cases


def modded_client(target: str) -> int:
    mc = target.split("-")[0]
    run = ROOT / "run" / f"vanilla-{mc}"
    server_dir = run / "server"
    jar = server_dir / "server.jar"
    if not jar.exists():
        print(f"missing {jar}: download Mojang's official server jar first (see the docstring)")
        return 1
    out = ROOT / "build" / "scenes" / target / "vanilla"
    out.mkdir(parents=True, exist_ok=True)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    write_props(server_dir / "server.properties", {
        "server-port": str(PORT), "online-mode": "false", "enable-rcon": "true", "rcon.port": str(PORT + 10),
        "rcon.password": RCON_PASSWORD, "difficulty": "peaceful", "spawn-protection": "0", "enforce-secure-profile": "false",
    })
    client_dir = ROOT / "run" / target / "client"
    write_props(client_dir / "options.txt", dict(CLIENT_OPTIONS, showSubtitles="true", soundCategory_master="0.02"))
    log = open(out / "vanilla-server.log", "w", encoding="utf-8")
    server = subprocess.Popen([JAVA, "-Xmx2G", "-jar", "server.jar", "nogui"], cwd=server_dir, stdout=log,
                              stderr=subprocess.STDOUT, stdin=subprocess.PIPE)
    procs = []
    try:
        if not wait_file(out / "vanilla-server.log", r"Done \(", 300):
            print("  ! vanilla server did not start")
            return 1
        rcon = Rcon(port=PORT + 10, password=RCON_PASSWORD)
        forge = target.endswith("-forge")
        join = f"-PtestJoin=127.0.0.1:{PORT}" if forge else f"-PquickPlay=127.0.0.1:{PORT}"
        client = Proc(ROOT, f":{target}:runClient", out / "client.log", [join, "-PmcUser=Tester", "-PtestHooks", "-PclientSize=1280x720"])
        procs.append(client)
        if not wait_file(out / "vanilla-server.log", r"Tester joined the game", 900):
            print("  ! client did not join")
            return 1
        time.sleep(6)
        for c in ("time set noon", "weather clear", "gamemode creative Tester", "tp Tester 0.5 100 0.5 90 10",
                  "fill -4 99 -4 4 99 4 grass_block", "fill -4 100 -4 4 104 4 air"):
            rcon.cmd(c)
        ctx = Ctx(rcon, client_dir, out, None)
        hook(client_dir, "camera", "front")
        time.sleep(2)
        hook(client_dir, "toot")
        time.sleep(0.45)
        ctx.shot("modded-client-on-vanilla-server")
        time.sleep(2)
        text = "".join(client.lines)
        lines = [
            f"server answered hello: {'YES (unexpected)' if 'server runs Fart Together protocol' in text else 'no (expected)'}",
            f"local toot played: {'yes' if re.search(r'test: toot by Tester .* own=true', text) else 'NO'}",
            f"gas told by server: {'YES (unexpected)' if 'test: charge' in text else 'no (expected)'}",
            "errors: " + str(len([l for l in text.splitlines() if ('/ERROR]' in l or 'Exception' in l) and 'fart' in l.lower()])),
            f"still connected: {'yes' if 'Tester left the game' not in (out / 'vanilla-server.log').read_text(encoding='utf-8', errors='replace') else 'NO'}",
        ]
        (out / "modded-client-report.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
        print("\n".join(lines))
        return 0
    finally:
        for p in procs:
            kill_tree(p.proc)
        try:
            server.stdin.write(b"stop\n")
            server.stdin.flush()
            server.wait(30)
        except Exception:
            kill_tree(server)
        log.close()


def vanilla_client(target: str) -> int:
    mc = target.split("-")[0]
    out = ROOT / "build" / "scenes" / target / "vanilla"
    out.mkdir(parents=True, exist_ok=True)
    server_dir = ROOT / "run" / target / "server"
    server_dir.mkdir(parents=True, exist_ok=True)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    write_props(server_dir / "server.properties", {
        "server-port": str(PORT), "online-mode": "false", "enable-rcon": "true", "rcon.port": str(PORT + 10),
        "rcon.password": RCON_PASSWORD, "difficulty": "peaceful", "enforce-secure-profile": "false",
    })
    cfg = server_dir / "config" / "farmstead_fart_together-server.toml"
    results = []
    for items in (True, False):
        cfg.parent.mkdir(parents=True, exist_ok=True)
        cfg.write_text(f'preset = "default"\n\n[items]\nenabled = {"true" if items else "false"}\n', encoding="utf-8")
        server = Proc(ROOT, f":{target}:runServer", out / f"server-items-{'on' if items else 'off'}.log", [])
        try:
            if not server.wait_for(r"Done \(\d", 900):
                results.append(f"items {'on' if items else 'off'}: server did not start")
                continue
            time.sleep(2)
            verdict = vanilla_login(PORT, "Vanilla", PROTOCOL[mc])
            time.sleep(1)
            log = "".join(server.lines)
            errors = [l for l in log.splitlines() if ("/ERROR]" in l or "Exception" in l) and "fart" in l.lower()]
            results.append(f"items {'on' if items else 'off'}: {verdict}; mod errors in the server log: {len(errors)}")
        finally:
            kill_tree(server.proc)
            time.sleep(3)
    (out / "vanilla-client-report.txt").write_text("\n".join(results) + "\n", encoding="utf-8")
    print("\n".join(results))
    return 0


def hook(client_dir: Path, name: str, text: str = "") -> None:
    d = client_dir / "testhooks"
    d.mkdir(parents=True, exist_ok=True)
    (d / name).write_text(text, encoding="utf-8")


def wait_file(path: Path, pattern: str, timeout: float) -> bool:
    end = time.time() + timeout
    rx = re.compile(pattern)
    while time.time() < end:
        if path.exists() and rx.search(path.read_text(encoding="utf-8", errors="replace")):
            return True
        time.sleep(1)
    return False


if __name__ == "__main__":
    case, target = sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else "1.21.1-neoforge"
    sys.exit(modded_client(target) if case == "modded-client" else vanilla_client(target))
