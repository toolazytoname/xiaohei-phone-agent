#!/usr/bin/env bash
set -euo pipefail

project_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}"
platform="$sdk_root/platforms/android-36/android.jar"
build_tools="$sdk_root/build-tools/36.0.0"
build_dir="$project_dir/build"
keystore="${ANDROID_DEBUG_KEYSTORE:-$HOME/.android/debug.keystore}"

for required in "$platform" "$build_tools/aapt2" "$build_tools/zipalign" "$build_tools/apksigner" "$build_tools/d8"; do
  [[ -e "$required" ]] || { printf 'missing Android build dependency: %s\n' "$required" >&2; exit 1; }
done
mkdir -p "$build_dir/compiled" "$build_dir/generated" "$build_dir/classes" "$build_dir/dex"
rm -f "$build_dir"/*.apk "$build_dir/dex/classes.dex"
if [[ ! -s "$keystore" ]]; then
  mkdir -p "$(dirname "$keystore")"
  keytool -genkeypair -noprompt -keystore "$keystore" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
fi
"$build_tools/aapt2" compile --dir "$project_dir/res" -o "$build_dir/compiled/resources.zip"
"$build_tools/aapt2" link -I "$platform" --manifest "$project_dir/AndroidManifest.xml" \
  --java "$build_dir/generated" --min-sdk-version 26 --target-sdk-version 27 \
  --version-code 1 --version-name 0.1.0 -o "$build_dir/unsigned.apk" "$build_dir/compiled/resources.zip"
find "$project_dir/src" "$build_dir/generated" -name '*.java' -print0 | \
  xargs -0 javac -encoding UTF-8 -source 8 -target 8 -classpath "$platform" -d "$build_dir/classes"
"$build_tools/d8" --min-api 26 --output "$build_dir/dex" $(find "$build_dir/classes" -name '*.class' -print)
(cd "$build_dir/dex" && zip -q -u "$build_dir/unsigned.apk" classes.dex)
"$build_tools/zipalign" -f 4 "$build_dir/unsigned.apk" "$build_dir/aligned.apk"
"$build_tools/apksigner" sign --ks "$keystore" --ks-key-alias androiddebugkey --ks-pass pass:android \
  --key-pass pass:android --out "$build_dir/xiaohei-dsp-companion-debug.apk" "$build_dir/aligned.apk"
"$build_tools/apksigner" verify --verbose "$build_dir/xiaohei-dsp-companion-debug.apk"
printf 'built: %s\n' "$build_dir/xiaohei-dsp-companion-debug.apk"
