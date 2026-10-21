# SkyPeep 0.3.28 — screen-by-screen Android screenshot review

## Evidence and exact baseline

Source design references are the four user-provided illustrations in assets/previews/menu.jpg, levels.jpg, skins.jpg and gameplay.jpg. Reviewed actual 1080x1920 emulator screenshots taken from the fully green 0.3.27 CI run #508 (SkyPeep-Emulator-Smoke-508 artifact). The prior reference-vs-emulator report measured menu MAE 5.45/255, levels MAE 11.61/255 and skins MAE 8.99/255 (not to be mistaken for literal pixel parity).

## Confirmed issues and actual fixes in this revision

| Screen | Confirmed emulator defect | 0.3.28 change |
|---|---|---|
| Levels | Six 0 / 10001 parchment overlays intersected the painted island names, flag banners and stars; extra SELECTED board obscured the starter island | One small OPEN/SELECTED/DONE chip replaces old baked lock for each island, and the progress for the currently selected world moves to a single lower-toolbar label |
| Levels / world 2 | Dynamic game text said CLOUD DRIFT, but immutable original map JPEG still visibly said CLOUD PASS | Cover only outdated text on the existing island parchment with live CLOUD DRIFT label, leaving world art, marker, number and stars intact |
| Skins | App overpainted the original DEFAULT plaque with another DEFAULT label and generated brown BUY buttons/text that were difficult to distinguish from the source green controls | Keep DEFAULT pixels unchanged until another skin is selected, restore green button faces and high-contrast white BUY/EQUIP/EQUIPPED captions inside exactly the original touch rectangles |
| Shop | Brown-on-brown BUY captions were not sufficiently distinguishable | Use the existing illustrated green NinePatch and white captions, preserving offer prices and owned/equipped handling |
| Settings | Dark brown lettering on dark wood was hard to read, particularly the route difficulty value | Render bright high-contrast text without moving the Settings row hitboxes |
| Menu | Close to reference, avoid changing layout without evidence | Keep main menu styling and button geometry unchanged |
| Gameplay HUD / Pause / Victory | Existing validated functionality and gameplay balance retained | No physics/controls/pickup/hitbox change in this revision |

## Validation criteria

- JVM tests for compact world status labels and the separate selected-world coin footer.
- Run Android API 35 emulator across six worlds and all accessible illustrated pages. Test SKINS purchase/equip click regions, Shop, Settings, Home/resume and five viewport sizes.
- Require artwork reconstruction SHA-256, merged offline manifest, 4 JNI ABIs, Gradle Kotlin/unit tests, full dead-code audit and three release binaries.
- Enforce conservative mean absolute RGB-error budgets for unchanged Main Menu, Levels and Skins against provided references, while preserving complete screenshots for manual human comparison. An automated MAE threshold can miss local overlay mistakes and is not a certification of 1:1 equivalence.
- Public GitHub release only when the complete pipeline passes. Debug APK is for testing; unsigned release APK and AAB require external signing. F-Droid metadata does not mean official submission/acceptance.

## Limits

Other pages (Pause, Shop, Settings and Victory) do not have matching original user-provided reference illustrations in this batch, so those were corrected against their actual emulator screenshots and existing game art, not falsely represented as pixel-matched. Device-specific GPU failures and battery/power measurements remain unverified without hardware evidence.
