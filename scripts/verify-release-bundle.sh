#!/usr/bin/env bash
set -euo pipefail

bundle="${1:-}"
[[ -d "$bundle" && -s "$bundle/SHA256SUMS" ]] || {
  printf 'usage: %s /absolute/path/to/release-bundle\n' "$0" >&2
  exit 2
}

repo_root="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
bundle="$(CDPATH= cd -- "$bundle" && pwd)"

(cd "$bundle" && shasum -a 256 -c SHA256SUMS)

shopt -s nullglob
apks=("$bundle"/xiaohei-*-generic-arm64.apk)
sboms=("$bundle"/xiaohei-*.cdx.json)
provenances=("$bundle"/xiaohei-*.provenance.json)
[[ ${#apks[@]} -eq 1 && ${#sboms[@]} -eq 1 && ${#provenances[@]} -eq 1 ]] || {
  printf 'FAIL bundle must contain exactly one generic APK, SBOM, and provenance file\n' >&2
  exit 1
}

XIAOHEI_EXPECT_RELEASE=1 bash "$repo_root/scripts/scan-release-apk.sh" "${apks[0]}"

python3 - "${apks[0]}" "${sboms[0]}" "${provenances[0]}" "$repo_root" <<'PY'
import hashlib
import json
import pathlib
import subprocess
import sys

apk, sbom, provenance, repo = map(pathlib.Path, sys.argv[1:])

def sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()

apk_sha = sha256(apk)
sbom_sha = sha256(sbom)
sbom_data = json.loads(sbom.read_text(encoding="utf-8"))
provenance_data = json.loads(provenance.read_text(encoding="utf-8"))

assert any(
    item.get("alg") == "SHA-256" and item.get("content") == apk_sha
    for item in sbom_data.get("metadata", {}).get("component", {}).get("hashes", [])
), "SBOM does not bind the exact APK"
assert provenance_data.get("apk", {}).get("file") == apk.name, "provenance APK filename mismatch"
assert provenance_data.get("apk", {}).get("sha256") == apk_sha, "provenance APK hash mismatch"
assert provenance_data.get("sbom", {}).get("file") == sbom.name, "provenance SBOM filename mismatch"
assert provenance_data.get("sbom", {}).get("sha256") == sbom_sha, "provenance SBOM hash mismatch"
assert provenance_data.get("source_tree_clean") is True, "provenance does not attest a clean source tree"
revision = provenance_data.get("source_revision", "")
assert len(revision) == 40 and all(char in "0123456789abcdef" for char in revision), "invalid source revision"
subprocess.run(("git", "-C", str(repo), "cat-file", "-e", f"{revision}^{{commit}}"), check=True)
print(f"PASS release_bundle_binding apk_sha256={apk_sha} source_revision={revision}")
PY
