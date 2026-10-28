# Android distribution: Google Play, APK and F-Droid

## Every verified SkyPeep 0.3.24+ release contains

| Asset | Intended destination | Signing state |
|---|---|---|
| SkyPeep-X.Y.Z-debug.apk | Android manual testing | Debug signed, NOT production |
| SkyPeep-X.Y.Z-release-unsigned.apk | Sideload/other stores, sign locally | NOT installable unchanged |
| SkyPeep-X.Y.Z-play-upload-unsigned.aab | Google Play after signing | NOT accepted by Play unchanged |
| SkyPeep-X.Y.Z-fdroid-metadata.yml | Source build recipe for F-Droid review | Text metadata (tracks prior published source and pins each new release at packaging) |
| LICENSE-MIT.txt | Source code license | MIT |
| SkyPeep-X.Y.Z-SIGNING-AND-FDROID.md | This file and signing guidance | Text |
| SkyPeep-X.Y.Z-CONTENTS.txt | Included files and exact Git commit | Text |
| SHA256SUMS.txt | SHA-256 digest for other seven files | Text |

GitHub automatically also provides source-code archives for the tagged commit.
Verify all downloaded files by running `sha256sum --check SHA256SUMS.txt`.

## Google Play — AAB signing

Google Play requires the App Bundle to be signed by the **existing upload key**
for the **same application ID** com.brendigo.skypeep. The bundle provided by CI
is deliberately unsigned because keys and passwords must not be stored in GitHub.

On a trusted machine that already has your proper keystore, run:

```sh
jarsigner -keystore /secure/sky-upload.jks \
  -signedjar SkyPeep-0.3.35-play-signed.aab \
  SkyPeep-0.3.35-play-upload-unsigned.aab YOUR_EXISTING_UPLOAD_ALIAS

jarsigner -verify -verbose -certs SkyPeep-0.3.24-play-signed.aab
```

Use the real filename/version. Enter credentials only in local prompts.
Upload the signed AAB to Play Console only when its versionCode is higher
than the versionCodes already uploaded to that application. Play App Signing
creates optimized APKs from the App Bundle. The debug APK is not a substitute
for the Play-signed bundle. Never upload your key, signed keystore or passwords.

## Independent APK distribution

An unsigned release APK must be ZIP-aligned and signed before it can be installed:

```sh
zipalign -p -f 4 SkyPeep-0.3.35-release-unsigned.apk \
  SkyPeep-0.3.35-release-aligned.apk
apksigner sign --ks /secure/distribution.jks \
  --ks-key-alias YOUR_EXISTING_DISTRIBUTION_ALIAS \
  --out SkyPeep-0.3.35-release-signed.apk \
  SkyPeep-0.3.35-release-aligned.apk
apksigner verify --verbose SkyPeep-0.3.35-release-signed.apk
```

An installed package can only be updated with a matching signing certificate
and a higher versionCode. Debug, Google Play and F-Droid channels may use
different certs. Switching signing identities may require uninstalling the
old copy, which can erase local saves because Android backups are disabled.

## Official F-Droid submission preparation

F-Droid **builds the APK directly from public source and signs it with its own
key**. The unsigned APK attached to GitHub is useful for signing and auditing,
but is not what an official F-Droid repository should distribute as-is.

The upstream F-Droid metadata file is `.fdroid.yml`, and localized descriptions
are stored under `fastlane/metadata/android/en-US/` and `hr-HR/`.
The repository's upstream `.fdroid.yml` currently keeps the last published source (0.3.35; versionCode 47). **At packaging time** the
release asset `SkyPeep-X.Y.Z-fdroid-metadata.yml` is automatically expanded
to contain a `Builds` entry pinned to that release's **exact 40-character Git
commit**, `versionName` and `versionCode`. This gives F-Droid reviewers a
version-correct, immutable source recipe rather than a development branch.
After validation, sync the new entry back into upstream metadata and perform
an independent F-Droid build/scan before submitting.

To submit to the main F-Droid catalog, propose the metadata file in the
fdroiddata GitLab repository; verify the dependency scanner and actual
F-Droid build with `fdroid lint` and `fdroid build`. Review the provenance
and free licensing of the original artwork, music, sounds and LibGDX native
libraries as well as Kotlin sources. The presence of MIT LICENSE for the
repository alone does not establish F-Droid eligibility for every asset.
Acceptance into the main F-Droid repository is subject to independent review.

Real Android emulator screenshots should be added as actual store media once
visually reviewed; existing assets/previews images are **concept references**.

## Rebuild and verify on a trusted machine

Install Android SDK API 36, Java 17 and Gradle 8.14.3, then:

```sh
python3 tools/dead_code_audit.py
gradle :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease
bash tools/package-android-release.sh
cd release-packages && sha256sum --check SHA256SUMS.txt
```

The release packaging script verifies that the debug APK is signed and the
release APK is unsigned, validates file ZIP contents, and checks for AAB JAR
signatures. No signing key is used by the public CI.
