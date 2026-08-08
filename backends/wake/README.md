# Wake backends / 唤醒后端

All invocation sources implement one capability and lifecycle contract. The generic product ships Tier A first; assistant, CPU KWS, and vendor DSP backends are independently detected and enabled.

所有唤起来源实现同一套能力与生命周期契约。通用产品先交付 A 层；系统助手、CPU KWS 和厂商 DSP 后端分别探测、分别启用。

| Backend | Always-on audio | Device-specific | Default |
|---|---:|---:|---:|
| `manual` | No | No | Yes |
| `android-assistant` | System-dependent | OEM behavior | When selected by user |
| `cpu-kws` | Yes, foreground | No | No |
| `vendor-dsp` | DSP front end | Yes | Only on a verified profile |
