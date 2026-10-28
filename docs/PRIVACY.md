# SkyPeep — Privacy and local progress

**Last reviewed: 28 October 2026**

SkyPeep by Brendigo is an offline Android arcade game. The game does not ask for an account, display ads or use an analytics, advertising or remote crash-reporting SDK. Its Android manifest does not request INTERNET, location, contacts, camera, microphone or file-storage permissions. The app requests only the normal Android `VIBRATE` permission for optional haptic feedback when the player enables vibration. It does not require access to sensors or personal data for vibration. The merged manifests for debug and release are verified in CI.

## What stays on the device

Scores, stars, selected world and difficulty, collected coins and gems, owned and equipped bird costumes and flight trails, and music/sound/vibration settings are saved in the app's private Android preferences. These are not sent to a Brendigo server. No in-game purchases use real money.

The optional **SHARE** button prepares a plain-text score summary in Android's system share chooser. Nothing is sent by SkyPeep itself; only if the player chooses a recipient/app can that other application receive the summary, subject to its own terms.

Android's local audio engine plays bundled sound effects and music; SkyPeep does not record audio, listen through the microphone or send sensor data.

## Progress durability and limitations

SkyPeep writes collected coins and gems during real pickups and saves preferences on key events and pause. If a saved preference cannot be read reliably, the game will refuse further writes **for that session** rather than risk overwriting existing values with defaults. Cosmetic purchases and equipment changes are blocked until storage is safe. Following a failed flush, the complete in-memory progress snapshot must be saved successfully before subsequent limited pickup/record writes can clear the warning. A visible warning explains that storage must be checked.

SkyPeep currently disables Android app backup. **Uninstalling the application, clearing its storage or changing to an incompatible APK signing identity can permanently remove local game progress.** Do not uninstall an installed version merely to switch between Play, debug and F-Droid signatures. The game cannot guarantee recovery from device failure or storage corruption.

## Testing and publicly shared screenshots

The public repository's screenshot gallery contains only automated emulator captures and debug fixtures, not screenshots collected from players' devices. Crash logs from players are not uploaded automatically.

## Questions

Privacy questions and bug reports can be submitted through the [SkyPeep issue tracker](https://github.com/bren-wp/SkyPeep/issues). Please do not post passwords, personally identifying data or other private information publicly in a GitHub issue.
