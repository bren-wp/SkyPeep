#!/usr/bin/env bash
# No credentials or store signing keys are used by this release preparation.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$root"
version="$(sed -nE "s/^[[:space:]]*versionName '([^']+)'[[:space:]]*$/\1/p" app/build.gradle)"
version_code="$(sed -nE 's/^[[:space:]]*versionCode[[:space:]]+([0-9]+).*$/\1/p' app/build.gradle)"
[[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "Invalid versionName" >&2; exit 1; }
[[ "$version_code" =~ ^[0-9]+$ ]] || { echo "Invalid versionCode" >&2; exit 1; }

debug=app/build/outputs/apk/debug/app-debug.apk
release=app/build/outputs/apk/release/app-release-unsigned.apk
bundle=app/build/outputs/bundle/release/app-release.aab
for asset in "$debug" "$release" "$bundle"; do
  test -s "$asset" || { echo "Missing Android binary: $asset" >&2; exit 1; }
  unzip -t "$asset" >/dev/null
done
test -s .fdroid.yml && test -s LICENSE && test -s docs/ANDROID_DISTRIBUTION.md
test -s fastlane/metadata/android/en-US/short_description.txt
test -s fastlane/metadata/android/en-US/full_description.txt

sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
test -n "$sdk" || { echo "ANDROID_HOME / ANDROID_SDK_ROOT missing" >&2; exit 1; }
apksigner="$(find "$sdk/build-tools" -name apksigner -type f -print -quit)"
test -x "$apksigner" || { echo "No Android apksigner found" >&2; exit 1; }
"$apksigner" verify "$debug" >/dev/null || {
  echo "Debug APK not signed as expected" >&2; exit 1;
}
if "$apksigner" verify "$release" >/dev/null 2>&1; then
  echo "Release APK unexpectedly signed; refusing publication" >&2; exit 1;
fi
if unzip -Z -1 "$bundle" | grep -Eiq '^META-INF/[^/]+\.(RSA|DSA|EC|SF)$'; then
  echo "App Bundle appears signed; refusing publication" >&2; exit 1;
fi

out=release-packages
rm -rf "$out"
mkdir -p "$out"
base="SkyPeep-${version}"
cp "$debug" "$out/${base}-debug.apk"
cp "$release" "$out/${base}-release-unsigned.apk"
cp "$bundle" "$out/${base}-play-upload-unsigned.aab"
cp .fdroid.yml "$out/${base}-fdroid-metadata.yml"
# Generate distribution metadata pinned to the exact immutable release commit.
python3 - "$out/${base}-fdroid-metadata.yml" "$version" "$version_code" "$(git rev-parse HEAD)" <<'PY'
from pathlib import Path
import re
import sys

path, version, code, commit = sys.argv[1:]
assert re.fullmatch(r'[0-9a-f]{40}', commit)
assert re.fullmatch(r'[0-9]+', code)
metadata = Path(path)
contents = metadata.read_text(encoding='utf-8')
anchor = 'AutoUpdateMode: None'
assert contents.count(anchor) == 1
if not re.search(r'(?m)^    versionCode: ' + re.escape(code) + r'$', contents):
    build = (
        "  - versionName: '" + version + "'\n"
        + "    versionCode: " + code + "\n"
        + "    commit: " + commit + "\n"
        + "    gradle:\n"
        + "      - 'yes'\n"
    )
    contents = contents.replace(anchor, build + anchor)
contents = re.sub(r'(?m)^CurrentVersion:.*$', "CurrentVersion: '" + version + "'", contents)
contents = re.sub(r'(?m)^CurrentVersionCode:.*$', 'CurrentVersionCode: ' + code, contents)
assert ('    commit: ' + commit) in contents
assert ('CurrentVersionCode: ' + code) in contents
metadata.write_text(contents, encoding='utf-8')
PY
cp LICENSE "$out/LICENSE-MIT.txt"
cp docs/ANDROID_DISTRIBUTION.md "$out/${base}-SIGNING-AND-FDROID.md"
{
  printf 'SkyPeep %s (Android versionCode %s)\n' "$version" "$version_code"
  printf 'Git commit: %s\n' "$(git rev-parse HEAD)"
  printf '%s\n' 'Files:'
  printf '%s\n' '  *-debug.apk: debug-key signed, TESTING ONLY'
  printf '%s\n' '  *-release-unsigned.apk: sign locally with your distribution key'
  printf '%s\n' '  *-play-upload-unsigned.aab: sign with your Google Play upload key'
  printf '%s\n' '  *-fdroid-metadata.yml: source-build metadata; F-Droid signs independently'
  printf '%s\n' '  LICENSE-MIT.txt: review original asset/dependency licenses for F-Droid'
  printf '%s\n' '  *-SIGNING-AND-FDROID.md: secure signing and store submission'
  printf '%s\n' '  SHA256SUMS.txt: hashes of the seven other files'
  printf 'Source at https://github.com/bren-wp/SkyPeep/tree/%s\n' "$(git rev-parse HEAD)"
} > "$out/${base}-CONTENTS.txt"
(
  cd "$out"
  find . -maxdepth 1 -type f ! -name SHA256SUMS.txt -printf '%f\0' |
    sort -z | xargs -0 sha256sum > SHA256SUMS.txt
  test "$(wc -l < SHA256SUMS.txt)" -eq 7
  sha256sum --check --quiet SHA256SUMS.txt
)
echo "PASS: APK, unsigned APK, unsigned AAB, F-Droid metadata, license, instructions, SHA-256."
find "$out" -maxdepth 1 -type f -printf '%f\n' | sort
