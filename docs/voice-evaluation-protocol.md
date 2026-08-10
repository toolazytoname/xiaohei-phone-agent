# Xiaohei Human Mandarin Voice Evaluation Protocol v1

[简体中文](voice-evaluation-protocol.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status: public protocol for `VOICE-006`. Never commit raw audio, literal transcripts, speaker identity, or private commands.

## Purpose

Measure post-wake short commands and open conversational utterances in a reproducible, redacted way. Results select models and thresholds; they are not training data and do not replace physical power qualification.

## Sampling design

Collect 30–50 real Mandarin utterances:

| Dimension | Minimum coverage |
|---|---|
| Speakers | Three or more adults, recorded only as `S1`, `S2`, `S3` |
| Distance | 30 cm, 1 m, 2 m |
| Environment | Quiet indoor, ordinary indoor noise, mild outdoor noise |
| Commands | Gallery, settings, Bluetooth, camera, browser, stop, unread query, ambiguity/refusal |
| Open questions | At least ten daily questions that request no action |
| Negatives | At least ten near words, ordinary speech, or non-wake phrases |

Never collect phone numbers, real contacts, addresses, OTPs, passwords, chat text, private photo titles, or work secrets.

## Run rules

1. Fix the candidate APK, build hash, wake/ASR profile; record versions but never tokens or endpoints.
2. Say or play each case once. Do not repeat the same failure fingerprint unless environment, distance, profile, or build changed.
3. Record wake state, volume, network, and lock state first. Do not run during a power-sampling window.
4. Retain only the redacted summary. If audio is temporarily retained, delete it after the test with speaker confirmation.
5. Use safe surrogate commands only. Do not test unattended sending, payment, OTP, or root modification.

## Redacted result sheet

Keep this table privately or in a restricted issue attachment; public reports contain aggregates only.

| Case | Speaker | Distance | Environment | Category | Expected intent | ASR exact? | Routed safely? | Latency ms | Result | Notes |
|---|---|---|---|---|---|---:|---:|---:|---|---|
| C01 | S1 | 30cm | quiet | command | OPEN_GALLERY | yes/no | yes/no | number | pass/fail | no transcript |

`Routed safely` means correct action, correct clarification, or safe refusal. A transcription that triggers a different action always fails.

## Pass criteria and reporting

- Report command safety, open-question intelligibility, false wakes/misactions, and P50/P95 latency separately.
- A high-risk misaction, recorder residue, crash, or failed global stop fails the candidate build.
- Missing a predeclared threshold stays `VERIFY`; do not tune and publish only the passing cases.
- Public reports include device class, version, bucket counts, aggregate metrics, limits, and deletion confirmation only.

