# SkyPeep 0.3.36 — reconstructed-background seam audit

## Baseline

Based on main commit 557fcf74e9c3991c4701c06529ab0d1a6a2cac29 and published v0.3.35. The original four references and all 24 immutable artwork chunks remain unchanged. Their source archive must pass the existing fixed SHA-256 verification.

Issue #44 describes visible inpainting boundaries in real emulator gameplay. The issue remains open until manually reviewed; this code change is not a certification of perfect visual parity.

## Concrete correction

tools/RebuildWorlds.java formerly used a single row-wise RGB colour for the sky removed from original screenshots. This flattened horizontal lighting differences, showing artificial edges at feathered bird and pillar masks. It also filled missing rows only from previous values.

The source-derived reconstruction for Crystal Drop (world 3), Wind Gate (world 4) and Sun Peak (world 6) now:

- Excludes masked regions and photographed non-sky artwork from source sampling.
- Interpolates between valid sky rows in both directions, then smooths vertically.
- Blends locally sampled source-sky colour from a 32-pixel grid with bilinear interpolation and bounded colour deviation.
- Retains existing landmark, mask integrity, original-scene retention and fixed-object assertions.
- Changes no source illustration, gameplay physics, hitbox, original art hash, save key or wallet transaction.

Worlds 1, 2 and 5 retain their previous reconstruction; they still require the Issue #44 visual review.

## New test

Run: java -Djava.awt.headless=true tools/RebuildWorlds.java --self-test

The offline test uses a synthetic horizontal sky gradient to assert that reconstructed sky is spatial rather than flat and rejects abrupt adjacent-pixel colour steps. CI runs it before JVM and Android builds, then requires successful Android emulator smoke, six-world screenshot capture and visual comparison before preview release.

## Evidence and limitations

At PR creation time, the new CI result is pending. No test outcome or pixel-perfect parity is claimed in advance.

Real emulator screenshots must be inspected in all six worlds, on portrait and tablet viewports, both static and background-motion fixtures. Smooth interpolation alone cannot establish source-perfect reconstruction. Physical devices need separate GPU, lifecycle and cutout QA. Issue #44 must stay open until that evidence exists.
