# FVC-110: automated and static acceptance partial

Date: 2026-08-11 · Scope: deterministic code/build evidence plus one independent AOSP source-only mock lifecycle.

- The Android unit suite covers explicit ASR profiles/providers, legal voice-turn transitions, duplicate/late transition rejection, Conversation budgets, controls, privacy denials, audio leases, focus interruptions, and failure-recovery boundaries.
- Repository verification checks Conversation's zero-action boundary, no public credential/private-path artifacts, transcript-free release logging boundaries, TTS lifecycle, and independent channel configuration.
- Model-bearing private builds compile and install with matching signing identity; source-only builds are kept separate and are never installed over that private package.
- The current model-free `0.2.0-alpha.3 (4)` revision was fresh-installed on an independent Android 14 ARM64 AVD. After normal onboarding, independent-channel setup, and `adb reverse` to a fixed host SSE mock, two preregistered text turns displayed `1/6` and `2/6`. The second mock returned success only for the role sequence `system,user,assistant,user`, proving the UI used one shared bounded context.
- The mock recorded no request body and used neither a real model nor a user token. After the second turn, AudioFlinger reported `No active record clients` and logs showed no package Fatal/ANR. The run then used `am force-stop`, removed reverse, uninstalled the package, deleted the emulator UI XML, stopped the mock, and shut down the AVD. This proves zero resources after force-stop, not a human click on Stop or any voice/hearing behavior.

Not closed by this evidence: real OnePlus L2 two-turn conversation/cancel/offline failure/global stop, a complete L3 DSP conversation path, and real profile restoration after those tests.
