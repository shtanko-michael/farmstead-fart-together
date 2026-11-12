# Fart Together

Toot with style: animated poses, gas clouds and 24+ handcrafted sounds. Charge up on beans, scare mobs, jet-jump and pull off team combos with friends. Family-friendly by default.

Status: scaffold — all 22 targets build, start-up smoke check in game; gameplay not written yet. See [PLAN.md](PLAN.md).

Make your toot your own: cloud colour, particles, sound, pitch and pose, and your friends see and hear exactly your style.

Requires: nothing on NeoForge and Forge; [Fabric API](https://modrinth.com/mod/fabric-api) on Fabric (from the first gameplay release; the scaffold does not need it yet).

## Building

    JAVA_HOME=<JDK 25> ./gradlew build                         # every target
    JAVA_HOME=<JDK 25> ./gradlew :1.21.1-neoforge:build        # one target
    python -I tools/lang.py                                   # write and check the 12 lang files
    python -I ../farmstead-mod-tools/test-matrix/scene.py . 1.21.1-neoforge smoke   # in-game smoke check

Loaders: Fabric · NeoForge · Forge. Minecraft: 1.20.1 (Fabric, Forge), 1.21.1–1.21.11 and 26.1–26.3 (Fabric, NeoForge).

License: code — MIT, assets — CC BY-NC 4.0 (see [LICENSE](LICENSE)).
