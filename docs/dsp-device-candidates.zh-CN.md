# DSP 唤醒候选设备分档

[English](dsp-device-candidates.md)

## 先区分三句话

大家常把下面三件事都说成“这台手机支持 DSP”：

1. 硬件里有低功耗音频 DSP。
2. 原厂助手可以通过 DSP 唤醒。
3. 小黑能在当前 ROM 加载兼容模型并收到回调。

对小黑 DSP 后端真正有意义的是第三条。AOSP 明确说明 SoundTrigger 模型是厂商与客户端之间不透明的“隐藏契约”，这类应用本来就偏向厂商提供；Android 的 SoundTrigger 管理和 hotword 采集权限也属于 system/privileged/role 级别。因此“芯片里有 DSP”只是很弱的证据。

下面分档是工程先验，不是经过大量样本校准的百分比。任何新硬件/ROM 组合在完成真实 `load → start → 声学触发 → callback → unload` 和干净回滚前，都只能叫候选设备。

## 候选分档

| 档位 | 候选设备 | 原厂唤醒 | 小黑复用已有厂商模型 | 自定义“小黑小黑” | 判断依据 |
|---|---|---|---|---|---|
| S — 已实证 | 当前 OnePlus 8T `KB2000` + LineageOS 21 profile | 已实证 | 已用本地提取的原厂 SVA 4 词实证 | 未实证 | 息屏 ADSP/LPI 声学回调置信度 99，干净回滚通过 |
| A — 下一台成功率最高 | OnePlus 8、8 Pro，以及固件匹配的其他 8T 版本（SM8250 家族） | 证据很强 | 高概率，仍需逐台核对 OTA/ABI | 未知，明显更难 | 与已通过的 8T 共用 OnePlus SM8250 公共平台，公共 blob 清单含 Kona SoundTrigger 与 Qualcomm SVA 库 |
| A− — 大概率 | OnePlus 9、9 Pro（SM8350） | 证据很强 | 高到中等概率 | 未知 | 同厂商技术延续；公共 blob 含 CNN、RNN、VOP 与 ListenSoundModel，但 SoC/HAL 代际已变化 |
| B+ — 值得研究 | 小米 SM8250，例如 Mi 10（`umi`）、POCO F2 Pro/Redmi K30 Pro（`lmi`）；SM8350 例如 Mi 11（`venus`） | 平台证据很强 | 中等概率 | 中低概率 | Lineage 公共树含 SoundTrigger/SVA 资产，但小米客户端、模型、权限、配置和 SELinux 与 OnePlus 不同 |
| B — 有能力但改造较多 | OnePlus 7 系列（SM8150）；OnePlus 11/12 系列（SM8550/SM8650） | 平台证据很强 | 中等概率 | 中低概率 | 7 系是更早的 SVA/enrollment；11/12 是更新的 64 位 vendor 库代际，已验证 bridge 不能直接复用 ABI |
| C — 原厂能用，第三方链路封闭 | Pixel 6/6 Pro Tensor 参考；支持 Hey Google/Bixby 的 Galaxy 设备 | 原厂词可用 | 低概率 | 没有 OEM/system 合作时低概率 | Pixel 有自己的 `gs101` STHAL/Google enrollment；三星公开支持 Bixby 唤醒，但都不等于公开自定义模型契约 |
| C− — 仅凭硬件猜测 | 其他带原厂常驻助手、但没有检查固件的 Snapdragon 旗舰 | 通常可用 | 低到未知 | 低到未知 | Qualcomm/Hexagon 名称不能证明模型、插件、权限与客户端契约 |
| D — 当前 ROM 肯定走不了 | 模拟器；只有 AOSP fake STHAL；无 vendor SoundTrigger module；HAL 没有可用 model slot | 无硬件链 | 不可用 | 不可用 | 当前系统没有可连接的硬件 SoundTrigger 路径 |
| D — 当前方法肯定装不进去 | 锁 BL 的原厂系统，小黑既拿不到 Assistant role/system 权限，也没有公开兼容模型路径 | 原厂仍可能可用 | 当前方法不可用 | 当前方法不可用 | OEM 私有链能工作，但小黑无法加入；A 层主动唤起仍然可用 |

OnePlus 8T 的“已实证”目前特指原厂词和这套软件 profile，并不等于可公开分发的一键 APK，也没有证明自定义“小黑小黑”。

## 为什么优先 OnePlus 8/9 系

- LineageOS 的 OnePlus 8（`instantnoodle`）、8 Pro（`instantnoodlep`）和 8T（`kebab`）设备仓都依赖 `android_device_oneplus_sm8250-common`。
- 该 SM8250 公共 blob 清单包含 `sound_trigger.primary.kona.so`、`libcapiv2svacnn.so`、`libcapiv2vop.so` 和 `liblistensoundmodel2.so`。
- OnePlus 9（`lemonade`）和 9 Pro（`lemonadep`）共用 SM8350 公共树，其 blob 清单包含 CNN、RNN、VOP 与 ListenSoundModel。

这减少了若干未知数，但仍不能跳过模型与客户端兼容检查。因此若增加第二台测试机，优先 8 或 8 Pro，而不是马上跨 OEM。

## 为什么 Pixel、三星不是第一批小黑 DSP 目标

- Pixel 6/6 Pro 设备树含 Google `HotwordEnrollment...FUSIONPro` 和 `sound_trigger.primary.gs101.so`，证明厂商 hotword 栈存在，但不证明小黑可以提供任意模型。
- 三星官方明确支持兼容 Galaxy 设备的 Bixby 免按键唤醒，证明原厂功能存在；模型格式、enrollment、权限和系统客户端仍由三星控制。

它们很适合借鉴成熟交互，却不适合作为我们复用 Qualcomm SVA 工作的第一批目标。

## 六道只读门禁

按顺序判断，遇到硬条件缺失就停止：

1. **Framework：**存在 `soundtrigger` 与 `soundtrigger_middleware` 服务。
2. **真实 HAL：**middleware 能列出非 fake 厂商 module，并有可用 model/phrase 限额。
3. **低功耗配置：**vendor/ODM 声明 ADSP/DSP execution、LPI 或 hardware-hotword capture。
4. **算法闭包：**同一固件家族里存在匹配的模型解析库、二阶段插件、模型、vendor UUID 和 ABI。
5. **权限链：**小黑能成为系统助手，或被有意识地装成 privileged/system 组件；OEM 封闭且锁 BL 时停止。
6. **运行实证：**真实声音到 Android callback，之后 stop/unload 和重启回滚均成功。

自定义唤醒词生成是独立的第七道门。原厂模型通过，不代表厂商提供合法、可复现的编译/训练链。

## 一手证据

- [AOSP Sound Trigger 架构与厂商隐藏契约说明](https://source.android.com/docs/core/audio/sound-trigger)
- [AOSP AndroidManifest：`MANAGE_SOUND_TRIGGER` 与 hotword 权限](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-qpr2-release/core/res/AndroidManifest.xml)
- [LineageOS OnePlus SM8250 公共私有文件清单](https://github.com/LineageOS/android_device_oneplus_sm8250-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS OnePlus SM8350 公共私有文件清单](https://github.com/LineageOS/android_device_oneplus_sm8350-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Xiaomi SM8250 公共私有文件清单](https://github.com/LineageOS/android_device_xiaomi_sm8250-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Xiaomi SM8350 公共私有文件清单](https://github.com/LineageOS/android_device_xiaomi_sm8350-common/blob/lineage-23.2/proprietary-files.txt)
- [LineageOS Pixel 6/6 Pro（`raviole`）设备树](https://github.com/LineageOS/android_device_google_raviole/tree/lineage-23.2)
- [三星 Bixby Voice wake-up 官方文档](https://www.samsung.com/us/support/answer/ANS10001415/)
