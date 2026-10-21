# SkyPeep 0.3.26 — physics contact, collectibles and visual smoothness

## Runtime correctness

**Defect addressed:** When the bird struck the top/bottom visible edge, SkyPeep clamped and bounced `birdY`, but collision checks later in that *same physics substep* still referenced the pre-rebound `SkyPeepContact.Body`. Thus an obstacle could be reported as hit despite the rendered bird having already moved inside, and a legitimate heart/coin could be missed. Gameplay now rebuilds the body ellipse immediately after recovering to the painted sky. A pure `SkyPeepWorldBounds` module validates the bounce bounds and speed, enabling unit tests across all world/difficulty combinations and common Android display sizes.

**No gameplay-price or difficulty changes:** Three lives, brief hit protection, collectible heart intervals, six original world images, saved preferences, prices, route rewards and vertical velocity constants remain unchanged.

## Frame allocation and performance

The original `SkyPeepBackgroundMotion.crop` produced a new `Crop` data class on *every* gameplay rendering frame, even when the scene's source coordinates barely changed. The GL drawing path now reuses one `MutableCrop` buffer and writes to it through `cropInto`. A pure `crop` function remains for easy unit testing; regression tests assert byte-for-byte identical source crop coordinates for hundreds of frames across seven source widths, invalid-time guards and paused-to-playing transitions.

`SkyPeepPickup.overlaps` adds mathematically conservative disk-in-ellipse and ellipse-in-disk short circuits: if a pickup circle reaches the ellipse's inscribed disk, it genuinely overlaps; if it is beyond the circumscribed disk, it certainly does not. Other ambiguous cases retain the original exact 40-step closest-point bisection. Coin/gem/heart radii and silhouette contact tolerances have **not** been enlarged.

## Tests / release criteria

- JVM unit tests assert sky rebound moves contact inside the visible frame on 360×640, 720×1280, 1080×2400, 1600×2000 and for all modes/worlds.
- A regression reproduces the previous stale-body false stone collision, verifying the corrected body passes through a legitimate gate opening.
- Collectible contact tests sample diagonal/cardinal positions and all three pickup radii.
- The original original-art SHA-256 verification, native libGDX libraries for four ABIs, static dead-code audit, offline Android manifests, gameplay physics checks and complete emulator screen/responsive suite are unchanged.
- Preview release is gated on actual Android build, JVM tests, emulator QA and availability of debug APK, unsigned direct-install distribution APK, unsigned Play-upload AAB, F-Droid recipe and SHA-256.

## Not certified

Exact visual pixel parity for all illustration references, physical-device GPU measurement, prior user-device crash reproduction, Play Console signing and official F-Droid inclusion remain independent quality and distribution work. A development branch/PR is **not** a published release, and unsigned binaries cannot be uploaded unchanged to Google Play.
