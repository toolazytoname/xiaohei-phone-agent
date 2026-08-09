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
- The latest candidate completed a real two-step Settings task: `Network & internet`, re-observe, then `Internet`. Two success records contain snapshot transitions `1→2` and `3→4`, ending in Settings `SubSettings`.
- A bounded 256 KiB `agent-trace.v1.jsonl` export stores no tree, screenshot, prompt, or token. Two success rows and one deny row parsed as JSON; the sensitive target became `[REDACTED_POLICY_TARGET]`. The trace was cleared and Accessibility revoked afterward.
- Agent execution is now package-bound: the expected package must match the active Accessibility event and root snapshot. A real cross-app Calculator task clicked `1` exactly once with package `com.android.calculator2` and a successful trace. A race redirected to Settings produced zero clicks and was then stopped by disabling the service.
- Local policy is now tighter: even in an allowlisted app, generic Allow, Confirm, Next, Continue, and Accept controls are never clicked automatically. Visible permission and account/sign-in surfaces require user handoff; payment, transfer, card, OTP, and password surfaces remain denied. Unit cases cover a Contacts notification prompt, generic Next, and a payment-amount surface.
- Safe-entry testing found that Aperture's “Album” handed off to the Termux file receiver on this OnePlus build. It is no longer advertised as a stable safe entry. The executor now checks the package again after every click: a handoff records `package_changed` and stops immediately rather than counting as success or continuing.
- Calculator testing also found the same text in both formula output and the digit button. Exact-node lookup now prefers a match with a clickable ancestor, so passive output cannot shadow an actionable control; a device retest with `1` already in the formula passed. The temporary harness could not stably handle Android popup/accessibility state and is not shipped; future matrix cases use visible manual semantic selection or a formal UI automation framework.
- The package allowlist is exact rather than prefix-based: unreviewed `com.google.android.*` or `org.lineageos.*` packages cannot inherit access merely from their vendor namespace. Each supported package is listed explicitly and remains subject to label, surface, package-after-action, timeout, and stop checks.
- A consent-gated visual recovery is now available when semantic recovery cannot find a low-risk target. It is reachable only through the visible Agent notification while a safe package-bound task is pending; Android 13+ notification permission is explicitly shown/requested in the Agent UI. The service captures one screenshot into memory, immediately stops the task, and the next visible Agent screen tells the user to inspect it and provide a new exact low-risk label. It never uploads the image, writes it to disk, adds it to `agent-trace.v1`, or uses it for automatic clicks.
- On an independent Android 14 AOSP ARM64 emulator, a DocumentsUI task with a missing label entered the one-recovery handoff. The notification action's protected service intent was exercised by the acceptance harness: capture succeeded, the task stopped before any action, and the Agent page displayed the transient local-preview warning. App-private storage contained only the existing JSONL trace; no image file or screenshot trace content was found. The actual notification tap remains a normal user interaction, not an automatic action.

## Enforced bounds

Maximum 8 steps, 60 seconds total, one recovery, duplicate-action guard, package allowlist, sensitive-surface denial, and a global stop are enforced locally. The model cannot call Accessibility directly. Sending, deletion, installation, permission grants, and calls require a separate confirmation class; payment, banking, credentials, passwords, and OTP surfaces are denied.

## Open M5 exit gates

This slice proves planning, preview, policy, multi-step semantic execution, re-observation, redacted trace export, and consent-gated local visual recovery, not the full milestone. The 10–15 app task matrix and real multi-step recovery remain. The service is disabled after acceptance and must be explicitly enabled by the user.
