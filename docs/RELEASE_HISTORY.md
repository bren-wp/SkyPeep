# SkyPeep — release history and development status

## SkyPeep 0.3.38 (candidate — CI pending)

- Corrected optional Android vibration permission and added explicit source/merged-manifest CI gate.
- Prevented a partial successful flush from clearing a previous failed save; a full progress snapshot must first recover. BUY/EQUIP operations cannot proceed while storage is unsafe.
- Added storage recovery regression tests and expanded real screenshot comparison with three per-screen regional MAE measurements.
- Original artwork, saved keys, purchases, HUD composition and flight physics preserved; Issue #44 awaits human review.
- 16 real emulator screenshots remain in README and, after QA approval, in the versioned release. **Candidate only; do not assume tests have passed.**

## SkyPeep 0.3.37 (published, validated)


- Safeguard existing offline progress after saved preference read errors; avoid replacing recorded purchases or wallet values with startup fallbacks.
- Source-match Sky Start and Cloud Drift reconstructed background colours with original illustration lighting; follow-up visual comparison required for open Issue #44.
- Raise minimum Android SDK to 24 for Google Play automatic protection; target SDK stays 36, versionCode becomes 49.
- Public local-data privacy notice and a genuine Android emulator screenshot gallery featuring ten screens and six worlds, published only after successful CI.
- Original source artwork, physical flight balance and saved-data format remain unchanged.

## SkyPeep 0.3.36 (published, validated)

- [v0.3.36-preview.595](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.36-preview.595) passed CI build and six-world API 35 emulator navigation.
- Two-dimensional, original-source sky reconstruction implemented for worlds 3, 4 and 6, with interpolation regression tests and signed-state-verified release packaging.
- PR #51 merged into main. Real image metrics still showed gameplay deviations; screenshot QA is not a blanket 1:1 certification.

SkyPeep preserves its original Brendigo artwork and locally saved data through 0.3.x. The GitHub release page is authoritative for what users can install; source commits and draft pull requests are **not** releases.

| Version | Key verified development focus | Status |
|---|---|---|
| [0.3.18](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.18-preview.434) | Edge-to-edge viewport, HUD anchoring and pause hit areas | Published preview |
| [0.3.19](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.19-preview.443) | Fair coin/gem pickups, bounded pillar transitions and fixture saving | Published preview |
| [0.3.20](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.20-preview.448) | Managed procedural GPU textures and Android Home/resume regression | Published preview |
| [0.3.21](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.21-preview.462) | Midpoint precision, stable spawn timing, UI memory and dead-code audit | Published preview |
| [0.3.22](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.22-preview.465) | Physics-clock wind sampling and overlap-aware illustrated map touches | Published preview |
| [0.3.23](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.23-preview.470) | Torso-clearance scoring, numerical input guards and isolated debug QA saves | Published preview |
| [0.3.24](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.24-preview.485) | Three per-flight lives, timed recovery hearts, compact HUD, resource-transition and multi-store distribution files | Published preview |
| [0.3.25](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.25-preview.489) | Static-screen GPU idle rendering, sensor power savings, HUD/text allocation reduction and UI artwork extraction | Published preview |
| [0.3.26](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.26-preview.492) | Fixed rebound collision geometry and optimized pickup checks and background crop allocations | Published preview |
| [0.3.27](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.27-preview.508) | Six open worlds and 10,001 actual coin pickups each, without completion or streak currency bonuses | Published preview |
| [0.3.28](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.28-preview.511) | Screenshot-reviewed map, readable skin/shop buttons, Cloud Drift and Settings | Published preview; PR #42 merged; CI #513 succeeded |
| [0.3.29](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.29-preview.523) | Menu touch precision, DONE status, wallet transaction checks, victory contrast | Published preview; PR #43 merged; CI #523/#524 passed |
| [0.3.30](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.30-preview.543) | Illustrated Shop/Settings, reference wallet values, Sun Peak reconstruction and per-pickup saves | Published preview; PR #45 merged; CI #543/#544 successful |
| [0.3.31](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.31-preview.551) | Reduced pickup saves, guarded I/O, safe failed world transitions and release-shell regression | Published preview; PR #46 merged and CI #551/#552/#553 passed |
| [0.3.32](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.32-preview.559) | Safe per-key preferences reads, mid-flight record durability, share-sheet error recovery | Published preview; PR #47 merged; #559/#560/#561 succeeded |
| [0.3.33](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.33-preview.565) | Shop/Settings selected-island scene loading, emulator route QA | Published preview; PR #48 merged; CI #565/#566/#567 successful |
| [0.3.34](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.34-preview.569) | Landscape edge sampling for Shop/Settings on tall and wide devices | Published; PR #49 merged; CI #569–571 successful |
| [0.3.35](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.35-preview.579) | Branded app icon, adaptive launcher, accurate Skins EQUIP control | Published preview; PR #50 merged |\n| [0.3.36](https://github.com/bren-wp/SkyPeep/releases/tag/v0.3.36-preview.595) | Original-sky 2D reconstruction, image QA and responsive emulator tests | Published preview; PR #51 merged; CI #595 passed |

## Versioning and downloads

Android `versionCode` increases monotonically (0.3.33 = 45; 0.3.34 = 46; 0.3.35 = 47; 0.3.36 = 48; 0.3.37 candidate = 49). Every successfully validated release beginning with 0.3.24 attaches **three distinct binary artifacts**: testing-only debug APK, unsigned release APK for local signing, and an unsigned Play-upload AAB to sign using the existing upload key. It also includes SHA-256 checksums, F-Droid source-build metadata, license and signed-distribution instructions. No key is uploaded to GitHub. See [Android distribution and F-Droid preparation](ANDROID_DISTRIBUTION.md).

Keep existing published GitHub Releases; do not overwrite or delete them while preparing later versions. Do not treat preview artwork as genuine app screenshots.

## Outstanding quality gates

- Full visual review for every screen/world vs original references, particularly Moon Glow and Sun Peak backgrounds
- Real-device memory/GPU lifecycle and previously reported user-device crash stack trace
- Foldable/cutout and tablet interaction QA outside the emulator viewport matrix
- Release keystore signing and Play validation

[README](../README.md) · [Android workflow](https://github.com/bren-wp/SkyPeep/actions/workflows/android.yml)
