#!/usr/bin/env bash
set -euo pipefail

apk="${1:-}"
[[ -s "$apk" ]] || { printf 'usage: %s /absolute/path/to.apk\n' "$0" >&2; exit 2; }

sdk_root="${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}"
build_tools="${ANDROID_BUILD_TOOLS:-$sdk_root/build-tools/36.0.0}"
for tool in "$build_tools/apksigner" "$build_tools/aapt2" unzip strings shasum; do
  command -v "$tool" >/dev/null 2>&1 || [[ -x "$tool" ]] || {
    printf 'FAIL missing scanner dependency: %s\n' "$tool" >&2; exit 2;
  }
done

"$build_tools/apksigner" verify --verbose "$apk" >/dev/null
bad_path="$(unzip -Z1 "$apk" | awk '/(^\/|(^|\/)\.\.(\/|$)|\\)/ { print; exit }')"
[[ -z "$bad_path" ]] || { printf 'FAIL unsafe ZIP path: %s\n' "$bad_path" >&2; exit 1; }

bad_payload="$(unzip -Z1 "$apk" | awk 'tolower($0) ~ /\.(sh|exe|dll|dylib|jar|keystore|jks|pem|key)$/ { print; exit }')"
[[ -z "$bad_payload" ]] || { printf 'FAIL unexpected executable/secret payload: %s\n' "$bad_payload" >&2; exit 1; }

bad_secret="$(strings "$apk" | grep -E -m1 'sk-[A-Za-z0-9_-]{20,}|AKIA[0-9A-Z]{16}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' || true)"
[[ -z "$bad_secret" ]] || { printf 'FAIL possible embedded credential\n' >&2; exit 1; }

badging="$($build_tools/aapt2 dump badging "$apk")"
if [[ "${XIAOHEI_EXPECT_RELEASE:-0}" == 1 ]] && grep -q 'application-debuggable' <<<"$badging"; then
  printf 'FAIL release candidate is debuggable\n' >&2; exit 1
fi

permissions="$($build_tools/aapt2 dump permissions "$apk")"
allowed='android.permission.RECORD_AUDIO|android.permission.INTERNET|android.permission.CAMERA|android.permission.POST_NOTIFICATIONS|android.permission.FOREGROUND_SERVICE|android.permission.FOREGROUND_SERVICE_MICROPHONE|io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT'
unexpected="$(sed -n "s/^uses-permission: name='\([^']*\)'.*/\1/p" <<<"$permissions" | grep -Ev "^($allowed)$" || true)"
[[ -z "$unexpected" ]] || { printf 'FAIL unexpected permission(s):\n%s\n' "$unexpected" >&2; exit 1; }

xmltree="$($build_tools/aapt2 dump xmltree "$apk" --file AndroidManifest.xml)"
for gate in android.permission.BIND_VOICE_INTERACTION android.permission.BIND_SPEECH_RECOGNITION \
    android.permission.BIND_QUICK_SETTINGS_TILE android.permission.BIND_NOTIFICATION_LISTENER_SERVICE \
    android.permission.BIND_ACCESSIBILITY_SERVICE io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT; do
  grep -q "android:permission.*\"$gate\"" <<<"$xmltree" || {
    printf 'FAIL missing exported-component permission gate: %s\n' "$gate" >&2; exit 1;
  }
done

native="$(unzip -Z1 "$apk" | grep -E '^lib/[^/]+/[^/]+\.so$' | sort || true)"
native_count="$(grep -c . <<<"$native" || true)"
apk_sha="$(shasum -a 256 "$apk" | awk '{print $1}')"
printf 'PASS apk_static_scan sha256=%s signed=yes zip_paths=safe credential_hits=0 permissions=allowlisted exported_gates=present native_libraries=%s\n' \
  "$apk_sha" "$native_count"
printf '%s\n' "$native"
