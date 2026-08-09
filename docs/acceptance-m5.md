# M5 visible Phone Agent acceptance — foundation slice

Date: 2026-08-09  
Device: OnePlus 8T / Android 14

## Proven on device

- The optional Accessibility service was enabled for the run and reported `CONNECTED`. It captures bounded `snapshot.v1` semantic trees (maximum 160 nodes and depth 14); password nodes become `[REDACTED]`. It does not capture screenshots.
- A visible one-step task opened Android Settings, found the exact semantic label “Network & internet”, clicked its clickable node/ancestor, and re-observed a new snapshot. Trace: one step, `ok=true`, before version 1, after version 2, package `com.android.settings`.
- The first run exposed an over-broad sensitive-text denial and was safely stopped before clicking. The policy was narrowed to distinguish a sensitive target/active credential form from a harmless settings-index entry, then the valid task passed.
- A target “Enter verification code” was denied before any action. A missing target performed one bounded recovery; the user then used global stop while it was pending, producing `task=stopped` with no click.
- An OpenAI-compatible local mock was reached through `adb reverse`. It returned one JSON proposal. The UI displayed package, exact label, and explanation and did nothing until explicit confirmation. After confirmation, the local policy executed exactly one click and re-observed successfully.
- Two mock requests were made and no external model or user relay token was used. The reverse tunnel, mock profile, Accessibility grant, and Notification-listener test grant were removed afterward. The OnePlus DSP remained `DETACHED`.

## Enforced bounds

Maximum 8 steps, 60 seconds total, one recovery, duplicate-action guard, package allowlist, sensitive-surface denial, and a global stop are enforced locally. The model cannot call Accessibility directly. Sending, deletion, installation, permission grants, and calls require a separate confirmation class; payment, banking, credentials, passwords, and OTP surfaces are denied.

## Open M5 exit gates

This slice proves the planner/preview/policy/semantic-action/re-observe architecture, not the full milestone. Screenshot fallback, persistent versioned trace export, 10–15 app task coverage, and real multi-step recovery remain. The service is disabled after acceptance and must be explicitly enabled by the user.
