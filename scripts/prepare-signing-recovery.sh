#!/usr/bin/env bash
set -euo pipefail

keystore="${1:-}"
output_dir="${2:-}"
recipient="${XIAOHEI_RECOVERY_RECIPIENT:-}"
alias_name="${XIAOHEI_RELEASE_KEY_ALIAS:-}"
store_pass="${XIAOHEI_RELEASE_STORE_PASS:-}"
identity_file="${XIAOHEI_RECOVERY_IDENTITY_FILE:-}"
[[ -s "$keystore" && -n "$output_dir" && -n "$recipient" && -n "$alias_name" && -n "$store_pass" ]] || {
  printf 'usage: XIAOHEI_RECOVERY_RECIPIENT=age1... XIAOHEI_RELEASE_KEY_ALIAS=... XIAOHEI_RELEASE_STORE_PASS=... %s keystore.p12 output-dir\n' "$0" >&2
  exit 2
}
[[ "$recipient" == age1* ]] || { printf 'FAIL recovery recipient is not a native age recipient\n' >&2; exit 1; }

work_dir="$(mktemp -d /tmp/xiaohei-signing-recovery.XXXXXX)"
trap 'rm -rf "$work_dir"' EXIT
mkdir -p "$output_dir"
cp "$keystore" "$work_dir/xiaohei-release.p12"
LC_ALL=C keytool -exportcert -rfc -keystore "$keystore" -storepass "$store_pass" \
  -alias "$alias_name" -file "$work_dir/xiaohei-release-certificate.pem" >/dev/null

tar -C "$work_dir" -czf "$work_dir/recovery.tar.gz" \
  xiaohei-release.p12 xiaohei-release-certificate.pem
encrypted="$output_dir/xiaohei-signing-recovery.tar.gz.age"
age -r "$recipient" -o "$encrypted" "$work_dir/recovery.tar.gz"

keystore_sha="$(shasum -a 256 "$keystore" | awk '{print $1}')"
certificate_sha="$(LC_ALL=C keytool -printcert -file "$work_dir/xiaohei-release-certificate.pem" 2>/dev/null | \
  sed -n 's/^[[:space:]]*SHA256: //p' | tr -d ':' | tr '[:upper:]' '[:lower:]')"
encrypted_sha="$(shasum -a 256 "$encrypted" | awk '{print $1}')"
[[ "$certificate_sha" =~ ^[0-9a-f]{64}$ ]] || { printf 'FAIL cannot derive certificate SHA-256\n' >&2; exit 1; }
python3 - "$output_dir/recovery-manifest.json" "$keystore_sha" "$certificate_sha" "$encrypted_sha" <<'PY'
import datetime
import json
import pathlib
import sys

output, keystore_sha, certificate_sha, encrypted_sha = sys.argv[1:]
record = {
    "schema_version": 1,
    "created_at": datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat(),
    "encrypted_file": "xiaohei-signing-recovery.tar.gz.age",
    "encryption": "age/X25519",
    "keystore_sha256": keystore_sha,
    "certificate_sha256": certificate_sha,
    "encrypted_sha256": encrypted_sha,
    "contains_private_key": True,
    "storage_state": "staging-until-copied-to-owner-controlled-offline-media",
}
pathlib.Path(output).write_text(json.dumps(record, indent=2) + "\n", encoding="utf-8")
PY

if [[ -n "$identity_file" ]]; then
  [[ -s "$identity_file" ]] || { printf 'FAIL recovery identity file missing\n' >&2; exit 1; }
  age -d -i "$identity_file" -o "$work_dir/verified.tar.gz" "$encrypted"
  mkdir -p "$work_dir/verified"
  tar -C "$work_dir/verified" -xzf "$work_dir/verified.tar.gz"
  verified_sha="$(shasum -a 256 "$work_dir/verified/xiaohei-release.p12" | awk '{print $1}')"
  [[ "$verified_sha" == "$keystore_sha" ]] || { printf 'FAIL recovery decrypt hash mismatch\n' >&2; exit 1; }
  printf 'PASS encrypted-signing-recovery decrypt_verified=yes certificate_sha256=%s encrypted_sha256=%s storage=staging\n' \
    "$certificate_sha" "$encrypted_sha"
else
  printf 'PASS encrypted-signing-recovery decrypt_verified=no certificate_sha256=%s encrypted_sha256=%s storage=staging\n' \
    "$certificate_sha" "$encrypted_sha"
fi
