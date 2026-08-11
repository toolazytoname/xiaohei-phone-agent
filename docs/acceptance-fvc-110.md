# FVC-110: automated and static acceptance partial

Date: 2026-08-11 · Scope: deterministic code/build evidence only.

- The Android unit suite covers explicit ASR profiles/providers, legal voice-turn transitions, duplicate/late transition rejection, Conversation budgets, controls, privacy denials, audio leases, focus interruptions, and failure-recovery boundaries.
- Repository verification checks Conversation's zero-action boundary, no public credential/private-path artifacts, transcript-free release logging boundaries, TTS lifecycle, and independent channel configuration.
- Model-bearing private builds compile and install with matching signing identity; source-only builds are kept separate and are never installed over that private package.

Not closed by this evidence: clean-AOSP current-revision lifecycle, real OnePlus L2 two-turn conversation/cancel/offline failure/global stop, a complete L3 DSP conversation path, and profile restoration after those tests.
