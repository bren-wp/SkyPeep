# SkyPeep 0.3.25 — Android rendering, battery and maintainability audit

## Confirmed code changes

- **Demand-driven GPU rendering:** `SkyPeepRenderPolicy` switches from continuous rendering during PLAY and timed notifications to input/event-driven rendering for static menu, map, Shop, Settings, Pause and Result screens. The Android UI still requests a fresh frame after Home/Resume, and taps trigger frames through libGDX. This avoids needlessly redrawing large portrait photos while idle or paused.
- **Sensors disabled:** `MainActivity` explicitly disables Android accelerometer, compass and gyroscope registration because SkyPeep uses one-tap controls, not tilt control.
- **Removed hot-loop allocations:** Numeric score/coins/gems labels now come from `SkyPeepHudTextCache`, which recalculates only when values change. Text draw calls reuse a temporary Color object instead of allocating another for each label. Stone cap textures are resolved once per frame rather than once per obstacle.
- **Separation of concerns:** Procedural nine-patch panels and victory-star rasterization have moved out of the large `SkyPeepGame.kt` class to `SkyPeepUiArtwork.kt`. The factory now reuses a single mutable Color while constructing pixels, avoiding roughly 100,000 short-lived startup Color objects. Source artwork geometry, palette, and GPU management remain unchanged.
- **QA:** `SkyPeepRenderPolicyTest` asserts that all static screens idle and that gameplay and timed notification remain animated. `SkyPeepHudTextCacheTest` verifies label reuse, state changes and negative-value normalization; emulator smoke asserts live PLAY / idle PAUSE transitions. Existing JVM and Android tests continue to check physics, world obstacles, controls, six worlds, textures, permissions and all three release binaries.

## What these changes do not claim

These optimizations do **not** mean the Android app runs at a fixed 60 fps; libGDX's Android backend ignores the desktop `setForegroundFPS` method. This build keeps continuous rendering during gameplay and lets Android/driver VSYNC govern actual display refresh. Battery improvements are architectural expectations, not measured power consumption on a user's phone; physical-device profiling is still required.

The large gameplay class has been reduced and the new components individually tested. A more extensive extraction of gameplay state and screen navigation would be higher-risk and should be performed incrementally with complete emulator/visual comparison and save migration safeguards. No original JPG/PNG/audio assets were deleted or recompressed as part of this change, so visual parity and source-data integrity are retained where already tested.

## Distribution

Version 0.3.25 / versionCode 37 is a new development version; release automation is gated on both the Android build and emulator. It must include a debug test APK, an unsigned release APK, an unsigned Google Play AAB, a source-pinned F-Droid metadata file, MIT license, checksums and signing instructions. No signing keys, F-Droid acceptance or Google Play store deployment are implied. Existing published releases stay intact.
