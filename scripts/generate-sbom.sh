#!/usr/bin/env bash
set -euo pipefail

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
output="${1:-$repo_root/apps/android/xiaohei-android/build/xiaohei-sbom.cdx.json}"
apk="${XIAOHEI_APK:-}"
asr_apk="${XIAOHEI_LOCAL_ASR_APK:-}"
mkdir -p "$(dirname "$output")"
python3 - "$output" "$apk" "$asr_apk" <<'PY'
import datetime
import hashlib
import json
import pathlib
import sys

output, apk, asr = map(pathlib.Path, sys.argv[1:])
components = [{
    "type": "application",
    "bom-ref": "pkg:generic/xiaohei-android@0.2.0-alpha.1",
    "name": "xiaohei-android",
    "version": "0.2.0-alpha.1",
    "licenses": [{"license": {"id": "MIT"}}],
}]
if asr.is_file():
    digest = hashlib.sha256(asr.read_bytes()).hexdigest()
    components.append({
        "type": "library",
        "bom-ref": "pkg:generic/sherpa-onnx-android@1.13.4",
        "name": "sherpa-onnx Android + local model bundle",
        "version": "1.13.4",
        "hashes": [{"alg": "SHA-256", "content": digest}],
        "licenses": [{"license": {"name": "Upstream code Apache-2.0; model redistribution requires separate review"}}],
        "scope": "required",
    })
metadata_component = components[0].copy()
if apk.is_file():
    metadata_component["hashes"] = [{
        "alg": "SHA-256", "content": hashlib.sha256(apk.read_bytes()).hexdigest()
    }]
bom = {
    "bomFormat": "CycloneDX",
    "specVersion": "1.5",
    "version": 1,
    "metadata": {
        "timestamp": datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat(),
        "component": metadata_component,
        "tools": [{"vendor": "Xiaohei project", "name": "generate-sbom.sh"}],
    },
    "components": components,
}
output.write_text(json.dumps(bom, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"built SBOM: {output} components={len(components)}")
PY
