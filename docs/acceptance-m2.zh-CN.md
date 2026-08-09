# M2 离线短命令验收记录

日期：2026-08-09  
设备：OnePlus 8T / Android 14

## 路由门禁

- 33 条固定中文表达通过纯 Java 路由测试。
- “替我转账”进入 `UNKNOWN`，不产生 Android 动作。
- 导航目标只允许 1–80 字符并经过 URL 编码，不进入 shell。

## 真机动作矩阵

以下动作均经过与语音相同的 `CommandRouter → ActionDispatcher`，debug transcript 仅绕过麦克风：

| 动作 | 结果 |
|---|---|
| 相册/系统 Photo Picker | PASS |
| 系统设置 | PASS |
| Wi‑Fi 设置 | PASS |
| 蓝牙设置 | PASS |
| 相机 | PASS |
| 浏览器 | PASS |
| 拨号盘（未拨出） | PASS |
| 导航目标 | PASS |
| 手电筒开 / 关 | PASS / PASS |
| 媒体音量加 / 减 | PASS / PASS |
| 闹钟 | UNSUPPORTED：当前 ROM 未安装可处理 `SHOW_ALARMS` 的时钟 App |

总计 12 个可用动作，1 个设备能力缺失被明确报告。没有 Accessibility、Root shell、自动拨号或后台提交。

## 日用控制

- 通用模式“一键说话”不要求先手动 ARM。
- 全局停止会取消语音、关闭并释放 DSP、回到 OFF。
- 页面支持滚动并列出命令范围。
- SystemUI 已注册并实际点击“小黑说话”快捷设置磁贴，真机进入可见的 `LISTENING`。
- 界面切换中断时立即停止 ASR，恢复 `ARMED`，并明确提示麦克风已释放。
- “打开相册和相机”被判定为多目标歧义，不执行动作，要求用户一次只说一个目标。
- 脱敏诊断导出只包含版本、Android/设备型号、助手/DSP 状态和 ASR 可用性，不包含转写、Token 或 URL。
- 离线 ASR 不依赖网络状态。

## 30 次真机动作回归

`scripts/test-m2-device-actions.sh` 通过同一 Router/Dispatcher 连续执行三轮、每轮 10 个低风险动作。最终证据为 `actions=30 failures=0`；脚本逐条等待新增的 `ok=true`，缺失或重复都失败。

## 尚未满足的 M2 门禁

- 独立 ASR / Phone Agent 配置页和计划中的前台通知仍待实现。
- 仍需在当前 M2 构建上做一次真实重启恢复，以及真实来电中断；等价的 Activity 中断释放路径已通过。
