# SkyPeep 0.3.33 — panel world-art lifecycle and recovery

## Source-verified defect

`clickMap` updates the selected `level` and saves it without loading the
new world graphics. This is intentional, so navigating the illustrated map
does not allocate large JPG textures. However Shop and Settings (and their
full-bleed overscan) previously attempted `image("bg_$level")` directly.
The selected texture did not yet exist until `begin(level)` was called,
leading to empty panels in a valid MAP → MENU → SHOP/SETTINGS path.

## Implemented

- Stage selected-world textures on opening Shop or Settings if necessary.
- Keep the prior scene usable when `loadLevelArt` rejects a corrupt or missing
  resource, and display a non-crashing on-screen warning.
- Render both the center canvas and full-bleed edges from the actual loaded
  world instead of the still-unloaded selected-world identifier.
- Pure JVM tests cover all six worlds, unchanged selection, switched
  selection, failed-load fallback, and invalid indices.
- Update 0.3.33/versionCode 45, bilingual changelogs and preview packaging.

## Invariants and verification

- All six worlds stay selectable and use the original four illustrations.
- No gameplay physics, collision geometry, sound, 10,001 pickup goals,
  three lives, wallet balances, preferences keys or application ID changed.
- CI must pass JVM tests, dead-code check, release Bash syntax, Android
  APK/AAB artifact validation and emulator navigation across viewports.
- Emulator/manual route should explicitly navigate MAP → another island →
  MENU → SHOP and SETTINGS → BACK → PLAY.
- Pixel-for-pixel appearance on all physical devices and the previously
  user-reported runtime crash still require independent evidence.
