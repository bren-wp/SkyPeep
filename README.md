<div align="center">

<img src="app/res/mipmap-xxxhdpi/ic_launcher.png" alt="SkyPeep app icon" width="112" />

# SkyPeep
### A little aviator. Six skies to conquer.

**An offline-first illustrated arcade adventure by [Brendigo](https://brendigo.com).**

[🎮 Explore the worlds](#six-worlds-to-explore) · [🧡 How to play](#how-to-play) · [⬇️ Install a preview](#install-an-android-preview) · [🛠️ Development](#for-developers)

![Original SkyPeep gameplay illustration — concept reference, not a live screenshot](assets/previews/gameplay.jpg)

</div>

## One tap. An extraordinary journey.

Guide a brave little aviator through floating islands, glittering crystals, moonlit skies and sun-soaked mountains. Thread between illustrated stone pillars, collect coins and gems, and chase your best score. No account, internet connection, advertisements or real-money purchases required.

### What's inside

| | |
|---|---|
| ❤️ **Three chances per flight** | Keep flying after a collision; collect occasional hearts to recover one missing life, up to three |
| 🪽 **One-tap flight** | Flap to rise, time your descent and navigate evolving openings |
| 🏔️ **Six open worlds** | Sky Start, Cloud Drift, Crystal Drop, Wind Gate, Moon Glow and Sun Peak — all immediately playable |
| 🌬️ **Three challenges** | Breeze, Explorer and Tempest, with independent best scores and precision stars |
| 💎 **Collect and customize** | Gather 10,001 physical coin pickups per world across flights; buy and equip bird outfits and flight trails |
| 📱 **Android-first presentation** | Responsive fullscreen gameplay, pause and tappable menus without scrolling |
| 🔋 **Battery-conscious** | Static illustrated screens redraw only when needed; flight remains responsive, with no motion-sensor polling |

**Illustrated controls:** The Levels map keeps the original world banners, stars and backgrounds unobstructed: small OPEN/PLAY/DONE chips replace only the five illustrated lock icons without covering island numbers; the first island has no lock. The selected world and cumulative pickup count appear in the lower toolbar. The SKINS and SHOP BUY/EQUIP buttons use bright green illustrated surfaces and large readable captions.

**Illustrated panels:** Shop and Settings now display the existing world scenery rather than the Main Menu's logo and buttons behind their themed UI. The four supplied visual references remain unmodified. Reconstructed live world backgrounds still need separate visual cleanup before claiming 1:1.

**Reference fidelity:** Unchanged wallet amounts retain their exact painted Menu/Skins reference pixels; dynamic overlays appear only when coins or gems actually differ from the corresponding source artwork.

**Performance and recovery:** Real coin/gem pickups now flush only the current world progress and wallet counters, not every saved setting and unlock; full saves remain for purchases, route results and settings. Storage errors and unavailable incoming world art are handled without terminating the current game.

**Durable progress:** Each collected coin and gem is flushed to offline local storage as it is picked up, so the latest real collection does not wait for a tenth coin.

**Menu and purchase safety:** Menu tap targets no longer overlap across adjacent illustrated rows; skin buying uses a tested wallet transaction and never charges an already-owned costume. On the map, a selected completed world shows DONE. Gems come from real in-flight pickups, not an automatic level-completion grant.

**Game balance:** Precision is assessed at the midpoint of each stone passage; a score is granted when the actual bird body fully clears it. Clearing pillars alone does not complete a world: each needs 10,001 actual coin pickups, saved across attempts. Consecutive openings are constrained to achievable changes. On a sky-edge rebound, collisions are immediately recalculated from the corrected bird position. A short recovery shield prevents one obstacle from consuming several lives. Hearts do not require payment and appear only if the bird has been hurt.

## See the original artwork

These are **design-reference images**, not captured gameplay screenshots. Run-time screenshots from Android emulator testing appear in [GitHub Actions artifacts](https://github.com/bren-wp/SkyPeep/actions/workflows/android.yml).

| Main menu | World selection |
|:---:|:---:|
| ![Original SkyPeep menu design](assets/previews/menu.jpg) | ![Original six-world map design](assets/previews/levels.jpg) |
| Bird costumes | Flight composition |
| ![Original costume selector design](assets/previews/skins.jpg) | ![Original gameplay concept design](assets/previews/gameplay.jpg) |

## Six worlds to explore

| Chapter | Atmosphere |
|---|---|
| ☁️ **01 · Sky Start** | Bright blue skies, drifting islands and waterfalls |
| 🏔️ **02 · Cloud Drift** | Airy peaks, clouds and sweeping elevation |
| 💠 **03 · Crystal Drop** | Ancient structures and luminous crystal formations |
| 🌪️ **04 · Wind Gate** | Windswept routes and dramatic stone obstacles |
| 🌙 **05 · Moon Glow** | Moonlit landscapes with luminous details |
| ☀️ **06 · Sun Peak** | Golden vistas and the final sky challenge |

Every world features its own original reconstructed background, themed stone pillars and score interface. Source imagery is preserved and verified during every CI build.

## How to play

1. **Tap to flap.** Release to descend under gravity and changing air currents.
2. **Fly through the middle of the gap.** Avoid stone and the edges of the visible sky.
3. **Gather coins, gems and hearts.** Hearts appear periodically while injured; each restores one missing life.
4. **Complete each world.** All six are already open. Collect 10,001 actual coins per world over as many attempts as needed; precision determines earned stars.
5. **Choose your flight license.** Breeze is gentler, Explorer is the classic balance and Tempest is more demanding.

Bonus coins from streaks or completing a world have been removed. Six separate pickup totals, wallet currency, results, stars, outfits, trails and settings are saved locally. Hearts and the three flight lives **reset at the beginning of each new run**.

## Style your aviator

Dress the bird as **Default, Captain, Forest, Cosmic or Royal** and fly with **Classic, Cyan Dash, Golden Spark or Moonlight** trails. Everything is earned through play with in-game currency, never real money.

## Install an Android preview

Visit **[SkyPeep GitHub Releases](https://github.com/bren-wp/SkyPeep/releases)** and choose the latest **successfully validated** preview. The attached `SkyPeep-X.Y.Z-debug.apk` is the installable **testing-only** build. Each verified 0.3.24+ release also includes `SkyPeep-X.Y.Z-release-unsigned.apk` (sign locally for direct APK distribution), `SkyPeep-X.Y.Z-play-upload-unsigned.aab` (sign with the existing Google Play upload key), SHA-256 checksums, MIT license, F-Droid build metadata and signing instructions.

> **Important:** The release APK and Play AAB are deliberately **unsigned**, and F-Droid builds/signs its own APK from source. None of the unsigned files is installable as-is or immediately eligible for store upload. Follow the [signing and F-Droid guide](docs/ANDROID_DISTRIBUTION.md). The newest code on a development branch may be awaiting CI even when an older public preview is available.

Minimum Android SDK is API 23. The application is local-only with no requested Internet, location or contacts permissions. Scores can be optionally shared using the Android chooser.

## For developers

| Stack | Quality gates |
|---|---|
| Kotlin + LibGDX 1.14.0 | Kotlin JVM regression tests and Android API 35 emulator smoke |
| Android compile/target SDK 36 | Offline merged-manifest permission check |
| Java 17 + Gradle | 24-part original-art SHA-256 verification and native library checks |
| Portrait reference 941 × 1672 | Five physical emulator viewport sizes and per-screen screenshots |

```bash
python3 tools/dead_code_audit.py
gradle :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease
```

The audit and tests are meaningful checks, **not a guarantee of zero bugs, complete pixel-for-pixel parity or physical-device GPU stability**.

[Version history and release status](docs/RELEASE_HISTORY.md) · [Performance audit](docs/qa/PERFORMANCE_0.3.25.md) · [Contact and background QA](docs/qa/CONTACT_RENDER_0.3.26.md) · [Visual parity audit](docs/VISUAL_PARITY_AUDIT.md) · [Contact and responsive QA](docs/CONTACT_AND_RESPONSIVE_QA.md) · [Dead-code audit](docs/DEAD_CODE_AUDIT.md)

---

<div align="center">

**Built for curious flyers by [Brendigo](https://brendigo.com).**

[GitHub repository](https://github.com/bren-wp/SkyPeep) · [Releases](https://github.com/bren-wp/SkyPeep/releases) · [LICENSE](LICENSE)

</div>
