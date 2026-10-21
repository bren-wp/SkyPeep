# SkyPeep 0.3.22 — physics timing, map targeting and UI performance

## Concrete fixes

- World-specific gusts are sampled using a **gameplay-only simulation clock**, advanced after each bounded physics substep. The midpoint of each substep is used for sampling. Before this change the wind was sampled from a render-wide scenery animation timer, effectively holding the same gust for the entire render frame even when physics needed multiple substeps.
- Gameplay simulation clock resets at each level start and never advances on paused/menu/ready-for-first-tap states. Background animation remains separate and retains the original cinematic crop.
- Overlapping island tap regions on the illustrated level map now select the geometrically nearest displayed world marker, instead of the lowest world index. Drawing positions and the original clickable island masks remain unchanged.
- Fixed colours for Shop/Settings/victory shading, HUD star state, wallet labels and Settings row titles are cached. This avoids repeated Kotlin/LibGDX allocations every redraw without altering UI composition or tap rectangles.

## Verification

`SkyPeepWindClockTest` compares 60 Hz vs 120 Hz wind impulses for **all six worlds and all three flight licenses**, protects disabled/invalid ticks and confirms world 1 is calm. `SkyPeepMapProgressTest` covers shared edge regions and malformed or out-of-bounds taps.

The existing CI still executes the dead-code audit across production and test sources, hash-verifies restored original image assets, checks bundled Android native libraries and merged offline-only permissions, assembles debug APK and unsigned AAB, and runs Android emulator navigation, worlds, skins, settings, pause and responsive screenshot tests.

## Not yet certified

This does not reconstruct the original artwork behind baked-in sprites, change physics balancing factors or remove cosmetics. The original user-device crash signature has not been supplied. Physical GPU memory/performance, notch and foldable geometry, exact per-pixel visual parity and a signed Google Play bundle still require separate evidence.
