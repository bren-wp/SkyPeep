# SkyPeep — release history and development status

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
| **0.3.26** | Fixed rebound collision geometry and optimized pickup checks and background crop allocations | **In development — preview only after full CI passes** |

## Versioning and downloads

Android `versionCode` increases monotonically (0.3.25 = 37; planned 0.3.26 = 38). Every successfully validated release beginning with 0.3.24 attaches **three distinct binary artifacts**: testing-only debug APK, unsigned release APK for local signing, and an unsigned Play-upload AAB to sign using the existing upload key. It also includes SHA-256 checksums, F-Droid source-build metadata, license and signed-distribution instructions. No key is uploaded to GitHub. See [Android distribution and F-Droid preparation](ANDROID_DISTRIBUTION.md).

Keep existing published GitHub Releases; do not overwrite or delete them while preparing later versions. Do not treat preview artwork as genuine app screenshots.

## Outstanding quality gates

- Full visual review for every screen/world vs original references, particularly Moon Glow and Sun Peak backgrounds
- Real-device memory/GPU lifecycle and previously reported user-device crash stack trace
- Foldable/cutout and tablet interaction QA outside the emulator viewport matrix
- Release keystore signing and Play validation

[README](../README.md) · [Android workflow](https://github.com/bren-wp/SkyPeep/actions/workflows/android.yml)
