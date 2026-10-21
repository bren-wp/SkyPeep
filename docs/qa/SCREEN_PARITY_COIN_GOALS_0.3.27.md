# SkyPeep 0.3.27 — visual reference parity, world availability and coin collection

## Scope and design source

The illustrated UI in `assets/previews/menu.jpg`, `levels.jpg`, `skins.jpg` and the original gameplay/paused/result art remains the visual baseline. User-provided screen references are the layout authority. This update **does not** replace SkyPeep illustrations with system buttons, WebViews, stock photos or flattened uninteractive screenshots.

All drawn purchase/equip controls must coincide exactly with their `SkyPeepUiTouch` hitboxes. Screen-by-screen acceptance covers Splash, Main Menu, Levels, Skins, Shop, Settings, Play/HUD, Pause, failure and Victory.

## Requested rule changes

- All six illustrated worlds are playable immediately; the old locked-world gating no longer determines map selection or next-flight navigation.
- Each world separately tracks **10,001 actual picked-up coin icons** across multiple attempts. Only real contact with a coin contributes one. Each pickup still gives **5 wallet currency**, preserving purchasing power.
- Progress never decreases after buying bird costumes or trails and does not reset when the player retries, dies, changes difficulty, or chooses another world.
- The map, Play screen and result screen display the collection goal, separate from **score / pillar passes**. A world is cleared when and only when the actual pickup tally reaches 10,001.
- Delete all streak-based and one-time victory **bonus coin** awards. Precision still determines the star rating; pillars still score points, flight physics/collision thresholds are unchanged.
- Rename world 2 from **Cloud Pass** to **Cloud Drift** in app text and current docs, without replacing its background art.
- Dynamic coin/gem wallets should always display true saved values, not original screenshot placeholder examples.

## UI polish priorities and verification

- Keep premium source colors, fonts, wood/cream/blue/green NinePatch controls, bird sprite, six maps and backgrounds.
- Use contrasting text inside Settings toggles, currency panels, price tags and skins/shop BUY/EQUIP/EQUIPPED controls.
- Keep every painted purchase/equip badge positioned using the same reusable rectangles that route touches; test non-button gutters as inert.
- Avoid draw-path allocations, additional loading flashes or replacing user progress keys.
- Android 36 target, Android API 35 emulator screenshot matrix, all six world navigation, gamepad-free touch and pause tests, JUnit, original art hash, dead-code audit and three binary packages are required before publishing.

**Limits:** Pixel-for-pixel 1:1 parity of all new screenshots against references must be visually inspected; code review, automated MAE and screenshots alone are not proof. Existing user-device crashes also require physical-device logs. An unsigned Play AAB and upstream F-Droid metadata do not mean either store has accepted an update.
