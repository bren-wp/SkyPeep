# SkyPeep 0.3.34 — artwork-faithful Shop and Settings overscan

## Source review
The 0.3.33 panel navigation fix staged the correct selected-world JPG for SHOP/SETTINGS.
However `drawFullBleedEdges` still treated both as UI screenshots, sampling only
12 outer source pixels on wide/tall displays. These panels actually use `bg_$level`,
not `screen_menu`, so narrow sampling creates large uniform side/top strips and
visible boundaries when expanded to a non-reference device ratio.

## Change
- New pure `SkyPeepFullBleed.usesScenery` classifies PLAY, SHOP, SETTINGS and
  winning RESULT as background-world scenery.
- Shop and Settings now sample broader (up to 128px) illustrated source
  regions at wide/tall viewport margins; their existing shade extends into margins.
- Menu, Map, Skins, Pause, Splash and losing RESULT retain narrow UI edge
  sampling to avoid replicating baked buttons, currency icons or lettering.
- Added JVM assertions for every screen classification and representative
  360x640, 720x1280, 1080x2400, 1200x1920, 1600x2000, 2048x1536 sizes.

## Scope and limitations
No generated artwork, replacement assets, new permissions, physics changes,
hitbox modifications, wallet/memory-key migrations or reward adjustments.
The original 941x1672 authored canvas is unchanged, and tests confirm only
the enlarged margins use the updated sampling policy.
CI must verify Kotlin/JVM tests, dead-code audit, Bash syntax, artwork SHA-256,
offline manifest and ABI checks, APK/AAB packaging, Android 35 emulator,
responsive captures and a GitHub preview before merge. Exact physical-device
1:1 parity still requires human inspection of captured devices.
