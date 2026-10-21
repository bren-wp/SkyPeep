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
| **0.3.24** | Three per-flight lives, timed recovery hearts, compact HUD, resource-transition and documentation polish | **In development — not published until all CI checks pass** |

## Versioning and downloads

Android `versionCode` increases monotonically (0.3.23 = 35; planned 0.3.24 = 36). Each successful trusted preview run attaches an installable debug APK and an **unsigned** release AAB. Signing keys are never stored in the repository. The AAB is not a Play Store deployment artifact until separately signed and validated.

Keep existing published GitHub Releases; do not overwrite or delete them while preparing later versions. Do not treat preview artwork as genuine app screenshots.

## Outstanding quality gates

- Full visual review for every screen/world vs original references, particularly Moon Glow and Sun Peak backgrounds
- Real-device memory/GPU lifecycle and previously reported user-device crash stack trace
- Foldable/cutout and tablet interaction QA outside the emulator viewport matrix
- Release keystore signing and Play validation

[README](../README.md) · [Android workflow](https://github.com/bren-wp/SkyPeep/actions/workflows/android.yml)
