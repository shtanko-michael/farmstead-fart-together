"""Fills the listening page with the take prompts and writes files.json (published path -> source file).

    python -I art/sounds/picker/prepare.py      (from the repo root)
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CAND = ROOT / "art" / "sounds" / "candidates"
d = json.loads((CAND / "CANDIDATES.json").read_text(encoding="utf-8"))
prompts = {k: [t["prompt"] for t in v["takes"]] for k, v in d["slots"].items()}
page = Path(__file__).with_name("index.html")
s = page.read_text(encoding="utf-8")
if "__PROMPTS__" in s:
    page.write_text(s.replace("__PROMPTS__", json.dumps(prompts, ensure_ascii=False)), encoding="utf-8")
files = {}
for k, v in d["slots"].items():
    for t in v["takes"]:
        files[f"sounds/{k}/{k}-{t['take']}.mp3"] = (CAND / t["mp3"]).as_posix()
Path(__file__).with_name("files.json").write_text(json.dumps(files, indent=1), encoding="utf-8")
print(len(files), next(iter(files.items())))
