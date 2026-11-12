# Fart Together — карточки Modrinth и CurseForge

Всё, что вводится руками при создании проектов. Тексты описаний — в [modrinth.md](modrinth.md) и [curseforge.md](curseforge.md) (английский, Markdown). Версии загружает CI ([release.yml](../.github/workflows/release.yml)), страницу Modrinth синхронизирует [listing.yml](../.github/workflows/listing.yml) из [modrinth.json](modrinth.json); руками — только создание проектов и страница CurseForge.

## Общее

| Поле | Значение |
| --- | --- |
| Название | `Fart Together` |
| Slug | Modrinth и CurseForge: `farmstead-fart-together` (id появятся после создания проектов; CI читает их из переменных репозитория `MODRINTH_ID` / `CURSEFORGE_ID`) |
| Краткое описание (≤ 150 знаков) | `Cartoon toots with real gameplay: hold to charge, gas from beans, clouds that scare mobs, jet jumps and team combos. Family-friendly.` |
| Автор / организация | Farmstead |
| Лицензия | MIT (код). Ассеты — CC BY-NC 4.0, оговорено в LICENSE. На Modrinth: `MIT`, ссылка на LICENSE |
| Иконка | [`docs/media/icon-512.png`](../docs/media/icon-512.png) (512×512, Codex, выбор владельца — вариант 1); в моде — 256×256 |
| Баннер | [`docs/media/banner.webp`](../docs/media/banner.webp) 1280×480 — Modrinth и README; [`banner.gif`](../docs/media/banner.gif) 800×300 — CurseForge (WebP там не показывается) |
| Логотип | [`docs/media/logo.png`](../docs/media/logo.png): иконка, «FART TOGETHER» пиксельным шрифтом, «BY FARMSTEAD» |
| Карточка для соцсетей | [`docs/media/social-card.png`](../docs/media/social-card.png) 1280×640 — GitHub → Settings → Social preview |

Тон описаний — семейный: «toot», «gas», никаких грубых слов и картинок (площадки модерируют «cursed»).

## Modrinth

| Поле | Значение |
| --- | --- |
| Тип | Mod |
| Categories | `game-mechanics`, `social`; дополнительно `food`, `cursed` (ниша мода — «cursed»; если владелец не хочет этот ярлык — убрать из `modrinth.json`) |
| Environment | Client: **Required**, Server: **Required** |
| Loaders | Fabric, Quilt, Forge, NeoForge |
| Issues / Source / Wiki | ссылки на GitHub (в `modrinth.json`) |
| Зависимости (ставит CI на каждую версию) | Fabric-файлы: **Fabric API — required** (`P7dR8mSH`), Mod Menu — optional (`mOgUt4GM`); NeoForge/Forge — никаких |

## CurseForge

| Поле | Значение |
| --- | --- |
| Класс | Mods (Minecraft) |
| Main category | **Miscellaneous**; дополнительно Food, Player Transport (реактивный прыжок) — по желанию |
| Описание | тип Markdown, вставить [curseforge.md](curseforge.md) |
| Environment у файлов | Client and Server |
| Relations (ставит CI) | Fabric-файлы: Fabric API (`306612`) — Required, Mod Menu (`308702`) — Optional |
| Галерея | папка `build/curseforge-gallery/` (`python -I tools/brand.py gallery`), порядок и подписи — в её `CAPTIONS.md` |

## Файлы и версии

Каждый релиз — 22 файла (`farmstead-fart-together-<loader>-X.Y.Z+<mc>.jar`), по одному на цель из `stonecutter.properties.toml`. Номер версии на площадках `X.Y.Z+<mc>-<loader>`, название `Fart Together X.Y.Z (<Loader> <mc>)`. Проверка обновлений в моде сравнивает только `X.Y.Z`.

## Галерея

[`docs/media/gallery/`](../docs/media/gallery) — кадры из игры (тестовый стенд, без шейдеров), подписи — в [modrinth.json](modrinth.json). GIF функций — [`docs/media/gifs/`](../docs/media/gifs).

## Что должен сделать владелец (один раз)

1. Создать проект на **Modrinth** со slug `farmstead-fart-together` (поля выше) и прислать его id.
2. Создать проект на **CurseForge** со slug `farmstead-fart-together` и прислать его id.
3. Записать id в переменные репозитория:

       gh variable set MODRINTH_ID   -R shtanko-michael/farmstead-fart-together -b <modrinth id>
       gh variable set CURSEFORGE_ID -R shtanko-michael/farmstead-fart-together -b <curseforge id>

4. Вставить токены (в терминале, вставить по запросу):

       gh secret set MODRINTH_TOKEN   -R shtanko-michael/farmstead-fart-together
       gh secret set CURSEFORGE_TOKEN -R shtanko-michael/farmstead-fart-together

5. Actions → Release → Run workflow (dry run) — проверить, что собраны все 22 jar.
6. Тег `v0.1.0` и push тега — публикация. Затем Actions → Listing (сначала dry run) — страница Modrinth; страницу и галерею CurseForge — руками.

## Что ещё не готово

- [ ] Видео-ролик (для серии рилсов) — не делался.
- [ ] Звук «хора» для комбо и «музыкальный» редкий залп — сейчас хор собран из трёх существующих звуков.
- [ ] Своя текстура мягкого облачка (Codex) вместо ванильного дыма костра.
- [ ] Бобы в сундуках на 26.3 (новые построители лута).
