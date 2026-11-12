"""Writes the GIF table (two columns, a bold caption under each, the owner-approved layout) into README.md,
README.ru.md, listing/modrinth.md and listing/curseforge.md (made from modrinth.md with the GIF banner, CurseForge does
not show WebP). The table sits between the "See it" heading and the next paragraph; rerun after new GIFs.

    python -I tools/gif_tables.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RAW = "https://raw.githubusercontent.com/shtanko-michael/farmstead-fart-together/main/"
# clip, English caption, Russian caption
GIFS = [
    ("together", "Toot together: one big shared cloud", "Вместе: одно большое общее облако"),
    ("hold", "Hold to charge, the meter shows it", "Удержание — сила, шкала показывает"),
    ("toot", "Your colour, your sound, your pose", "Ваш цвет, звук и поза"),
    ("scare", "Mobs run from the cloud", "Мобы убегают от облака"),
    ("jet", "Jet jump", "Реактивный прыжок"),
    ("golden", "A golden toot after a golden apple", "Золотой залп после золотого яблока"),
]


def md_table(lang: str) -> str:
    rows = ["| | |", "| --- | --- |"]
    for i in range(0, len(GIFS), 2):
        pair = GIFS[i:i + 2]
        cap = [c[1] if lang == "en" else c[2] for c in pair]
        rows.append("| " + " | ".join(f"![{c}](docs/media/gifs/{g[0]}.gif)" for g, c in zip(pair, cap)) + " |")
        rows.append("| " + " | ".join(f"**{c}**" for c in cap) + " |")
    return "\n".join(rows)


def html_table() -> str:
    rows = ["| | |", "| --- | --- |"]
    for i in range(0, len(GIFS), 2):
        cells = [f'<img src="{RAW}docs/media/gifs/{g}.gif" alt="{c}" width="100%"><br>**{c}**' for g, c, _ in GIFS[i:i + 2]]
        rows.append("| " + " | ".join(cells) + " |")
    return "\n".join(rows)


def put(path: Path, heading: str, table: str) -> None:
    text = path.read_text(encoding="utf-8")
    # between the heading and the first non-table paragraph after it
    pattern = re.compile(rf"({re.escape(heading)}\n\n)(?:GIFS_TABLE\w*|(?:\|.*\n)+)", re.M)
    new, n = pattern.subn(lambda m: m.group(1) + table + "\n", text, count=1)
    if n != 1:
        raise SystemExit(f"{path.name}: no '{heading}' table to replace")
    path.write_text(new, encoding="utf-8")


def main() -> None:
    put(ROOT / "README.md", "## See it", md_table("en"))
    put(ROOT / "README.ru.md", "## Посмотреть", md_table("ru"))
    put(ROOT / "listing" / "modrinth.md", "## ✦ See it", html_table())
    cf = (ROOT / "listing" / "modrinth.md").read_text(encoding="utf-8").replace("docs/media/banner.webp", "docs/media/banner.gif")
    (ROOT / "listing" / "curseforge.md").write_text(cf, encoding="utf-8")
    print("tables written")


if __name__ == "__main__":
    main()
