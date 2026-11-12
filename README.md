# Fart Together

Toot with style: animated poses, gas clouds and 24+ handcrafted sounds. Charge up on beans, scare mobs, jet-jump and pull off team combos with friends. Family-friendly by default.

Status: M2 — key G (rebindable) toots with a pose, a particle cloud and one of 8 sounds. Gas comes from food (beans and cabbage are the mod's own crops) and sets the strength: sound, loudness and cloud size. The toot leaves a gas cloud behind that scares mobs away (harmless by default). Each player's style is synced through the server, which clamps it to its rules; the "My style" settings tab has a live preview. Jet jump, team combos and achievements: next milestone. See [PLAN.md](PLAN.md).

Make your toot your own: cloud colour, particles, sound, pitch and pose, and your friends see and hear exactly your style.

Requires: nothing on NeoForge and Forge; [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric.

Install it on the server and on clients. A client on a server without the mod still toots, but only for itself; players without the mod see nothing. Clients without the mod can log in (checked with a vanilla login on NeoForge and Fabric 1.21.1), but beans, cabbage and the crops are registered content they do not know: what a vanilla client does when it meets them (an item in a hand, a crop in a chunk) is not tested yet. A dedicated server with vanilla players can set `items.enabled = false` in its file (restart): then the mod adds nothing to the registries; the server log then shows a few harmless "unknown key" lines for the mod's recipe and loot tables.

## Gas

- Food gives gas: beans 15, cabbage 12, a bowl of beans 60, beetroot soup 35, mushroom stew 30, rotten flesh 20, an enchanted golden apple fills the meter; milk empties it. Any other food gives `charge.unlistedFood` (2).
- The weights are data: `data/<namespace>/gas_foods/*.json`, for example `{"foods": {"minecraft:beetroot": 10, "#c:foods/vegetable": 4}}`. A datapack changes ours by shipping the same path (`data/farmstead_fart_together/gas_foods/vanilla.json`) or adds its own file; `/reload` or `/fart reload` applies it.
- A toot needs `charge.minToToot` (10) and lets out all the gas: under a third of the meter is a small toot, then medium, big, and a full meter.
- Beans and cabbage seeds drop from grass and ferns now and then; a bowl of beans is a bowl and three beans.

## Server file

`config/farmstead_fart_together-server.toml`, presets `default`, `family` (longer cooldown, no effects), `plain` (no personal styles), `chaos` (fast toots, big clouds with nausea that reach other players).

| Section | Keys |
| --- | --- |
| `toot` | `cooldownSeconds` |
| `charge` | `required`, `max`, `minToToot`, `passivePerMinute`, `unlistedFood` |
| `cloud` | `enabled`, `durationSeconds`, `radius`, `scareMobs`, `scareRange`, `scaredMobs` (ids or `#tags`), `effects` (`"minecraft:nausea 4 1"`), `affectsPlayers` (off: no PvP), `respectServerPvp` |
| `items` | `enabled` (dedicated servers, restart), `beansFromGrass`, `cabbageSeedsFromGrass` |
| `style` | `allowCustom`, `maxSize`, `maxAmount`, `maxDuration`, `allowedParticles`, `allowedSounds`, `updateCooldownSeconds` |

## Commands

| Command | Who | What |
| --- | --- | --- |
| `/fart info` | everyone | your gas, cooldown, cloud radius; the server's rules |
| `/fart reload` | operators | reads the server file and the gas food tables again |
| `/fart charge <players> <amount>` / `full` | operators | sets gas |
| `/fart toggle <players>` | operators | switches a player's toots off or back on |
| `/farttogether reload` / `config` | client | your own settings |

## Permissions (LuckPerms or any permission mod of the loader)

| Node | Default | What it allows |
| --- | --- | --- |
| `farmstead_fart_together.use` | everyone | toot |
| `farmstead_fart_together.style.custom` | everyone | a personal style (otherwise the plain green puff) |
| `farmstead_fart_together.style.rainbow` | nobody | gradient and rainbow colours |
| `farmstead_fart_together.style.huge` | nobody | size and duration caps x1.5 |
| `farmstead_fart_together.style.sounds.rare` | nobody | rare sounds by name (none yet) |
| `farmstead_fart_together.cloud.bypass` | nobody | cloud effects never reach this player |
| `farmstead_fart_together.command.info` | everyone | `/fart info` |
| `farmstead_fart_together.command.reload` | operators | `/fart reload` |
| `farmstead_fart_together.command.charge` | operators | `/fart charge` |
| `farmstead_fart_together.command.toggle` | operators | `/fart toggle` |

| Meta (number) | Default | What it sets |
| --- | --- | --- |
| `farmstead_fart_together.cooldown` | `toot.cooldownSeconds` | seconds between toots |
| `farmstead_fart_together.cloud.radius` | `cloud.radius` | radius of a full-strength cloud |
| `farmstead_fart_together.charge.max` | `charge.max` | size of the gas meter |

Example: `/lp group vip meta set farmstead_fart_together.cooldown 1`. On NeoForge and Forge the meta is read through the loader's permission API (string nodes), on Fabric through fabric-permissions-api's options.

## Building

    JAVA_HOME=<JDK 25> ./gradlew build                         # every target
    JAVA_HOME=<JDK 25> ./gradlew :1.21.1-neoforge:build        # one target
    python -I tools/lang.py                                   # write and check the 12 lang files (and every key the code uses)
    python -I tools/gen_content.py                            # models, blockstates, loot, recipe, gas foods (+ placeholder textures if missing)
    python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge smoke   # in-game smoke check
    python -I tools/duo.py 1.21.1-neoforge                    # two clients: styles, gas, cloud, crops, commands, My style
    python -I tools/sounds.py install                         # put the sound takes chosen in art/sounds/slots.json into the mod

Data files are written once in the 1.21.2+ form; `buildSrc/src/main/kotlin/DataCompat.kt` rewrites folder names and recipe shapes for 1.20.1 and 1.21.1 while the jar is built.

Loaders: Fabric · NeoForge · Forge. Minecraft: 1.20.1 (Fabric, Forge), 1.21.1–1.21.11 and 26.1–26.3 (Fabric, NeoForge).

License: code — MIT, assets — CC BY-NC 4.0 (see [LICENSE](LICENSE)).
