# SkyPeep 0.3.38 — local save recovery, optional haptics and visual QA

## Starting point

Current main cf6c978f2799b01e73f49a006a21c951d34642ad (verified 0.3.37 preview and real 16-image screenshot gallery). All original JPGs and the immutable art archive remain unchanged. Physics, world rules, prices and stable Android preference keys are unchanged.

## Reproducible defects addressed

1. When a preference flush threw an exception, the warning could be cleared after the next successful **partial** coin/score save, even though costumes, settings, other-world counters and stars from an earlier full snapshot might still be missing on disk. The storage guard now marks a full snapshot as pending on any flush failure, retries a full `save()` before partial saves, and clears recovery only on a successful full write. Unit tests model repeated failures, successful partial writes and a valid full recovery.
2. The existing user-visible Vibration switch used the Android haptic API but AndroidManifest did not declare the normal `android.permission.VIBRATE` permission. It is now declared and CI verifies that both merged build variants preserve the permission. It has no runtime permission prompt or internet access.
3. The skin/trail BUY and EQUIP hitboxes were functional, but the player could still attempt in-game purchases with unreadable original saved data or a failed storage snapshot. These actions are now disabled until the save gate is safe, preventing misleading purchase-success feedback in those states.

## Visual reference scope

Four source references: `assets/previews/menu.jpg`, `levels.jpg`, `skins.jpg`, `gameplay.jpg`. Ten actual emulator screen states and six gameplay-world PNG screenshots already appear in the public release gallery and player README. The screen comparator now emits upper/middle/lower region MAEs alongside whole-screen MAE, helping locate specific regions with source deviations. No reference exists for independent Shop, Settings, Splash, Pause and results layouts; these can be inspected for consistency but never claimed 1:1 to a non-existent image.

Source-original reference assets and runtime screenshots must never be conflated. Dynamic scores, three hearts, collected wallet amounts and live bird/obstacle positions legitimately differ from baked reference art. Issue #44 remains open for manual seam inspection in six worlds.

## Verification gate

- `python3 tools/dead_code_audit.py`
- `python3 tools/check_release_shell.py .github/workflows/android.yml`
- `java -Djava.awt.headless=true tools/RebuildWorlds.java --self-test`
- Gradle JVM tests, APK/AAB builds, checksum and normal VIBRATE manifest permission checks
- Real API 35 emulator screen navigation, purchases, save isolation, pause/resume, all six worlds and six viewport sizes
- `tools/CompareSkyPeepScreens.java`: honest numerical deviation report, with region breakdown and real emulator images
- Only a fully successful trusted-branch run may publish the clearly labeled unsigned release APK/AAB and debug-signed test APK

All outcomes are pending until the new CI run completes. No device-specific memory/leak, 60/90/120Hz timing, OEM haptic hardware or pixel-perfect certification is implied.
