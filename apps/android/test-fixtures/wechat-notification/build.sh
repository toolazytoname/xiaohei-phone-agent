#!/usr/bin/env bash
set -euo pipefail

fixture_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}"
platform="$sdk_root/platforms/android-36/android.jar"
tools_dir="$sdk_root/build-tools/36.0.0"
out="$fixture_dir/build"
keystore="${ANDROID_DEBUG_KEYSTORE:-$HOME/.android/debug.keystore}"
mkdir -p "$out/classes" "$out/dex"
rm -f "$out"/*.apk "$out/dex"/*.dex
"$tools_dir/aapt2" link -I "$platform" --manifest "$fixture_dir/AndroidManifest.xml" \
  --debug-mode --min-sdk-version 26 --target-sdk-version 35 -o "$out/unsigned.apk"
javac -encoding UTF-8 -source 8 -target 8 -classpath "$platform" -d "$out/classes" \
  "$fixture_dir/src/com/tencent/mm/FixtureActivity.java"
"$tools_dir/d8" --min-api 26 --output "$out/dex" $(find "$out/classes" -name '*.class' -print)
(cd "$out/dex" && zip -q -u "$out/unsigned.apk" classes.dex)
"$tools_dir/zipalign" -f 4 "$out/unsigned.apk" "$out/aligned.apk"
"$tools_dir/apksigner" sign --ks "$keystore" --ks-key-alias androiddebugkey \
  --ks-pass pass:android --key-pass pass:android --out "$out/wechat-notification-fixture.apk" "$out/aligned.apk"
printf 'built test-only fixture: %s\n' "$out/wechat-notification-fixture.apk"
