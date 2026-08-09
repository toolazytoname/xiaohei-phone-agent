#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "usage: $0 /path/to/OVoiceManagerServiceOnePlus.apk /path/to/local/model.uim" >&2
  exit 2
fi

source_apk="$1"
output_model="$2"
asset="assets/sm4_xiaobuxiaobu.uim"
[[ -s "$source_apk" ]] || { echo "missing source APK: $source_apk" >&2; exit 1; }
mkdir -p "$(dirname "$output_model")"
unzip -p "$source_apk" "$asset" > "$output_model"
[[ -s "$output_model" ]] || { rm -f "$output_model"; echo "asset not found: $asset" >&2; exit 1; }
chmod 0600 "$output_model"
echo "extracted private local model: $output_model"
