# SkyPeep 0.3.39 — screen-by-screen visual and interaction audit

## Evidence and comparisons

The four user-supplied original images are the existing `assets/previews/{menu,levels,skins,gameplay}.jpg`; they remain unchanged. The actual previously successful Android API 35 run #772 (`40799409805`) provides 16 main-screen/world PNGs and six-resolution screenshots. Its comparator measures RGB mean absolute error after aspect-fit to the artwork:

| Source reference | Correct emulator capture | Previous RGB MAE /255 |
|:--|:--|--:|
| Main Menu | menu.png | 4.92 |
| Levels / World Map | levels.png | 7.05 |
| Skins | skins.png | 5.14 |
| Sun Peak Gameplay | world-6.png | 30.82 |

This is a *baseline*, **not** 0.3.39 test data. Gameplay screenshots differ dynamically in pickups, bird, HUD and pillars. Do not compare a different world's painting to Sun Peak. Pixel-exact parity cannot be claimed while some original assets for worlds 1–5 contain baked and occluded/blurred scenery (Issue #44).

## Direct visual findings from genuine screenshots in successful run #772

| Screen | Evidence / defect | Changes and acceptance target |
|:--|:--|:--|
| Splash | Original SkyPeep branding is correctly loaded; no reference-specific splash painting | Preserve authentic bird, full-screen background and no extra taps |
| Main Menu | Source scene already close; wallet amounts can change during play | Preserve original illustrated buttons; dynamic labels constrained inside wallet faces |
| Levels | Original paths/islands mostly preserved; world 2 text is dynamically corrected | Preserve island art/locks/stars and hitbox-to-illustrated button parity |
| Gameplay 1–6 | Source backgrounds still contain reconstruction bands (Issue #44); different score lengths can overflow the winged badge | Restrict live score to badge and world label to separate ribbon; no old TAP TO FLY banner; keep six-world physics and collectible progress |
| Pause | Captured with actual RESUME/RESTART/HOME and MUSIC/SFX | Keep touch gutter separation and three lives |
| Game Over | Dynamic scores, coins and progress compete for limited card space | Clamp glyphs to illustrated cards and guarantee retry/home/share alignment |
| Victory | The screenshot shows headline/score labels overlapping underlying panels | Correct LibGDX text baseline direction and test independent result panels |
| Skins | Real equipped/default state and four BUY actions captured | Keep values, prices and captions inside the artwork; non-button birds remain inert |
| Shop | Screenshot showed FLIGHT TRAILS heading BELOW its wood header, and COINS/GEMS labels overlapping lower panel borders | Correct glyph-top-origin vertical containment; test long economy figures and purchased state |
| Settings | Screenshot showed SETTINGS below the wood header, and label margins inconsistent | Correct same font baseline error; preserve real MUSIC/SFX/VIBRATION/DIFFICULTY actions |

### Root cause: text positioned relative to the wrong glyph edge

The legacy `SkyPeepTextBounds.position` assumed LibGDX BitmapFont letters extended **upwards** from `BitmapFont.draw(..., y)`. The actual glyph layout extends downwards. Consequently a height-limited title could be clamped to a smaller y and pushed further **below** the header. This is visible on the real CI #772 screenshot, independently of source-art inpainting.

In 0.3.39 the positioning guard now requires `textBottom = y - glyphHeight` to remain within its rectangle while `textTop = y` stays below the top edge. Tests cover Shop/Settings wood headers, dynamic Victory/loss values, wallet balances, Skin card captions, Game Over and one-to-ten-digit winged score.

### Mandatory gate before preview release

1. Source artwork SHA-256 integrity and dead-code audit.
2. JVM tests (`SkyPeepTextBoundsTest` and other existing suites), native libraries, Gradle APK/AAB packaging and signing state verification.
3. API 35 emulator navigation through the ten principal screens, all six gameplay worlds, wallet/ownership/settings, pause/resume and six viewports.
4. `CompareSkyPeepScreens.java` actual-reference output including Sun Peak HUD ROIs. Inspect real Shop/Settings/Victory screenshots for residual overlapping text.
5. Upload **actual** screenshot PNGs to a versioned GitHub Release only if all gates pass; the README gallery must never show reference mockups as emulator output.
6. Release APK and AAB stay explicitly **unsigned**; the test APK uses the debug certificate. Include SHA-256, signature instructions and F-Droid metadata.

## Limits

The 0.3.39 text baseline fix does not recreate original layers obscured by bird/pillars/HUD in world backgrounds 1–5. Issue #44 remains open. Passing automated tests is not certification of 1:1 visual identity or 60/90/120Hz performance on physical Android devices.
