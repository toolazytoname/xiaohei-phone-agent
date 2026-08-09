# OnePlus 8T M1 真机验收记录

日期：2026-08-09  
设备：OnePlus 8T（KB2000 / OnePlus8T）  
系统：LineageOS 21 / Android 14  
产品：小黑普通 APK + 独立 UID `system_ext` DSP Companion

## 已通过

- 三次 Assistant Role 修复后的冷启动：每次 `sys.boot_completed=1`，Role holder、`VoiceInteractionService`、小黑 RecognitionService 与 `mBound=true` 均保持。
- 三次产品界面 arm/disarm：每次到达 `ACTIVE`；HAL load/start 成功并进入 LPI；stop 与 unload status 0；middleware 最终 `DETACH`。
- 息屏“小布小布”声学 callback、750 ms 自动 re-arm 与第二次 callback 已通过；DSP 二阶段置信度为 99。
- 离线中文 ASR 将“打开相册”转写为完全匹配文本，启动系统 Photo Picker，完成后恢复 `ARMED`。
- ASR 结束后 audio_flinger 报告 `No active record clients`；DSP disarm 后无 active/loaded model。
- 普通 APK 卸载与同一离线 APK 重装通过；Companion 在整个过程中保持 `DETACHED`。
- 三次连续全链路均通过：确认 `Dozing`/显示关闭后播放“小布小布”，DSP 二阶段置信度 99，Assistant 拉起，离线 ASR 识别“打开手机相册/请打开手机相册”，Photo Picker 动作成功并自动 re-arm；每轮结束均无 active record client。

## 本轮发现并修复

1. 只写 secure settings 不跨重启：改用 Android Assistant Role。
2. 全局 RecognitionService 会恢复为其他应用：小黑显式绑定自有离线 RecognitionService。
3. Activity 调试生命周期不能作为日常服务：改为签名保护的 DSP 前台服务。
4. 锁屏自动 startService 被 Android 14 拒绝：状态读取改为签名保护的只读 ContentProvider。
5. 直接卸载可能留下陈旧 Role holder：回滚脚本先移除 Role 再卸载。
6. Assistant 与维护入口可产生多个 Activity task：主界面改为 `singleTask`，统一复用状态实例。

## 尚未满足的 M1 门禁

- 仍需物理拔掉 USB 后完成 DSP OFF/ARMED 功耗 A/B。
- 120 ms 提示音已实现，仍需一次真机听感确认。
