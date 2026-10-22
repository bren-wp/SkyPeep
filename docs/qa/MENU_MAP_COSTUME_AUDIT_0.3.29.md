# SkyPeep 0.3.29 — navigation, map and costume transaction audit

## Grounded baseline

- Started from main merge commit `178df761724eaa60fe105f6b37fc61826f31f6ed` after PR #42 was merged and 0.3.28 was published.
- Kept original four design-reference illustrations under `assets/previews/`; no generated replacements.

## Source-verified defects and fixes

1. Menu LEVELS and SKINS used top 1155 + height 169 = bottom 1324 while SHOP and SETTINGS began at 1318. Their six-pixel collision stole taps from lower-row buttons. All menu actions now share a testable target resolver without row overlap.
2. Map selected-world status won over collected-world completion, hiding DONE from a selected completed world. DONE now takes precedence, while WORLD N and 10001 progress identify selection in the footer.
3. UI previously told players COMPLETE WORLDS FOR GEMS; no such reward exists in `finish(true)`. Corrected to COLLECT GEMS IN FLIGHT, matching actual gem pickups.
4. Bird skin purchases now validate stock id and balances and return immutable transaction results; owned skins can be equipped without a second deduction.
5. Updated Android `versionName`/`versionCode`, release branch gating, F-Droid published baseline, English/Croatian changelog and README.

## QA gates and limitations

- Run JVM tests (including menu seam, selected DONE status and costume purchase cases) and static dead-code audit.
- Android emulator must verify Menu, Levels, Skins, Shop, Settings, HUD, Pause, Game Over and Victory on all supported screen sizes.
- Confirm original art hash, offline merged manifests, ABI libs, APK/AAB validation and source metadata package.
- Physics, gate spacing, hearts, 3 lives, six open worlds and separately saved 10,001 coin goals were not modified.
- Passing tests do not prove physical-device compatibility or exact 1:1 pixel match; inspect emulator captures before making such a claim. Unsigned bundles need independent signing; official F-Droid listing is not established.
