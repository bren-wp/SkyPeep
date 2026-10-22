# SkyPeep 0.3.30 — illustrated Shop/Settings and reliable pickup persistence

## Evidence and source defects

The Android #523/#524 emulator runs (five responsive sizes) show Menu, Map, Skins, Shop, Settings, gameplay, pause and results. Shop and Settings use screen_menu.jpg as a backdrop, then add semi-transparent panels: this re-exposes unrelated menu buttons under the actual interface. Coin pickup persistence had been batched after ten coins, and gem pickups were not immediately saved.

## Change

- Render original illustrated `bg_$level` behind dedicated Shop and Settings panels; don't reload screen_menu.jpg for those states.
- Flush wallet and world-goal progress on each real coin pickup; the final coin still uses finish(true), which saves.
- Flush gems on each real gem pickup. Real pickup frequency is gate-bound, not frame-bound.
- Keep all source art and player settings/ownership keys unmodified. Physics, contacts, lives, rewards, six worlds, 10001 goal, stars and skin catalogue remain unchanged.
- Update Kotlin JVM asset-policy tests, release metadata and versionCode 42.

## Validation requirements and limitations

Verify Gradle/JVM/dead-code checks, reconstructed art SHA-256, offline merged manifests, four native ABI libraries, APK/AAB packages and Android emulator screenshot/navigation. Inspect Shop/Settings at 360x640, 720x1280, 1080x2400, 1200x1920, 1600x2000 and confirm Back works. Device-specific performance/storage wear from each coin flush has not been benchmarked. Live gameplay reconstruction artifacts (including obvious flat-color transitions in Sun Peak) remain unresolved; no unsupported 1:1 claim. Store signing and F-Droid acceptance remain separate.

## Reference-exact wallet plates

User's source `menu.jpg` paints 1250 coins / 75 gems; `skins.jpg` paints 1250 coins / 89 gems. Android previously painted a replacement badge over these numbers unconditionally, causing avoidable pixel drift. `SkyPeepReferenceWallet` now skips a specific overlay if the respective saved value equals the illustration, while still updating other amounts independently. JVM tests cover reference values, changed values and the difference between Menu versus Skins sample gem totals. No static placeholder is used to represent a changed wallet.
