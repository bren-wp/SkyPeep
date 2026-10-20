# SkyPeep 0.3.21 — Contact fairness and full-screen Android QA

## Collision and gameplay geometry

The original authored canvas is 941 × 1672 virtual coordinates. `SkyPeepBirdLayout` paints the original animated bird at a 295 × 232 nominal box while `SkyPeepContact` uses a smaller **47 × 44 radius torso/head ellipse**, anchored at 59.5% of the width and 46.5% of the height. Decorative wing, beak, scarf and anti-alias margins are excluded. An additional 5 virtual-pixel contact grace and 36-pixel obstacle cap side inset prevent invisible near-miss deaths without disabling actual solid-stone contact.

Coins and gems use `SkyPeepPickup`: exact ellipse-to-circle collision with the **rendered** sprite centres and radii (coin 37, gem 41), rather than the superseded large invisible pickup ellipses. The old `FlightRules.intersectsObstacle` and `FlightRules.overlapsPickup` helpers and their obsolete test cases were removed as dead production code in v0.3.21.

The bounded frame delta is at most 50 ms; gameplay integrates the capped delta in physics steps of at most 1/120 second. Invalid renderer deltas are rejected. Gate spawn timing carries fractional overrun, so obstacle periods do not vary systematically by frame rate. `SkyPeepGatePlacement` limits consecutive opening jumps for each of the six worlds and three difficulties.

**New in 0.3.21:** A precision passage is evaluated when the gate midpoint crosses the bird body centre, not when the trailing edge passes behind it. The result remains fixed even if the bird flaps after safely clearing the centre.

## Full-screen illustration, HUD and UI

`ExtendViewport` preserves reference-screen character art and button geometry; illustrated background margins fill extra Android viewport space. Score remains centred relative to the original canvas, while coin, gem and pause HUD plates remain anchored to the displayed world edges. Pause touch geometry is matched to the visible art.

Settings and Shop use the project's branded illustrated button palette and mascot, not placeholder stock images. Menu, map and skins hit regions activate only real on-screen controls; empty backgrounds no longer produce misleading click audio. Optional Android audio playback failure cannot terminate a flight.

## Emulator coverage

GitHub CI builds debug APK and unsigned AAB, validates the static dead-code audit, resource-art SHA-256, native ABIs and merged manifest, and then runs on an Android API 35 emulator. It exercises splash/menu, world map, skins and purchases, shop, settings, all six world scenes, pause/resume, result screens, score displays, skin customization and saved settings after restart.

Responsive device-size checks run for **360×640, 720×1280, 1080×2400, 1200×1920 and 1600×2000**. Actual screenshots and logcat traces are stored as `SkyPeep-Emulator-Smoke-*` workflow artifacts; `tools/ResponsiveViewportQa.java` checks screen continuity and the original canvas alignment.

## Remaining limits

Pixel-perfect 1:1 parity for every visual reference, original alpha-mapped pillar collision, large foldable safe areas, physical-device EGL/memory pressure, and a signed Google Play release remain to be verified separately. A green emulator build cannot certify all Android GPUs or reproduce a user-device crash without that device's stack trace.
