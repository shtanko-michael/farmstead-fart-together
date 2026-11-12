"""Toot sounds: candidate takes from the ElevenLabs CLI for the owner to choose from, and the chosen ones in the mod.

    python -I tools/sounds.py generate [--takes 5] [slot ...]  # raw takes in build/sounds/takes/ (existing kept)
    python -I tools/sounds.py candidates [slot ...]            # art/sounds/candidates/<slot>/<slot>-<n>.ogg + .mp3
                                                               # and art/sounds/candidates/CANDIDATES.json
    python -I tools/sounds.py install                          # art/sounds/slots.json -> the mod's sounds/toot/*.ogg
    python -I tools/sounds.py sheet                            # build/sounds/sheet.png: waveforms of every take

The owner listens to the candidates and chooses. Swapping the sound of a slot is one line in
`art/sounds/slots.json` (`"short": 3`) followed by `install`, which copies that candidate's .ogg to
src/main/resources/assets/farmstead_fart_together/sounds/toot/<slot>.ogg (sounds.json names only the slot).

`generate` uses the owner's ElevenLabs subscription (OAuth, no API key) and records the credits each take cost
(the subscription's character count before and after the call). Every candidate is processed the same way the
game hears it: leading/trailing silence trimmed, short fades, mixed down to mono (Minecraft positions only mono
sounds in 3D), normalised to -16 LUFS, Vorbis .ogg plus an .mp3 copy for listening in a browser.

Prompts stay cartoonish and family-friendly: the catalogues moderate "cursed" content.
"""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TAKES = ROOT / "build" / "sounds" / "takes"
CANDIDATES = ROOT / "art" / "sounds" / "candidates"
SLOTS_FILE = ROOT / "art" / "sounds" / "slots.json"
OUT = ROOT / "src" / "main" / "resources" / "assets" / "farmstead_fart_together" / "sounds" / "toot"

STYLE = "cartoon sound effect, comedic and family-friendly, clean studio recording, no voice, no music, no laughter"
# Second round (takes 6-10) for the slots the owner found too far from the real thing: a fart, not a toy.
STYLE_V2 = "realistic comedic fart sound effect, close microphone, dry, no voice, no music, no laughter, no other sounds"

# slot: the second round's prompts, one per take 6-10
V2: dict[str, list[str]] = {
    "squeaky": [
        "A short high-pitched squeaky fart, tight and whiny, rising at the end",
        "A squeaky little fart, a thin high squeal of escaping gas",
        "A high squeaky fart that wobbles in pitch, funny and quick",
        "A sharp squeaky fart, a short high-pitched 'pweee'",
        "A squeaky fart squeezed out slowly, high and thin",
    ],
    "long": [
        "A long drawn-out fart, about two seconds, wavering in pitch and slowly trailing off",
        "A long rumbling fart that goes on and on, changing pitch, ending with a little sputter",
        "A long slow fart with a vibrating flutter, fading out at the end",
        "A very long squeaky-to-low fart, sliding down in pitch over two seconds",
        "A long sputtering fart with several bursts at the end",
    ],
    "tiny": [
        "A tiny short fart, a quick quiet 'pfft'",
        "A very small soft fart, one short little toot",
        "A tiny squeaky fart, very short and quiet",
        "A small quick fart, a single tiny pop of air",
        "A tiny polite fart, barely audible, short",
    ],
    "short": [
        "A short quick fart, a single crisp 'pfft', everyday and funny",
        "A quick snappy fart, one short burst",
        "A short punchy fart with a little flutter at the end",
    ],
    "bubbly": [
        "A bubbly fluttering fart, wet-sounding wobble of many tiny bursts",
        "A wobbly rippling fart, a fast flutter like a motorboat, short and silly",
        "A bubbly fart in a bathtub, gurgling bubbles rising, funny",
    ],
    "trumpet": [
        "A loud trumpeting fart, a brassy honk like a horn, proud and comic",
        "A booming fart that sounds like a tuba blast, one big brassy note",
        "A fanfare-like fart, two quick honks and a long brassy note",
    ],
    "rumble": [
        "A deep low rumbling fart, powerful and bassy, short",
        "A huge thunderous fart, low rumble with a rattle, comic and loud",
        "A deep growling fart that rattles, very low pitch, about a second and a half",
    ],
    "puff": [
        "A soft silent-but-deadly fart, a quiet airy hiss of gas with no tone",
        "A gentle muffled fart, soft and airy, more breath than sound",
        "A quiet airy fart, a short soft 'pffff' of escaping air",
        "A soft muffled fart through clothes, low and breathy, very quiet",
        "A barely audible airy fart, a soft whisper of gas",
    ],
}

# slot: purpose, target seconds, prompts (take n uses prompts[min(n, len) - 1]: takes 1-4 share the first prompt
# with different seeds, take 5 is a variation)
SLOTS: dict[str, dict] = {
    "short": {"purpose": "The everyday toot: quick, light, the one heard most often.", "seconds": 0.7, "prompts": [
        "A short quick whoopee cushion toot, a single rubbery 'pfft'",
        "A quick little rubbery whoopee cushion 'prrt', snappy and light",
    ]},
    "squeaky": {"purpose": "High squeak: the funny, harmless one.", "seconds": 0.9, "prompts": [
        "A high-pitched squeaky whoopee cushion squeak, like a balloon neck being stretched",
        "A thin squeaky toot from a rubber balloon, rising in pitch at the end",
    ]},
    "bubbly": {"purpose": "Fluttering, wobbly toot; also the base for the underwater variant later.", "seconds": 1.1, "prompts": [
        "A bubbly fluttering whoopee cushion toot, rubbery and wobbly",
        "A wobbly rubbery flutter, like a whoopee cushion with bubbles, bouncy and silly",
    ]},
    "long": {"purpose": "The long, drawn-out one for a big charge.", "seconds": 2.2, "prompts": [
        "A long whoopee cushion slowly deflating, the pitch wobbling up and down, ending in a little squeak",
        "A long slow balloon deflating with a wavering rubbery tone, ending with a tiny squeak",
    ]},
    "trumpet": {"purpose": "Brassy cartoon blat, a comic fanfare.", "seconds": 1.0, "prompts": [
        "A single comedic low brass blat on a tuba, short and silly, like a cartoon toot",
        "A silly short trombone 'bwap', one low comedic brass note",
    ]},
    "tiny": {"purpose": "A tiny, cute peep for a small charge.", "seconds": 0.5, "prompts": [
        "A tiny cute squeak of air from a small rubber toy, very short and quiet",
        "A very small soft peep of air from a squeezed rubber duck, cute and quiet",
    ]},
    "rumble": {"purpose": "Deep, low and powerful: the big one.", "seconds": 1.3, "prompts": [
        "A deep low rubbery rumble from a big whoopee cushion, short and comedic",
        "A big low rubbery 'brrrp' from a giant whoopee cushion, deep and bouncy",
    ]},
    "puff": {"purpose": "Streamer-safe replacement for every sound: a soft air puff, nothing rude.", "seconds": 0.6, "prompts": [
        "A soft gentle puff of air, a small whoosh like blowing out a candle, nothing rude",
        "A soft short whoosh of air from a small bellows, gentle and clean",
    ]},
}


def ffmpeg() -> str:
    import imageio_ffmpeg
    return imageio_ffmpeg.get_ffmpeg_exe()


def cli() -> str:
    return shutil.which("elevenlabs") or "elevenlabs"


def used_credits() -> int | None:
    r = subprocess.run([cli(), "user", "subscription", "get", "--format", "json"], capture_output=True, text=True)
    try:
        return int(json.loads(r.stdout)["character_count"])
    except (ValueError, KeyError, json.JSONDecodeError):
        return None


def prompt_for(slot: str, n: int) -> str:
    if n >= 6 and slot in V2:
        return f"{V2[slot][(n - 6) % len(V2[slot])]}. {STYLE_V2}."
    prompts = SLOTS[slot]["prompts"]
    return f"{prompts[min(n, len(prompts)) - 1] if n >= 5 else prompts[0]}. {STYLE}."


def body_for(slot: str, n: int) -> dict:
    return {"text": prompt_for(slot, n), "duration_seconds": max(0.5, round(SLOTS[slot]["seconds"] + 0.3, 1)),
            "prompt_influence": 0.55}


def log_path() -> Path:
    return TAKES / "takes.json"


def read_log() -> dict:
    return json.loads(log_path().read_text(encoding="utf-8")) if log_path().exists() else {}


def generate(slots: list[str], takes: int) -> None:
    TAKES.mkdir(parents=True, exist_ok=True)
    log = read_log()
    spent = 0
    for slot in slots:
        for n in range(1, takes + 1):
            out = TAKES / f"{slot}-{n}.mp3"
            if out.exists():
                continue
            body = body_for(slot, n)
            before = used_credits()
            r = subprocess.run([cli(), "text-to-sound-effects", "convert", "--json", json.dumps(body),
                                "-o", str(out), "-q"], capture_output=True, text=True)
            if r.returncode != 0 or not out.exists():
                print(f"  ! {out.name}: {(r.stderr or r.stdout).strip()[:300]}")
                break
            after = used_credits()
            cost = after - before if before is not None and after is not None else None
            spent += cost or 0
            log[out.stem] = {"prompt": body["text"], "request": body, "credits": cost}
            log_path().write_text(json.dumps(log, indent=2), encoding="utf-8")
            print(f"  {out.name} {out.stat().st_size} bytes, {cost} credits")
    print(f"spent this run: {spent} credits")


def analyse(path: Path) -> dict:
    """Length and where the sound starts and ends (above -40 dB)."""
    r = subprocess.run([ffmpeg(), "-hide_banner", "-i", str(path), "-af", "silencedetect=noise=-40dB:d=0.05",
                        "-f", "null", "-"], capture_output=True, text=True, errors="replace")
    log = r.stderr
    dur = re.search(r"Duration: (\d+):(\d+):([\d.]+)", log)
    length = int(dur.group(1)) * 3600 + int(dur.group(2)) * 60 + float(dur.group(3)) if dur else 0.0
    starts = [float(x) for x in re.findall(r"silence_start: ([\d.]+)", log)]
    ends = [float(x) for x in re.findall(r"silence_end: ([\d.]+)", log)]
    head = ends[0] if starts and starts[0] < 0.01 and ends else 0.0
    tail = length - starts[-1] if starts and len(starts) > len(ends) else 0.0
    return {"length": length, "head": head, "span": max(0.05, length - head - tail)}


def process(src: Path, dst_ogg: Path, dst_mp3: Path) -> float:
    a = analyse(src)
    start = max(0.0, a["head"] - 0.01)
    length = a["span"] + 0.08
    fade = min(0.12, length / 4)
    af = (f"atrim=start={start:.3f}:duration={length:.3f},asetpts=PTS-STARTPTS,"
          f"afade=t=in:d=0.005,afade=t=out:st={max(0.0, length - fade):.3f}:d={fade:.3f},"
          "loudnorm=I=-16:TP=-1.5:LRA=11")
    common = [ffmpeg(), "-hide_banner", "-loglevel", "error", "-y", "-i", str(src), "-af", af, "-ac", "1", "-ar", "44100"]
    subprocess.run([*common, "-c:a", "libvorbis", "-q:a", "5", str(dst_ogg)], check=True)
    subprocess.run([*common, "-c:a", "libmp3lame", "-q:a", "2", str(dst_mp3)], check=True)
    return round(analyse(dst_ogg)["length"], 2)


def candidates(slots: list[str]) -> None:
    log = read_log()
    index_path = CANDIDATES / "CANDIDATES.json"
    index = json.loads(index_path.read_text(encoding="utf-8")) if index_path.exists() else {"slots": {}}
    for slot in slots:
        d = CANDIDATES / slot
        d.mkdir(parents=True, exist_ok=True)
        entry = {"purpose": SLOTS[slot]["purpose"], "takes": []}
        for src in sorted(TAKES.glob(f"{slot}-*.mp3"), key=lambda p: int(p.stem.split("-")[1])):
            n = int(src.stem.split("-")[1])
            ogg, mp3 = d / f"{src.stem}.ogg", d / f"{src.stem}.mp3"
            seconds = process(src, ogg, mp3)
            info = log.get(src.stem, {})
            entry["takes"].append({
                "take": n,
                "file": f"{slot}/{ogg.name}",
                "mp3": f"{slot}/{mp3.name}",
                "prompt": info.get("prompt", prompt_for(slot, n)),
                "durationSeconds": seconds,
                "requestedSeconds": body_for(slot, n)["duration_seconds"],
                "credits": info.get("credits"),
            })
            print(f"  {slot}/{ogg.name} {seconds:.2f} s, credits {info.get('credits')}")
        index["slots"][slot] = entry
    known = [t["credits"] for s in index["slots"].values() for t in s["takes"] if t["credits"] is not None]
    index["note"] = ("Choose one take per slot, then set it in art/sounds/slots.json and run "
                     "`python -I tools/sounds.py install`. Takes 1-4 share a prompt (different seeds), take 5 "
                     "is a variation. credits = the subscription's character count before/after the call; null "
                     "means it was not measured (generated before measuring was added).")
    index["creditsMeasured"] = sum(known)
    index_path.write_text(json.dumps(index, indent=2, ensure_ascii=False), encoding="utf-8")
    print(index_path)


def install() -> None:
    chosen = json.loads(SLOTS_FILE.read_text(encoding="utf-8"))
    OUT.mkdir(parents=True, exist_ok=True)
    for slot, take in chosen.items():
        if slot.startswith("_"):
            continue
        src = CANDIDATES / slot / f"{slot}-{take}.ogg"
        shutil.copy2(src, OUT / f"{slot}.ogg")
        print(f"  {slot}: take {take} -> {(OUT / f'{slot}.ogg').relative_to(ROOT)}")


def sheet() -> None:
    """One waveform strip per take, one row per slot."""
    ff = ffmpeg()
    strips = TAKES.parent / "strips"
    strips.mkdir(parents=True, exist_ok=True)
    chosen = json.loads(SLOTS_FILE.read_text(encoding="utf-8")) if SLOTS_FILE.exists() else {}
    rows = []
    for slot in SLOTS:
        cells = []
        for p in sorted((CANDIDATES / slot).glob(f"{slot}-*.ogg"), key=lambda p: int(p.stem.split("-")[1])):
            png = strips / f"{p.stem}.png"
            mine = str(chosen.get(slot)) == p.stem.split("-")[1]
            subprocess.run([ff, "-hide_banner", "-loglevel", "error", "-y", "-i", str(p), "-filter_complex",
                            f"showwavespic=s=300x80:colors={'0x7FBF3F' if mine else '0x9a9a9a'},"
                            f"drawtext=text='{p.stem}':x=6:y=4:fontcolor=white:fontsize=14,"
                            "pad=310:90:5:5:color=0x202020", "-frames:v", "1", str(png)], check=True)
            cells.append(png)
        if cells:
            rows.append(cells)
    width = max(len(r) for r in rows)
    inputs, filters, labels, idx = [], [], [], 0
    for ri, r in enumerate(rows):
        for c in r:
            inputs += ["-i", str(c)]
        ins = "".join(f"[{idx + i}:v]" for i in range(len(r)))
        pad = "" if len(r) == width else f",pad={310 * width}:90:0:0:color=0x101010"
        filters.append(f"{ins}hstack=inputs={len(r)}{pad}[r{ri}]" if len(r) > 1 else f"{ins}copy{pad}[r{ri}]")
        labels.append(f"[r{ri}]")
        idx += len(r)
    filters.append(f"{''.join(labels)}vstack=inputs={len(rows)}[out]")
    out = TAKES.parent / "sheet.png"
    subprocess.run([ff, "-hide_banner", "-loglevel", "error", "-y", *inputs, "-filter_complex", ";".join(filters),
                    "-map", "[out]", "-frames:v", "1", str(out)], check=True)
    print(out)


def main() -> int:
    args = sys.argv[1:]
    if not args:
        print(__doc__)
        return 2
    cmd, rest = args[0], args[1:]
    takes = 5
    if "--takes" in rest:
        i = rest.index("--takes")
        takes = int(rest[i + 1])
        rest = rest[:i] + rest[i + 2:]
    slots = rest or list(SLOTS)
    if cmd == "generate":
        generate(slots, takes)
    elif cmd == "candidates":
        candidates(slots)
    elif cmd == "install":
        install()
    elif cmd == "sheet":
        sheet()
    else:
        print(__doc__)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
