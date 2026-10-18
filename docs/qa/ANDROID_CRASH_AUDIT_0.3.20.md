# SkyPeep 0.3.20 — Android crash and lifecycle audit

## Evidence

- Run #445 for the merged 0.3.19 main commit failed before emulator startup with `Error on ZipFile unknown archive` while installing Android 35 system images. The Android Gradle, JUnit, merged-manifest, native ABI and packaged-art checks passed. This is a **CI environment failure**, not proof of an app process crash.
- Runs #443 and #444 passed emulator checks on the preceding 0.3.19 branch. A device-specific user-reported crash has not yet been reproduced or matched to a stack trace.
- LibGDX `Texture(Pixmap)` is an **unmanaged** GPU texture. The game created and disposed Pixmaps for the five costumes, reusable UI chrome, pillar stem, star, trail and one-pixel colour texture; that data then cannot be re-uploaded after an Android EGL context loss. This is a concrete resilience defect regardless of the user's reported crash signature.

## Implemented

- Centralized generated textures in `SkyPeepManagedTexture` using managed `PixmapTextureData`; the source Pixmap is retained for GL restoration and freed exactly once when the Texture is disposed. File-backed original art and local save keys remain unchanged.
- Added a generated-texture management check in game startup/resume logs.
- Extended real emulator smoke with Home → return-to-activity → PAUSE → HOME, process-alive checks and managed-texture diagnostics.
- Added bounded retries and cleanup for corrupt Android 35 SDK system-image downloads in GitHub Actions.
- Updated build `versionName=0.3.20`, `versionCode=32` and gated preview release metadata.

## Remaining verification

- A green emulator run verifies a basic lifecycle transition; it cannot guarantee the emulator actually destroyed its EGL context, or replace testing on the user's crashing physical device.
- Need the actual Android `FATAL EXCEPTION`, native tombstone or Play Console crash report (stack and device/Android version) to definitively identify the reported runtime crash.
- Test every screen and world after context loss on physical devices; verify pixel-level 1:1 against the original references, FPS, cold start and low-memory recovery.
- Unsigned preview AAB is not a Google Play upload bundle; sign and validate later.
