# FVC-030：开放对话 ASR profile 验收

日期：2026-08-11 · 范围：代码与构建门禁，不是人耳准确率结论。

- `COMMAND` 与 `CONVERSATION` 已显式区分；只有前者加载命令热词并使用热词解码。
- `CONVERSATION` 使用无热词 `greedy_search`；未知或大小写不匹配 profile 均拒绝，不回退命令模式。
- profile 从 `VoiceCommandSession` 的受限 extra 传给应用内识别服务和离线引擎；聊天入口不会调用命令归一化或 `CommandRouter`。
- 已保留稳定 provider 标识 `local_command_14m`、`local_conversation_candidate`、`android_system`；它们不会触发下载或自动切换。
- `bash apps/android/xiaohei-android/test.sh`、`bash apps/android/xiaohei-android/build.sh` 以及 `git diff --check` 已通过。

本次构建是无模型开发 APK，不能覆盖 OnePlus 的私有含模型包。尚未进行真人中文开放聊天转写，因此不得宣称准确率通过；这属于 FVC-040/FVC-050 真机门禁。
