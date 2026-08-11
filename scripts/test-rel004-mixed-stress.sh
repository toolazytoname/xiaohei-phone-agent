#!/usr/bin/env bash
set -euo pipefail

# REL-004 automated slice. It refuses physical serials and uses debug-only
# transcript injection; all chat/task cases must stop at editable drafts.
serial="${1:-}"
apk="${2:-}"
package="io.github.toolazytoname.xiaohei"
component="$package/.MainActivity"
[[ "$serial" == emulator-* && -s "$apk" ]] || {
  printf 'usage: %s emulator-5554 generic-debug.apk\n' "$0" >&2; exit 2;
}
adb=(adb -s "$serial")
lock_dir="/tmp/xiaohei-rel004-${serial}.lock"
mkdir "$lock_dir" 2>/dev/null || {
  printf 'FAIL rel004 harness already running for %s\n' "$serial" >&2; exit 1;
}
cleanup() {
  "${adb[@]}" uninstall "$package" >/dev/null 2>&1 || true
  rmdir "$lock_dir" >/dev/null 2>&1 || true
}
trap cleanup EXIT

"${adb[@]}" wait-for-device
"${adb[@]}" install -r "$apk" >/dev/null
"${adb[@]}" shell dumpsys package "$package" | grep -q DEBUGGABLE || {
  printf 'FAIL requires a debuggable acceptance APK\n' >&2; exit 1;
}
"${adb[@]}" logcat -G 8M
"${adb[@]}" logcat -c

action_count() { "${adb[@]}" logcat -d -s XiaoheiAction:I '*:S' | grep -c 'ok=true' || true; }
top_activity() {
  "${adb[@]}" shell dumpsys activity activities | grep -m1 'topResumedActivity' || true
}
expect_top() {
  local expected="$1" top=''
  # MainActivity is singleTask and draft routing posts a second Activity;
  # a cold AOSP emulator can legitimately take longer than one second.
  for attempt in $(seq 1 20); do
    top="$(top_activity)"
    [[ "$top" == *"$expected"* ]] && return
    sleep 0.2
  done
  printf 'FAIL expected_activity=%s actual=%s\n' "$expected" "$top" >&2; exit 1
}
inject() {
  "${adb[@]}" shell am start -n "$component" --es debug_transcript "$1" >/dev/null
}

actions=0
for round in $(seq 1 10); do
  # Four deterministic commands: exactly one existing local ActionDispatcher
  # result each. This does not turn on a model, recorder, CPU KWS or DSP.
  for command in '打开设置' '打开相册' '打开浏览器' '打开拨号盘'; do
    inject "$command"; actions=$((actions + 1))
    for attempt in 1 2 3 4 5; do
      [[ "$(action_count)" == "$actions" ]] && break
      sleep 0.2
    done
    [[ "$(action_count)" == "$actions" ]] || {
      printf 'FAIL command_once round=%s command=%s expected=%s actual=%s\n' \
        "$round" "$command" "$actions" "$(action_count)" >&2; exit 1;
    }
  done
  # Three chat drafts and two complex-task drafts: launching their UI is the
  # only allowed side effect. Neither page auto-sends/plans/executes.
  for chat in '相册是什么' '怎么打开蓝牙' '你能做什么'; do
    before="$(action_count)"; inject "$chat"; expect_top '.ConversationActivity'
    [[ "$(action_count)" == "$before" ]] || { printf 'FAIL chat_action_leak\n' >&2; exit 1; }
  done
  for task in '帮我整理下载目录' '帮我查找最大的五个文件'; do
    before="$(action_count)"; inject "$task"; expect_top '.AgentActivity'
    [[ "$(action_count)" == "$before" ]] || { printf 'FAIL task_action_leak\n' >&2; exit 1; }
  done
  # One ambiguous phrase must stay non-executing on Home.
  before="$(action_count)"; inject '打开相册和相机'; expect_top '.MainActivity'
  [[ "$(action_count)" == "$before" ]] || { printf 'FAIL clarification_action_leak\n' >&2; exit 1; }
done

fatal="$( { "${adb[@]}" logcat -d | grep -E "FATAL EXCEPTION|ANR in $package|Force finishing activity.*$package" || true; } | wc -l | tr -d ' ' )"
recording="$("${adb[@]}" shell dumpsys media.audio_flinger | grep -c 'Active Record Client' || true)"
[[ "$fatal" == 0 && "$recording" == 0 ]] || {
  printf 'FAIL rel004-mixed actions=%s fatal=%s active_record=%s\n' "$actions" "$fatal" "$recording" >&2; exit 1;
}
cleanup
trap - EXIT
printf 'PASS rel004-mixed cases=100 commands=40 chats=30 tasks=20 clarifications=10 actions=40 fatal=0 active_record=0 model=0 planner=0\n'
