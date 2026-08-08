# OEM Wakeword Design Reference: what to keep and what to replace

[中文](oem-wakeword-design-reference.zh-CN.md)

The stock OnePlus “Xiaobu Xiaobu” client is an important reference for the validated OnePlus 8T DSP path. It is not a dependency to transplant. Its value is a set of product interaction boundaries shaped by a real phone implementation.

| OEM capability | Xiaohei equivalent | Why it matters |
|---|---|---|
| Model list, enablement, and confidence thresholds | A `Wakeword Profile` with keyword, backend, threshold, device profile, and state | Users must know what is actually listening |
| One Android `Intent` per model | An `Action Adapter` with visible action preview | “Wakeword → Open Gallery” is the first safe vertical slice |
| Success/failure tone and screen feedback | Configurable, disable-able feedback policy | A wake hit must be perceptible, never silent execution |
| Re-arm after a hit without a look-ahead buffer | Broker returns to `ARMED` after event consumption | One hit must not silently disable the assistant |
| Training and keyword settings | Future Xiaohei enrollment surface | Model origin, training state, and deletion must be visible |
| Separate recognition and activity start | `Wakeword Broker → Policy → Android Action` | A DSP hit is not authorization for arbitrary execution |

Do not transplant the system UID/persistent service, Oplus wrappers, daemon/cross-user integration, or proprietary APK/model/libraries. Xiaohei uses an independent package, least privilege, public Android APIs where available, and locally extracted device assets only for a gated profile.

```text
DSP wake hit (“Xiaobu Xiaobu”, later “Xiaohei Xiaohei”)
                 ↓
redacted wakeword-event.v1
                 ↓
low-risk policy: Open Gallery, no confirmation
                 ↓
public Android Intent
                 ↓
visible result and return to ARMED
```
