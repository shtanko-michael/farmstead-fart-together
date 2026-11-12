# Fart Together — handoff for the session that starts the mod (2026-11-12)

Written by the session that built and released Footprints, the first mod of the line. Read this, then
`PLAN.md` (this repo), `../ROADMAP.md`, `../RELEASING.md`.

## Where things are

- `C:\Users\admin\farmstead-mods\` — the public mods line; one git repo per mod. Only Footprints has a
  GitHub origin so far (`shtanko-michael/farmstead-footprints`). This repo has no origin: commit locally,
  do not create or push remotes until the owner gives a URL.
- `farmstead-mod-template/` — Stonecutter 0.10 multi-loader template (older than Footprints).
- **`footprints/` is the real reference implementation.** Copy its structure, not the template's:
  - build: `settings.gradle.kts`, `stonecutter.gradle.kts`, `stonecutter.properties.toml`,
    `build.{fabric,forge,neoforge}.gradle.kts`, `buildSrc/`. Gradle needs JDK 25:
    `JAVA_HOME="C:/Users/admin/.jdks/jdk-25.0.5+8" ./gradlew build -q` (all targets) or
    `:1.21.1-neoforge:build`.
  - 22 targets: 1.20.1 fabric/forge; 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2,
    26.3 fabric/neoforge. vcs version = 1.21.1-neoforge. Version-specific code uses Stonecutter
    comments (`//? if >=1.21.2 {` … `/*…*///?} else`). Footprints' sources are a map of every API break
    between those versions (renderer, screens, models, commands, chat messages, NativeImage,
    registries…): grep its `//? if` lines before guessing.
  - Fabric has NO Fabric API dependency: own mixins for tick, commands (`ClientPacketListenerMixin`),
    language, rendering; version-gated mixins are listed through the `${extraClient}` template key in
    `*.mixins.json` (see `build.fabric.gradle.kts`). Fart Together needs networking and server logic,
    so check PLAN.md §3–4 against "zero dependencies"; if Fabric API is truly needed for networking,
    decide it explicitly and say so in the docs.
  - `brand/FarmsteadBrand.java` — every mod logs `[Farmstead] <Name> <ver> started (...)` and checks
    Modrinth for updates once (opt-out key + `-Dfarmstead.updateCheck=false`). Mandatory.
  - `config/` — own TOML config with comments, ranges, presets, generated settings screen (tabs per
    section, block/mob pickers, About tab, "Try it" hook). Config files are named
    `farmstead-<modid>-<side>.toml`.
  - `tools/lang.py` + `tools/lang_more.py` — 12 languages, `--check` fails on missing keys. Mandatory.
  - `client/TestHooks.java` + `scenes/*.py` + `../farmstead-mod-tools/test-matrix/scene.py` — headless
    in-game checks: the runner starts a dev server + client for a target, drives it by RCON and hook
    files (`.shot` screenshots, `record` frames for GIFs, `config.open`, `command.run`…).
    `python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge <scene>`.
    Verify in game by reading screenshots, never report "it compiled" as "it works".
  - `media/gifs_from_frames.py` — recorded frames to listing GIFs.
  - `listing/` — `modrinth.md`, `curseforge.md`, `modrinth.json`, `LISTING.md`.
  - `.github/workflows/{build,release,listing}.yml`, `tools/modrinth_listing.py` — already copied into
    this repo (generic, read everything from stonecutter.properties.toml / listing/modrinth.json).
- `../references/` — cloned sources of other mods (`INDEX.md`). Study how they work, never copy code,
  even MIT.
- `../NICHE-AUDIT.md`, `../ROADMAP.md` — standards: 12 languages, LuckPerms nodes for every action,
  presets, polished media.

## Owner's standing rules (from memory)

- Reply to the owner in Russian. Code, commits, docs in English unless the doc is Russian by design.
- Research names and niches before proposing a name or feature: check Modrinth/CurseForge slugs and
  the whole niche (Footprints was nearly launched into a crowded niche because of a shallow audit).
- Study competitor code, never copy it.
- Every mod: branded start-up line + update check; config `farmstead-<modid>-<side>.toml`.
- Show every render/screenshot/comparison sheet to the owner as it is produced (SendUserFile).
- One agent at a time: no parallel agent waves.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Push only with an origin
  the owner gave. Never touch permission settings. Ask before running downloaded third-party jars.
- Assets: images through Codex CLI (`ai-proxy/tools/SkinGen/skingen.mjs` pattern in the monorepo
  `C:\Users\admin\minecraft-api`), sounds through ElevenLabs CLI (`elevenlabs music compose` /
  sound effects; OAuth on the owner's subscription, no API key). Fart Together needs 20+ sounds.
- Release: follow `../RELEASING.md` (projects, ids as repo variables, tokens as secrets set by the
  owner in a terminal, listing workflow, tag). Listing layout the owner approved: GIFs in a
  two-column table with a bold caption under each.

## Lessons from Footprints worth not repeating

- A `Long.MIN_VALUE` "never computed" sentinel overflowed (`now - MIN`) and silently disabled light
  lookups for weeks: use an explicit flag.
- From 1.21.2 the level's main pass is a lambda: inject into named methods after it, not `renderLevel`.
- mc-publish needs game versions one per line.
- 26.x draws custom geometry before translucent terrain.
- Measure and compare with competitors honestly (screenshots, FPS, features) and tell the owner where
  we are behind.

## First steps

1. Re-check the niche and the name "Fart Together" on Modrinth/CurseForge (slug, competing mods,
   their downloads and features) and report to the owner before coding much.
2. Scaffold from Footprints: rename package (`online.farmsteadminecraft.<mod>`), mod id, branding,
   config, lang tooling, test hooks; get all 22 targets building and a start-up smoke test on each loader.
3. Then PLAN.md MVP: key, pose animation, particle cloud, sounds, server-side rules, LuckPerms.
