# SkyPeep 0.3.21 — full-source dead-code and runtime reachability audit

## Production source and regression tests

Last complete audit on the 0.3.21 branch: **30 Kotlin/Java production files**, **0 confidently unused symbols**, **0 unreferenced production methods**, and **1 intentional QA-only helper** (`SkyPeepBirdLayout.aspectError`, referenced by the aspect-ratio test). Existing dynamic texture names and Android framework entry points remain protected.

CI runs `python3 tools/dead_code_audit.py` on **all production Kotlin files and Java asset tooling**. It fails on confidently unused imports and private symbols. A new second pass compares production method definitions against **all production Kotlin sources and all JVM regression tests**; QA-only methods and externally unreferenced methods are reported for manual classification, rather than being silently deleted.

**Confirmed obsolete implementation removed in 0.3.21:** `FlightRules.intersectsObstacle` (legacy rectangle collision) and `FlightRules.overlapsPickup` (oversized ellipse/halo model). They had no production call sites after migrating to `SkyPeepContact` and `SkyPeepPickup`; their old-only unit cases have been removed. The current ellipse-to-stone and body-to-collectible regression tests remain.

Existing code continues to have several distinct, actively used drawing and logic components. The central `SkyPeepGame.kt` is still a large controller; a wholesale extraction would require more end-to-end screenshot evidence and is not warranted as an unaudited bulk deletion.

## Resources, security and compatibility

- Six rebuilt world images, their obstacle art, themed HUD, three bird poses, currencies, fonts and sound files are checked by Android CI before artifact publishing.
- Source artwork is restored from 24 fixed chunks and compared against an immutable SHA-256 before reconstruction.
- Packaged native `libgdx.so` is checked for four Android ABIs.
- Source and effective merged Android manifest permissions are verified: no network, location, microphone, camera or contact permissions and no cleartext traffic or Android backup.
- Procedural costume/UI/flight textures are managed and explicitly disposed; real Android Home/Resume emulator navigation is checked.
- Existing local progression keys, world unlocks, currency and cosmetics remain compatible across v0.3.20 → v0.3.21.

## Gameplay, performance and UI changes

- Gate precision credit is fixed at the obstacle's mid-crossing rather than a subsequent trailing-edge moment, making progress/stars independent from flaps after a successful passage.
- Spawning carries its fractional physics remainder into the next countdown, preventing frame-rate-dependent drift.
- Invalid graphics frame deltas are bounded using the existing validated physics helper.
- Score/HUD/trail colour palettes and title strings are allocated once rather than repeatedly in the hot draw path.
- Blank menu regions remain silent; actionable buttons still play sound and keep the original illustrated hit regions.
- Music playback transitions no longer throw fatal exceptions if Android audio becomes temporarily unavailable.

## Audit limitations

This is a **whole-codebase static pass plus integration checks**, not a formal proof that every path is reachable. Kotlin/JVM reflection, Android lifecycle callbacks, dynamically resolved texture names, native libraries, visual opacity masks, third-party bytecode and real-device GPU drivers cannot be exhaustively classified by static symbol counting. Thus unresolved candidates are review items, not automatic deletion targets.

**Not yet certified:** per-pixel 1:1 parity against every reference screen, actual user-device crash reproduction, GPU/memory profiling on physical devices, foldable cutouts and a signed Play-ready AAB. Do not label an unsigned bundle Play-ready.
