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

## Sun Peak mask regression

The user's `gameplay(1).jpg` shows a sun, right-side temple and floating landscape behind the bird and stone pillars. The previous build's world-6 mask `Area(-20,418,618,774)` hid most of those landmarks, repainting them with synthetic sky. This revision narrows world-6 masks to the photographed right stone columns, their caps, the bird silhouette and leftward streaks. Source-pixel-coordinate invariants prevent selected sun/island/temple/waterfall pixels being erased while also confirming each known static object is covered. The exact post-reconstruction appearance is to be checked in the next emulator capture before claiming 1:1; world-5 and other remaining reconstructed areas are outside this specific fix.

## Source-matched sunset sampling

The actual packaged `bg_6.jpg` from CI #537 preserves the illustrated sun and temple, but shows yellow horizontal colour shifts due to generic artificial sky replacement. A local prototype sampled only warm orange/yellow sky pixels from the user-supplied gameplay image, using per-row medians and a 181-row triangular smoothing kernel. The Java world-6 reconstruction now uses that same deterministic source-matched field and keeps the original image outside the masks. World 3/4 blue sky behaviour remains unchanged. Final emulator evidence is still required.

## Canonical Sun Peak reference instead of pre-banded source

The APK from CI #539 still included horizontal stripe remnants after replacing colours: the old `bg_6` in the packed source was itself already partially repainted. The exact original Sun Peak gameplay illustration supplied by the user is available at `assets/previews/gameplay.jpg` (520×924). The deterministic JDK-only Gradle generator now takes this as a second tracked input, bicubic-upscales it to the required 706×1254 world canvas and removes its photographed static HUD, bird, streaks and pillars with the tuned masks and source-matched sky. The original reference JPEG is never overwritten or included as a duplicate app asset. This change directly protects 1:1 original scenery rather than repeatedly repairing the old corrupted image. Exact runtime parity still depends on emulator captures and dynamic overlay verification.
