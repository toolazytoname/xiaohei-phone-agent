# 小黑 0.2.0-alpha.2 — 内部验收版本

这是本地 debug 签名的内部候选包，不是公开 Release。

相对 alpha.1 的新增内容：

- 增加默认关闭的“小黑小黑”CPU 前台唤醒，与 OnePlus DSP 控件和状态相互独立。
- KWS 与离线中文 ASR 共用一份 sherpa-onnx runtime；上游 APK/模型输入不进入 Git。
- 可见 Phone Agent 支持真实两步执行，每步后重新观察。
- 增加有界脱敏 JSONL 轨迹；不持久化语义树、截图、Prompt、通知正文或 Token。
- 增加双语威胁模型、精确 APK 静态安全门禁，以及同时覆盖 ASR/KWS 输入的 CycloneDX SBOM。
- OnePlus 8T 已验证 code 2→3 升级、普通降级拒绝、显式维护降级、重新升级、事务式卸载和全新安装。
- 首次启动现会先解释基础模式，以及可选的麦克风、通知、无障碍和 OnePlus DSP 能力，不会在说明页请求权限。

尚未满足的发布门禁：第二 Android 设备/profile、真实来电和隔离消息账号、Phone Agent 10–15 App 与截图回退、8–24 小时拔线功耗、正式签名治理、外部依赖/恶意软件复核，以及模型再分发批准。
