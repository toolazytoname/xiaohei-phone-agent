# OnePlus 8T · LineageOS 21 reference profile

This is Xiaohei's first advanced wake-backend reference, not its generic compatibility boundary.

Validated on 2026-08-08:

- Qualcomm SoundTrigger module 0, HAL 2.3, version 257.
- Stock SVA 4 Chinese model loaded and entered ADSP/LPI active state.
- Screen-off acoustic input reached the second-stage RNN at confidence 99 and produced an Android callback.
- Temporary APK, systemless libraries, and the single Magisk probe were removed; baseline services recovered.

Still required before this profile can ship as a supported backend:

- Minimal Android 14 broker with a unique UID.
- Local extraction/build flow for legally held OEM assets; no blob redistribution.
- Three physical-unplug power A/B runs and an 8–24 hour idle regression.
- Cold-boot, start/stop, permission failure, uninstall, and rollback automation.
- A custom “Xiaohei” model or an explicit decision to retain another phrase.

中文：这是小黑首个高级唤醒参考设备，不是通用兼容边界。DSP 声学闭环与干净回滚已经通过，但正式 Broker、合法本地资产提取、物理拔线功耗和长期稳定性尚未完成，因此当前不能作为普通用户可下载后端发布。
