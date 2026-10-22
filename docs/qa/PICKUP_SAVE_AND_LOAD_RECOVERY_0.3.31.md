# SkyPeep 0.3.31 — pickup save and world-transition resilience

## Verified source issue

The 0.3.30 `SkyPeepGame` called `save()` after each real coin/gem, which wrote all
route bests, every star set, both wallets, skins, trails, difficulty, settings,
and all six world progress counters before one synchronous `prefs.flush()`.
That work happened on the render thread on each pickup; storage errors
could propagate from `pause()`, `dispose()` or `updateGameStep()` as uncaught
Android exceptions. `begin()` selected the target level before loading new
artwork and allowed a texture exception to terminate the process.

## 0.3.31 changes

- `savePickup()` persists only coins, gems and `collected_world_N` for the active world. The flush still happens immediately on every actual pickup.
- A common guarded persistence path checks QA fixture isolation, performs one flush, logs storage exceptions and keeps in-memory progress for a subsequent retry. The full `save()` remains for results, settings, purchasing, pause and exit.
- The selected level and gameplay state change only after its textures stage successfully. A failed world load leaves the prior scene and saved selection intact.
- Regression tests preserve the existing world-key naming convention, range limits and wallet nonnegative rule.
- CI checks the release shell syntax, preventing an apostrophe in release notes from silently reaching the release job.

## QA and limitations

- Gate Gradle unit tests, dead-code audit, source art SHA-256, Android debug APK, unsigned release APK/AAB and offline manifest checks.
- Emulator must traverse all six worlds, pause/resume, shopping, Settings, UI sizes and Home/return. Verify installed builds do not lose local progress across a normal restart.
- The reduced number of preference mutations is source-verifiable, but frame time and real-device storage latency still require measurement; no claim of zero dropped frames or zero crashes.
- A storage failure cannot be considered a successful save; progress remains in RAM only until retry and may still be lost if the process terminates before storage recovers.
- Original artwork, source art hash, scoring, obstacle spacing, three lives, 10,001 physical-coin goal, permissions and identifier remain unchanged.
