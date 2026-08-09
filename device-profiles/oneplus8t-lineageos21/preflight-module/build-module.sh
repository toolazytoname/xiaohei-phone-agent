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
cp "$module_dir/system/etc/sysconfig/xiaohei-dsp-hiddenapi.xml" \
  "$output_root/system/etc/sysconfig/xiaohei-dsp-hiddenapi.xml"
cp "$module_dir/system/system_ext/etc/permissions/privapp-permissions-xiaohei-dsp.xml" \
  "$output_root/system/system_ext/etc/permissions/privapp-permissions-xiaohei-dsp.xml"
cp "$apk" "$output_root/system/system_ext/priv-app/XiaoheiDspCompanion/XiaoheiDspCompanion.apk"
(cd "$output_root" && zip -q -r "$output_zip" .)
echo "built: $output_zip"
