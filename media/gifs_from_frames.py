"""Recorded frames (tools/duo.py --record) -> docs/media/gifs/<clip>.gif, 560 px wide, 15 fps, own palette per clip.

    python -I media/gifs_from_frames.py [target]      default target: 1.21.1-neoforge
"""
import subprocess
import sys
from pathlib import Path

import imageio_ffmpeg

ROOT = Path(__file__).resolve().parent.parent
target = sys.argv[1] if len(sys.argv) > 1 else "1.21.1-neoforge"
srcs = [ROOT / "run" / target / d / "recordings" for d in ("client", "client2")]
out = ROOT / "docs" / "media" / "gifs"
out.mkdir(parents=True, exist_ok=True)
ff = imageio_ffmpeg.get_ffmpeg_exe()
for clip in sorted((p for src in srcs if src.exists() for p in src.iterdir() if p.is_dir()), key=lambda p: p.name):
    frames = sorted(clip.glob("*.png"))
    if not frames:
        continue
    gif = out / f"{clip.name}.gif"
    vf = "fps=15,scale=560:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=200:stats_mode=diff[p];[b][p]paletteuse=dither=bayer:bayer_scale=4:diff_mode=rectangle"
    subprocess.run([ff, "-y", "-v", "error", "-framerate", "20", "-i", str(clip / "%05d.png"), "-vf", vf, "-loop", "0", str(gif)],
                   check=True)
    print(f"{gif.name}: {len(frames)} frames, {gif.stat().st_size // 1024} KB")
