# Roadmap

[简体中文](roadmap.zh-CN.md) · [Detailed delivery checklist](product-delivery-plan.md)

Xiaohei has a portable product track and an advanced wake-backend track. The downloadable product is not blocked on vendor DSP support.

## Portable product track

| Milestone | Outcome | Exit gate |
|---|---|---|
| P0 — Product scaffold | Independent bilingual product repository and public contracts | Repository boundary and CI verification pass |
| P1 — Generic Android alpha | Button/Quick Settings invocation, short ASR command, and “open gallery” | Works on an unrooted declared Android profile; no DSP dependency |
| P2 — Assistant entry | User-selectable assistant/session entry where supported | Runtime capability detection and clear OEM limitation state |
| P3 — Notification assistant | Read notification-level unread state | User-granted access, local summary, no chat crawling |
| P4 — Confirmed reply | Draft and confirm one message reply | Recipient/content preview, explicit confirmation, no bulk background action |
| P5 — Public alpha | Reproducible install, UI, docs, and release artifacts | Fresh-device test, CI, signed APK, checksums, bilingual release notes |

## Advanced wake-backend track

| Milestone | Outcome | Exit gate |
|---|---|---|
| W0 — OnePlus DSP evidence | Stock Chinese phrase reaches ADSP/LPI and Android callback | Completed on OnePlus 8T; clean rollback verified |
| W1 — Backend interface | Manual, assistant, CPU KWS, and DSP implement one capability contract | Unsupported backends cannot activate or block base mode |
| W2 — OnePlus DSP Broker | Minimal Android 14 component with a unique UID | Three cold boots, three start/stop cycles, tested uninstall and recovery |
| W3 — Power qualification | Physically unplugged DSP OFF/ARMED comparison | Three 60-minute A/B runs, no ordinary AudioRecord or persistent AP wakelock, 8–24 hour regression |
| W4 — Custom keyword | Native “Xiaohei” model or clearly labeled CPU fallback | Same power/accuracy/rollback gates as W3 |
| W5 — More devices | A second independently validated hardware/ROM profile | No OnePlus-specific code leaks into the generic core |

Each milestone produces one structured acceptance record. The same failure fingerprint must not trigger repeated acoustic tests, screenshots, or model calls without new evidence.
