#!/usr/bin/env bash
set -euo pipefail

project_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
sdk_root="${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}"
platform="$sdk_root/platforms/android-36/android.jar"
build_tools="$sdk_root/build-tools/36.0.0"
build_dir="$project_dir/build"
keystore="${ANDROID_DEBUG_KEYSTORE:-$HOME/.android/debug.keystore}"
local_asr_apk="${XIAOHEI_LOCAL_ASR_APK:-}"
local_kws_apk="${XIAOHEI_LOCAL_KWS_APK:-}"
variant="${XIAOHEI_BUILD_VARIANT:-debug}"
version_code="${XIAOHEI_VERSION_CODE:-4}"
version_name="${XIAOHEI_VERSION_NAME:-0.2.0-alpha.3}"

if [[ "$variant" != debug && "$variant" != release ]]; then
  printf 'invalid XIAOHEI_BUILD_VARIANT: %s\n' "$variant" >&2
  exit 1
fi
if [[ "$variant" == release ]]; then
  keystore="${XIAOHEI_RELEASE_KEYSTORE:-}"
  key_alias="${XIAOHEI_RELEASE_KEY_ALIAS:-}"
  store_pass="${XIAOHEI_RELEASE_STORE_PASS:-}"
  key_pass="${XIAOHEI_RELEASE_KEY_PASS:-}"
  [[ -s "$keystore" && -n "$key_alias" && -n "$store_pass" && -n "$key_pass" ]] || {
    printf 'release build requires XIAOHEI_RELEASE_KEYSTORE, _KEY_ALIAS, _STORE_PASS and _KEY_PASS\n' >&2
    exit 1
  }
  if [[ "${XIAOHEI_ALLOW_TEST_SIGNING:-0}" != 1 && ( "$key_alias" == androiddebugkey || "$keystore" == *debug.keystore ) ]]; then
    printf 'refusing public release signed by Android debug key\n' >&2
    exit 1
  fi
  if [[ ( -n "$local_asr_apk" || -n "$local_kws_apk" ) && "${XIAOHEI_MODEL_REDISTRIBUTION_APPROVED:-0}" != 1 ]]; then
    printf 'release build with ASR/KWS assets requires XIAOHEI_MODEL_REDISTRIBUTION_APPROVED=1 after documented rights review\n' >&2
    exit 1
  fi
else
  key_alias="androiddebugkey"
  store_pass="android"
  key_pass="android"
fi

for required in "$platform" "$build_tools/aapt2" "$build_tools/zipalign" "$build_tools/apksigner" "$build_tools/d8"; do
  [[ -e "$required" ]] || { printf 'missing Android build dependency: %s\n' "$required" >&2; exit 1; }
done

mkdir -p "$build_dir/compiled" "$build_dir/generated" "$build_dir/classes" "$build_dir/dex"
rm -rf "$build_dir/asr" "$build_dir/kws" "$build_dir/assets"
rm -f "$build_dir"/*.apk "$build_dir/dex"/classes*.dex
mkdir -p "$build_dir/assets"
cp -R "$project_dir/assets/." "$build_dir/assets/"

if [[ ! -s "$keystore" ]]; then
  mkdir -p "$(dirname "$keystore")"
  keytool -genkeypair -noprompt -keystore "$keystore" -storepass android -keypass android \
    -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
fi

"$build_tools/aapt2" compile --dir "$project_dir/res" -o "$build_dir/compiled/resources.zip"
if [[ -n "$local_asr_apk" ]]; then
  [[ -s "$local_asr_apk" ]] || { printf 'missing local ASR APK: %s\n' "$local_asr_apk" >&2; exit 1; }
  mkdir -p "$build_dir/asr"
  (cd "$build_dir/asr" && unzip -q "$local_asr_apk" 'assets/*' 'lib/arm64-v8a/*' 'classes*.dex')
fi
if [[ -n "$local_kws_apk" ]]; then
  [[ -s "$local_kws_apk" ]] || { printf 'missing local KWS APK: %s\n' "$local_kws_apk" >&2; exit 1; }
  mkdir -p "$build_dir/kws"
  (cd "$build_dir/kws" && unzip -q "$local_kws_apk" 'assets/*' 'lib/arm64-v8a/*' 'classes*.dex')
fi

# KWS distributions also contain the complete sherpa runtime. Prefer that one
# when present, while merging the independently sourced ASR and KWS assets.
runtime_dir="$build_dir/asr"
if [[ -n "$local_kws_apk" ]]; then runtime_dir="$build_dir/kws"; fi
if [[ -n "$local_asr_apk" || -n "$local_kws_apk" ]]; then
  mkdir -p "$build_dir/assets"
  if [[ -d "$build_dir/asr/assets" ]]; then cp -R "$build_dir/asr/assets/." "$build_dir/assets/"; fi
  if [[ -d "$build_dir/kws/assets" ]]; then cp -R "$build_dir/kws/assets/." "$build_dir/assets/"; fi
  if [[ -n "$local_kws_apk" ]]; then
    kws_model="$build_dir/assets/sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01"
    cp "$project_dir/kws/keywords.txt" "$kws_model/keywords.txt"
    # The upstream APK duplicates two model checkpoints. The app uses epoch 12.
    find "$kws_model" -name '*epoch-99*' -delete
  fi
fi

link_args=(-I "$platform" --manifest "$project_dir/AndroidManifest.xml" --java "$build_dir/generated"
  --min-sdk-version 26 --target-sdk-version 35 --version-code "$version_code" --version-name "$version_name")
if [[ "$variant" == debug ]]; then link_args+=(--debug-mode); fi
link_args+=(-A "$build_dir/assets")
"$build_tools/aapt2" link "${link_args[@]}" -o "$build_dir/unsigned.apk" "$build_dir/compiled/resources.zip"
find "$project_dir/src" "$build_dir/generated" -name '*.java' -print0 | \
  xargs -0 javac -encoding UTF-8 -source 8 -target 8 -classpath "$platform" -d "$build_dir/classes"
"$build_tools/d8" --min-api 26 --output "$build_dir/dex" $(find "$build_dir/classes" -name '*.class' -print)
# APK Signature Scheme v2/v3 covers ZIP metadata. Normalize generated DEX
# timestamps and omit platform-specific ZIP extra fields before signing.
find "$build_dir/dex" -name 'classes*.dex' -exec touch -t 198001010000 {} +
(cd "$build_dir/dex" && zip -X -q -u "$build_dir/unsigned.apk" classes.dex)
if [[ -n "$local_asr_apk" || -n "$local_kws_apk" ]]; then
  next_dex=2
  for source_dex in "$runtime_dir"/classes*.dex; do
    cp "$source_dex" "$build_dir/dex/classes${next_dex}.dex"
    touch -t 198001010000 "$build_dir/dex/classes${next_dex}.dex"
    (cd "$build_dir/dex" && zip -X -q -u "$build_dir/unsigned.apk" "classes${next_dex}.dex")
    next_dex=$((next_dex + 1))
  done
  (cd "$runtime_dir" && zip -X -q -0 -u "$build_dir/unsigned.apk" lib/arm64-v8a/*.so)
fi
"$build_tools/zipalign" -f 4 "$build_dir/unsigned.apk" "$build_dir/aligned.apk"
output="$build_dir/xiaohei-$variant.apk"
"$build_tools/apksigner" sign --ks "$keystore" --ks-key-alias "$key_alias" \
  --ks-pass "pass:$store_pass" --key-pass "pass:$key_pass" --v1-signing-enabled false \
  --out "$output" "$build_dir/aligned.apk"
"$build_tools/apksigner" verify --verbose "$output"
printf 'built: %s variant=%s version=%s(%s)\n' "$output" "$variant" "$version_name" "$version_code"
