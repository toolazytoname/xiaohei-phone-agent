#!/usr/bin/env bash
set -euo pipefail

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
apk="${1:-}"
sbom="${2:-}"
output="${3:-$repo_root/dist}"
[[ -s "$apk" && -s "$sbom" ]] || {
  printf 'usage: %s /absolute/path/to-release.apk /absolute/path/to-sbom.json [output-dir]\n' "$0" >&2
  exit 2
}

sdk_root="${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}"
build_tools="${ANDROID_BUILD_TOOLS:-$sdk_root/build-tools/36.0.0}"
tracked_status="$(git -C "$repo_root" status --porcelain --untracked-files=no)"
[[ -z "$tracked_status" ]] || {
  printf 'FAIL tracked source tree is not clean; commit or revert changes before provenance generation\n' >&2
  exit 1
}

XIAOHEI_EXPECT_RELEASE=1 bash "$repo_root/scripts/scan-release-apk.sh" "$apk"
mkdir -p "$output"

python3 - "$repo_root" "$apk" "$sbom" "$output" "$build_tools/aapt2" "$build_tools/apksigner" <<'PY'
import datetime
import hashlib
import json
import pathlib
import re
import shutil
import subprocess
import sys

repo, apk, sbom, output, aapt2, apksigner = map(pathlib.Path, sys.argv[1:])
badging = subprocess.run(
    (str(aapt2), "dump", "badging", str(apk)),
    check=True,
    capture_output=True,
    text=True,
).stdout
package = re.search(r"package: name='([^']+)'", badging)
version_code = re.search(r"versionCode='([^']+)'", badging)
version_name = re.search(r"versionName='([^']+)'", badging)
if not all((package, version_code, version_name)):
    raise SystemExit("cannot parse APK identity")

certs = subprocess.run(
    (str(apksigner), "verify", "--print-certs", str(apk)),
    check=True,
    capture_output=True,
    text=True,
).stdout
certificate = re.search(r"certificate SHA-256 digest: ([0-9a-f]+)", certs, re.IGNORECASE)
if not certificate:
    raise SystemExit("cannot parse signing certificate")

def digest(path):
    hasher = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()

apk_sha = digest(apk)
sbom_data = json.loads(sbom.read_text(encoding="utf-8"))
sbom_hashes = sbom_data.get("metadata", {}).get("component", {}).get("hashes", [])
if not any(item.get("alg") == "SHA-256" and item.get("content") == apk_sha for item in sbom_hashes):
    raise SystemExit("SBOM does not bind the exact APK SHA-256")

revision = subprocess.run(
    ("git", "-C", str(repo), "rev-parse", "HEAD"),
    check=True,
    capture_output=True,
    text=True,
).stdout.strip()
version = version_name.group(1)
apk_name = f"xiaohei-{version}-generic-arm64.apk"
sbom_name = f"xiaohei-{version}.cdx.json"
shutil.copy2(apk, output / apk_name)
shutil.copy2(sbom, output / sbom_name)
for suffix in ("md", "zh-CN.md"):
    source = repo / "docs" / f"release-notes-{version}.{suffix}"
    if source.is_file():
        shutil.copy2(source, output / source.name)

provenance = {
    "schema_version": 1,
    "product_id": "xiaohei-phone-agent",
    "package": package.group(1),
    "version_name": version,
    "version_code": int(version_code.group(1)),
    "source_revision": revision,
    "source_tree_clean": True,
    "generated_at": datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat(),
    "distribution_scope": "generic-arm64-no-embedded-asr-or-kws-models",
    "model_assets_embedded": False,
    "apk": {
        "file": apk_name,
        "sha256": apk_sha,
        "size_bytes": apk.stat().st_size,
        "certificate_sha256": certificate.group(1).lower(),
        "debuggable": False,
    },
    "sbom": {"file": sbom_name, "sha256": digest(output / sbom_name)},
    "known_limitations": [
        "No embedded offline ASR or KWS model in the public candidate",
        "Vendor DSP support requires a separately validated device profile",
        "Foreground CPU wake-word accuracy and power are not qualified",
    ],
}
provenance_path = output / f"xiaohei-{version}.provenance.json"
provenance_path.write_text(json.dumps(provenance, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

checksums = []
for path in sorted(output.glob(f"*{version}*")):
    if path.name != "SHA256SUMS":
        checksums.append(f"{digest(path)}  {path.name}\n")
(output / "SHA256SUMS").write_text("".join(checksums), encoding="utf-8")
print(f"built release bundle: {output} version={version} revision={revision}")
PY
