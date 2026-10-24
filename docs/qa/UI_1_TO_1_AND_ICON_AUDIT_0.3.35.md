# SkyPeep 0.3.35 — 1:1 illustration, touch, icon and UX audit

## Original source of truth

The user-provided portrait references in `assets/previews/menu.jpg`,
`levels.jpg`, `skins.jpg`, `gameplay.jpg` remain untouched. They are
520×924 JPEG references of a 941×1672 logical drawing canvas.
Other screens (Splash, Pause, Results, Victory, Shop, Settings) do not
have independent original per-screen references; they are reviewed against
consistent SkyPeep motifs and actual emulator screen captures, not falsely
certified as source-pixel-identical.

## Audited screen states

| Screen | Assets / state | Touch and visual contract |
|---|---|---|
| Splash | source `screen_splash` | tap to Menu, preserve undistorted content |
| Menu | exact original `screen_menu` | PLAY, LEVELS, SKINS, SHOP, SETTINGS and wallet controls |
| Map | exact original `screen_map` | six open islands, correct status chip and selected world |
| Gameplay | reconstructed active per-world scenery | physically collected currency, score, lives, pause and 6 themed worlds |
| Pause | original `screen_pause` | RESUME, RESTART, HOME, MUSIC and SFX separate, inert gutters |
| Lose | original `screen_over` | accurate HOME/RETRY/SHARE and inert card background |
| Victory | native scene from active world art | accurate HOME/NEXT/SHARE, stars and score |
| Skins | original `screen_skins` | 4 cards, correct wallet, BUY/EQUIP and DEFAULT state |
| Shop | world art plus branded parchment | only visible BUY/EQUIP badges activate, wallet checks |
| Settings | world art plus branded parchment | accurate row toggles, Back, difficulty |

## Confirmed defect fixed

Previously `clickSkins` activated a loose rectangle (x285..683,y810..930)
overlapping the DEFAULT name plaque and missing the bottom of the actual
green EQUIPPED button. When a nondefault outfit was worn, the game also
replaced the static DEFAULT name with that outfit name while the baked
green button incorrectly kept reading EQUIPPED. Now the DEFAULT name
remains source-exact; the green button toggles dynamic EQUIP/EQUIPPED
and its drawn/touch geometry shares one `defaultEquipRect`.

## Launcher icon audit

The existing highest-density icon depicts the correct original blue aviator
but the bird occupies only about 104 of 192 pixels horizontally (~54%).
Build tooling now extracts this local source asset, enlarges the bird
within the original blue circular style and generates Android legacy
launcher icons at every density and a separate adaptive-icon foreground.
This avoids new unrelated artwork and an undersized launcher subject.

## QA scope

- Kotlin tests for all UI hitboxes, skins DEFAULT control and award ownership;
- Android emulator navigates ten distinct screen states, five responsive
  sizes, all six worlds, costume purchase and reverting to Default;
- Automated visual comparison includes source Menu/Map/Skins/Game screenshot
  metrics and real captured screenshots, but does NOT establish absolute 1:1;
- Gradle checks resource generation, image dimensions and adaptive XML,
  debug/release APK, unsigned AAB, four ABIs and source art SHA-256;
- Physical-device cutouts, status/gesture bar insets and GPU context-loss
  behaviour still require manual QA and crash logs.
