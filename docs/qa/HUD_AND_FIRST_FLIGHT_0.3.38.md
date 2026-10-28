# SkyPeep 0.3.38 — gameplay HUD and uncluttered first flight review

This update uses the exact four user-supplied 520x924 reference JPEGs already committed in `assets/previews`. Those original files are byte-identical to the uploaded reference images and remain unmodified. No new generated artwork, game physics or save schema is introduced.

## Specific problems

- The live 10,001-pickup counter previously occupied a dark chip directly below the central illustrated score, competing with and visually merging into the SUN PEAK-style winged ribbon. It is now kept in the left margin under the small hearts.
- The three lives were drawn with oversized spacing and lacked a reserved status lane. Their new 21-unit icons fit below the left coin badge and do not intersect the centre score.
- The live gameplay values incorrectly used the saved wallet, unlike the supplied reference where gameplay values differ from menu totals. Gameplay now renders separately counted **real coin and gem pickups in the current flight**. Menu/Skins/Shop keep the original stored wallet; per-world 10,001 actual pickups and the existing wallet rewards are unchanged. Numeric labels are reduced/recentred in their original plates.
- The first-flight prompt has been completely removed: no tooltip or instruction box obscures the original painting. The first touch still starts the flight, and the existing input and physics are unchanged.
- The illustrated score image previously used a compressed height of 174 units for a 315-unit width (source aspect 367:210). The new 315x180 dimensions are within 0.13% of source ratio. Generated score pixels are now matched to each of the six different original backgrounds (pale Cloud Drift, navy Wind Gate, sky-blue Sun Peak), instead of a cream rectangle or one generic cyan fill. Wings and name ribbon are verified unchanged outside the allowed score inset.

## Layout invariants

At the 941x1672 design size, the score is drawn x=313..628, top-down y=8..188. The goal chip is x=14..274 and top-down y=205..255. The heart centres are x=47/98/149 at top-down y=156. The coin/gem/pause plates retain their previous anchored positions and use the physical screen edges on wide/tall displays. Source banners are no longer covered by a centre goal panel.

`SkyPeepHudLayoutTest` checks HUD non-overlap constraints for six portrait/tablet sizes: 360x640, 720x1280, 1080x1920, 1080x2400, 1200x1920 and 1600x2000.

`tools/CompareSkyPeepScreens.java` additionally reports actual Android screenshot MAE for the sky-start coin/lives, winged score, gems/pause and left goal regions. All these are *diagnostic measurements*; dynamic coin values, bird pose and obstacles are expected to differ from static concept art, and no 1:1 certification is asserted.

## Remaining visual review

Issue #44 remains open for the original scenery seams. Evaluate each world (not only the static reference) on a physical device and verify the live per-flight HUD, six score badges and unobstructed initial flight view in motion. The four original images cover Menu, Levels, Skins and Sun Peak Gameplay, not every unrelated settings/result screen.

This candidate must complete JVM tests, rebuilt-art integrity, emulator navigation and pixel-comparison reports before a verified release is claimed.
