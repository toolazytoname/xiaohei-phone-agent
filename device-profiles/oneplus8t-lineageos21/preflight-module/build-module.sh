#!/usr/bin/env bash
set -euo pipefail

profile_dir="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
module_dir="$profile_dir/preflight-module"
apk="$profile_dir/../../apps/android/oneplus8t-dsp-companion/build/xiaohei-dsp-companion-debug.apk"
output_root="$module_dir/build/root"
output_zip="$module_dir/build/xiaohei-dsp-preflight.zip"

[[ -s "$apk" ]] || { echo "missing companion APK: $apk" >&2; exit 1; }
rm -rf "$output_root" "$output_zip"
mkdir -p "$output_root/system/etc/sysconfig" \
  "$output_root/system/system_ext/etc/permissions" \
  "$output_root/system/system_ext/priv-app/XiaoheiDspCompanion"
cp "$module_dir/module.prop" "$output_root/module.prop"
cp "$module_dir/customize.sh" "$output_root/customize.sh"
cp "$module_dir/system/etc/sysconfig/xiaohei-dsp-hiddenapi.xml" \
  "$output_root/system/etc/sysconfig/xiaohei-dsp-hiddenapi.xml"
cp "$module_dir/system/system_ext/etc/permissions/privapp-permissions-xiaohei-dsp.xml" \
  "$output_root/system/system_ext/etc/permissions/privapp-permissions-xiaohei-dsp.xml"
cp "$apk" "$output_root/system/system_ext/priv-app/XiaoheiDspCompanion/XiaoheiDspCompanion.apk"
if [[ -n "${XIAOHEI_WAKE_MODEL:-}" ]]; then
  [[ -s "$XIAOHEI_WAKE_MODEL" ]] || { echo "invalid XIAOHEI_WAKE_MODEL" >&2; exit 1; }
  mkdir -p "$output_root/system/system_ext/etc/xiaohei"
  cp "$XIAOHEI_WAKE_MODEL" \
    "$output_root/system/system_ext/etc/xiaohei/sm4_xiaobuxiaobu.uim"
fi
if [[ -n "${XIAOHEI_OEM_LIB_ROOT:-}" ]]; then
  required_system_ext=(
    liblistenjni.qti.so
    liblistensoundmodel2.qti.so
    liblsmclient.so
    vendor.qti.hardware.ListenSoundModel@1.0.so
  )
  for library in "${required_system_ext[@]}"; do
    source_file="$XIAOHEI_OEM_LIB_ROOT/system_ext/lib64/$library"
    [[ -s "$source_file" ]] || { echo "missing private OEM library: $source_file" >&2; exit 1; }
    mkdir -p "$output_root/system/system_ext/lib64"
    cp "$source_file" "$output_root/system/system_ext/lib64/$library"
  done
  public_libraries="$XIAOHEI_OEM_LIB_ROOT/system_ext/etc/public.libraries-qti.txt"
  rnn_plugin="$XIAOHEI_OEM_LIB_ROOT/vendor/lib/libcapiv2svarnn.so"
  [[ -s "$public_libraries" ]] || { echo "missing private linker declaration" >&2; exit 1; }
  [[ -s "$rnn_plugin" ]] || { echo "missing private 32-bit RNN plugin" >&2; exit 1; }
  mkdir -p "$output_root/system/system_ext/etc" "$output_root/system/vendor/lib"
  cp "$public_libraries" "$output_root/system/system_ext/etc/public.libraries-qti.txt"
  cp "$rnn_plugin" "$output_root/system/vendor/lib/libcapiv2svarnn.so"
fi
(cd "$output_root" && zip -q -r "$output_zip" .)
echo "built: $output_zip"
