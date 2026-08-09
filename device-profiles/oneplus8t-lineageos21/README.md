# OnePlus 8T · LineageOS 21 reference profile

This is Xiaohei's first advanced wake-backend reference, not its generic compatibility boundary.

Validated on 2026-08-08:

- Qualcomm SoundTrigger module 0, HAL 2.3, version 257.
- Stock SVA 4 Chinese model loaded and entered ADSP/LPI active state.
- Screen-off acoustic input reached the second-stage RNN at confidence 99 and produced an Android callback.
- Temporary APK, systemless libraries, and the single Magisk probe were removed; baseline services recovered.

Current product-profile status:

- The unique-UID Android 14 Companion now owns bounded attach/load/start/callback/re-arm/stop/unload/detach. Three cold boots and three screen-off acoustic command chains passed on the physical device.
- Local extraction/build flow for legally held OEM assets; no blob redistribution.
- Three physical-unplug power A/B runs and an 8–24 hour idle regression.
- Public redistribution remains impossible without OEM asset rights. The DSP phrase remains the locally held OEM phrase; “Xiaohei Xiaohei” is currently an independently controlled CPU fallback.

The implementation boundary is now explicit: [DSP Companion contract](dsp-companion-contract.md). The ordinary Xiaohei app stays unprivileged; a local, device-gated companion owns only the SoundTrigger lifecycle and emits a redacted event.

中文：这是小黑首个高级唤醒参考设备，不是通用兼容边界。正式 Companion、三轮冷启动、DSP 声学闭环和干净回滚已通过；但 OEM 资产再分发权、物理拔线功耗和长期稳定性尚未完成，因此仍不能作为普通用户可下载后端发布。
