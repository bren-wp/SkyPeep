# SkyPeep 0.3.24 — arcade gameplay and UI audit

## Implemented
- Three lives per flight, protected collision recovery and no unintended persistent loss of coins, stars or unlocked worlds
- Spawn one recovery heart at bounded intervals when injured, collectible through the same ellipse-circle contact calculation as coins and gems
- Dedicated managed GPU heart art built procedurally once and explicitly disposed during application teardown; no third-party art or replacement of original source sprites
- Compact numeric HUD plates and three readable life indicators; pause button and original touch geometry unchanged
- Retained six original world themes and separately illustrated pillar cap PNGs; their vertical shafts still extend to the display edges
- Atomic texture transitions are addressed independently from scoring and physics; the underlying scene-switch hitch must be verified by emulator/physical-device frame recordings, not asserted eliminated by build success

## Test strategy
`SkyPeepLivesTest` checks damage, invulnerability, recovery caps and timed heart spawn gates. Existing `SkyPeepPickupTest` checks torso-circle pickup alignment. Android emulator integration must cover all screens, six world backgrounds, navigation, changed HUD display, cold start and Home/resume. Android builds verify 24 original art chunks SHA-256, permission manifest, JUnit, dead-code audit and native ABIs.

## Theme not yet implemented
A squirrel and acorns require a proper dedicated animated sprite sheet, collection icon and associated hitbox/world integration. The project currently contains the original aviator and coin/gem/heart pickups only. A squirrel mode must not be claimed or substituted using a generic bird tint.

## Remaining constraints
Google Play AAB signing, physical-device crash reproduction, professional texture/frame profiling and full 1:1 visual parity are still pending. Original design-reference screenshots remain explicitly identified as concepts, not emulator screenshots.

## Screenshot contrast regression

The old comparator sampled four pixels from the previous, larger score face and demanded a white background. The compact 0.3.24 Wind Gate HUD legitimately includes dark illustrated stone/sky. The revised test samples the actual compact score region, checks luminance contrast instead of forcing the original white-pixel coordinates, and brightens Wind Gate's score ink to pale cyan for readable numerals while retaining its dark themed background. The screenshot check remains a required CI step.
