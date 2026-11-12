# Fart Together

Toot with style: animated poses, gas clouds and 24+ handcrafted sounds. Charge up on beans, scare mobs, jet-jump and pull off team combos with friends. Family-friendly by default.

Status: M1 — key G (rebindable) toots with a pose, a particle cloud and one of 8 sounds; each player's style is synced through the server, which clamps it to its rules. Food charge, items, gas cloud gameplay, combos: next milestones. See [PLAN.md](PLAN.md).

Make your toot your own: cloud colour, particles, sound, pitch and pose, and your friends see and hear exactly your style.

Requires: nothing on NeoForge and Forge; [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric.

Install it on the server and on clients. A client on a server without the mod still toots, but only for itself; players without the mod see nothing.

Permissions (LuckPerms or any permission mod of the loader): `farmstead_fart_together.use` (default on), `.style.custom` (on), `.style.rainbow` (gradients and rainbow, off), `.style.huge` (x1.5 size and duration caps, off), `.style.sounds.rare` (off).

## Building

    JAVA_HOME=<JDK 25> ./gradlew build                         # every target
    JAVA_HOME=<JDK 25> ./gradlew :1.21.1-neoforge:build        # one target
    python -I tools/lang.py                                   # write and check the 12 lang files
    python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge smoke   # in-game smoke check
    python -I tools/duo.py 1.21.1-neoforge                    # two clients: one sees and hears the other's toot
    python -I tools/sounds.py install                         # put the sound takes chosen in art/sounds/slots.json into the mod

Loaders: Fabric · NeoForge · Forge. Minecraft: 1.20.1 (Fabric, Forge), 1.21.1–1.21.11 and 26.1–26.3 (Fabric, NeoForge).

License: code — MIT, assets — CC BY-NC 4.0 (see [LICENSE](LICENSE)).
