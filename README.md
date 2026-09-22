# Eclipse Realms

A 3D action RPG - overworld exploration, a town with a shop, combat skills,
and procedurally generated multi-floor dungeons. Built with
[libGDX](https://libgdx.com/) (Java), targeting **Android (APK)** and
**Desktop (Windows/Linux/Mac)** from one codebase.

This project was rebuilt from scratch to run smoothly on low-end hardware -
specifically **8GB RAM / Snapdragon 680-class phones** and **3rd-gen Intel
i3 laptops** - and has **no cloud/API dependency**: everything (including
character models) is generated procedurally at runtime, so there is nothing
to download, no API key, and no network calls.

## What changed vs. a "normal" high-end build

| Problem on weak hardware | Fix in this project |
|---|---|
| Stutter from GC pauses when spawning enemies | `ObjectPool` - enemies are recycled, never `new`'d mid-fight (`util/ObjectPool.java`, `entities/enemies/Enemy.java`) |
| GPU overload from high-poly textured models | Every character is a small set of untextured flat-shaded primitives, generated in code (`graphics/CharacterModelFactory.java`) - typically under 300 triangles and one draw call each |
| CPU overload from enemy AI running every render frame | AI runs on its own fixed low-Hz "logic tick", decoupled from render rate (`util/DevicePerformanceTier.logicTickHz()`, used in `screens/DungeonScreen.java`) |
| Fixed quality settings that are wrong for half of all devices | `util/DevicePerformanceTier` auto-detects device RAM/cores on Android, watches real frame times at runtime, and scales draw distance, enemy count, particle count, resolution and target FPS up or down automatically |
| Screens leaking GL memory over a long session | `EclipseRealmsGame.switchScreen()` explicitly disposes the outgoing screen (libGDX's `Game.setScreen()` does not do this by itself) |
| Shadows / MSAA costing frame time on weak GPUs | Both disabled by default; only enabled if the device proves it has headroom |

## Every character looks different

There are no reused/recolored models. Each character - Warrior, Mage, Rogue,
and every enemy type (Slime, Skeleton, Bandit, Golem) plus the Shopkeeper NPC
- has its own distinct proportions, silhouette, palette and signature prop,
all defined separately in `graphics/CharacterModelFactory.java`. Because
they're built from primitives instead of imported art assets, there are no
large model/texture files to ship or load.

## Project layout

```
eclipse-realms/
  core/     - all game logic + rendering (shared by both platforms)
  android/  - Android module -> exports the .apk
  desktop/  - Desktop (LWJGL3) module -> exports a runnable jar
  .github/workflows/build-apk.yml  - CI: builds a debug APK on every push
```

Key files in `core/src/main/java/com/eclipserealms/game/`:
- `util/DevicePerformanceTier.java` - the adaptive performance manager
- `graphics/CharacterModelFactory.java` - procedural, distinct character models
- `entities/`, `entities/enemies/` - player and enemy logic
- `combat/` - skills + damage resolution
- `world/` - town, shop, inventory, dungeon generation
- `input/` - shared touch (virtual joystick) + keyboard/mouse controller
- `ui/` - HUD and shop panel (same code path on phone and PC)
- `screens/` - MainMenu, Town (overworld+shop), Dungeon

## Building it yourself

**Prerequisites:** JDK 17, Android Studio (Giraffe or newer) if building the APK.

### Open in Android Studio (recommended)
1. Open this folder in Android Studio.
2. Let it sync - Android Studio will generate the Gradle wrapper
   (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) automatically on first
   sync if they aren't already present.
3. Run the `android` configuration on a device/emulator, or
   **Build > Build Bundle(s) / APK(s) > Build APK(s)** to get an installable
   `.apk` under `android/build/outputs/apk/`.

### Command line (if you have Gradle installed)
```
gradle :android:assembleDebug     # -> android/build/outputs/apk/debug/
gradle :desktop:run               # run the PC build directly
```
Once you've generated the wrapper once (via Android Studio, or by running
`gradle wrapper` yourself), you can use `./gradlew` instead of `gradle` from
then on, and commit the wrapper files so CI and other machines don't need
Gradle preinstalled.

### GitHub CI
`.github/workflows/build-apk.yml` builds a debug APK on every push to `main`
and attaches it to the workflow run as a downloadable artifact - push this
repo to GitHub and you'll get an APK out of the Actions tab without needing
Android Studio at all. It provisions Gradle itself, so it works immediately
even before you've committed a Gradle wrapper.

## Performance tuning knobs

If a specific device still feels heavy, the first things to try in
`util/DevicePerformanceTier.java`:
- Lower `resolutionScale()` for `LOW` further (e.g. `0.6f`)
- Lower `maxVisibleEnemies()` for `LOW`
- Raise `CHECK_INTERVAL_SEC` / adjust the step-down threshold in `update()`
  if the tier flips too eagerly

Desktop testing tip: run with `--lowspec` to force the same LOW tier an
8GB/SD680 phone would get, e.g. from the `desktop` module:
```
gradle :desktop:run --args="--lowspec"
```

## Known scope of this build

This is a genuine, playable vertical slice - not a stub - covering one
town, one 5-floor dungeon type, 3 classes with 3 skills each, and 4 enemy
types. It's intentionally scoped so every system in it (pooling, adaptive
performance, procedural models, dungeon generation) is real and extendable,
rather than being a much larger but shallower project. Natural next additions
(more dungeons, a full inventory/equipment screen, saved progress) slot into
the existing `world/` and `ui/` packages without needing architecture
changes.
