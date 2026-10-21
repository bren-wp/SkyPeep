# SkyPeep 0.3.23 — fairness, QA save integrity and numerical stability

## Verified source defects addressed

1. **Delayed scores:** the former condition `gate.x + width < BIRD_X + 40` credited a gate significantly later than the actual solid-body clearance. `SkyPeepGatePass.hasClearedBody` now uses the torso ellipse's left bound and collision grace, while maintaining precision sampling at gate midpoint.
2. **Debug fixture save integrity:** the former `shouldPersist(world, mapUnlocked)` ignored special screenshot Intent inputs such as `qaDifficulty`, and could write temporary test selections into real preferences. Every one of the nine synthetic fixture inputs now participates in the save guard. Ordinary non-QA gameplay persists normally.
3. **Invalid times:** background cropping with `NaN` animation time could fail during integer conversion; invalid gust time could produce non-finite acceleration. The background crop sanitizes elapsed time, and the wind model returns calm acceleration on non-finite input.

## Testing

JVM regressions verify exact gate-clearance thresholds and invalid values, independently activate each debug fixture flag, and check invalid animation/gust times in all six worlds and three difficulty modes. Existing build gates continue to verify artwork reconstruction with SHA-256, code reachability audit, four JNI ABIs, offline-only manifest permissions, Android navigation/screenshots and five responsive resolutions.

## Boundaries of this release

The change does not alter original image files, screen positions, saved progress keys, in-game virtual currency, skin or trail pricing, or the three difficulty multipliers. It does not certify exact per-pixel 1:1 visual parity or resolve hardware-specific crashes without their stack traces. Release AAB remains unsigned and is NOT Google Play ready.
