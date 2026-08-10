# 架构

[English](architecture.md)

## 信任边界

```text
常驻但不采集命令音频          Wakeword Broker
短生命周期敏感音频            Voice Gateway
文本与模型交换                 模型适配器
风险与授权                     Policy Engine
修改设备状态                   Android Actions
用户可见与可控制               小黑 Android App
```

任何组件都不能默默扩大另一个组件的权限。一次唤醒只授权一个短命令会话；模型解释出的命令也不能直接授权高风险动作，必须经过策略和确认。

## 组件

### Wakeword Broker

为 App 按钮、快捷设置、快捷方式/硬件 Intent、被选中的 Android 系统助手、CPU KWS 和厂商 DSP 定义统一的能力接口。只有 DSP 适配器负责 SoundTrigger attach/load/start/stop/unload；CPU KWS 与手动/助手唤起不能被标成 DSP。

### Voice Gateway

命中唤醒后只打开有边界的音频会话，完成 VAD、调用可替换 ASR 并尽快关闭。两条命令之间不保持麦克风录音。

### Policy Engine

把文本转成结构化意图，选择动作适配器，评估风险，需要时请求确认，并保存脱敏决策。模型/Provider 是适配器选择，不构成操作授权。

### Android Actions

按优先顺序使用：公开 Android Intent、通知访问、可见的无障碍交互、明确授权的本地 shell 适配器。冒充私有协议不在产品边界内。

### Android App

展示 `OFF / ARMING / ARMED / LISTENING / THINKING / CONFIRMING / ACTING / ERROR`、权限、当前模型配置、动作预览和本地脱敏历史。唤醒词、模型选择和可选远端控制必须保持为相互独立的控制项。

## 版本化契约

- `wakeword-event.v1.schema.json`：不含原始音频，只描述来源、唤醒词别名、置信度和采集边界。
- `action-request.v1.schema.json`：描述目标、动作、风险、确认、dry-run 和脱敏策略。
- `task-plan.v1.schema.json`：描述绑定请求、1–8 步的 dry-run DAG，以及工具/风险、依赖、幂等与超时边界；它不授予执行权限。
- `confirmation-grant.v1.schema.json`：描述一次性、纯内存的本机手势确认，绑定 task/request/plan、目标/内容摘要和 1–60 秒单调时钟窗口；它不是能力令牌。
- `tool-catalog.v1.schema.json`：描述不可变的受审工具元数据、真实封闭输入/输出 Schema、回滚声明、受众和超时。目录成员资格不等于执行权限，详见[版本化工具目录边界](versioned-tool-catalog.zh-CN.md)。

运行时 payload 可以在内存里含有用户私密信息，但 fixture 和公开验收报告在保存或发布前必须脱敏。

## 跨仓规则

- `android-ai-stack` 通过适配器提供模型配置与 AI Runtime 状态；小黑不能直接读取私有 CC Switch 数据库。
- `android-device-test` 验证 UI 与设备行为，但产品专属 selector 和动作预期留在本仓。
- `pocket-pentest` 提供经授权的设备能力，普通用户运行小黑不应强依赖渗透测试环境。
- `happy-relay-deploy` 是可选项；没有远端 server 时，本地唤醒、策略和 Android 动作仍须可用。
- `oneplus-8t-mobile-lab` 链接通过验收的组合与教材，不 vendoring 本仓源码。

## 兼容规则

通用 Android 应用不能只因为 SoC 家族相似就加载厂商 DSP 私有库。只有匹配明确设备/ROM profile 并通过只读能力检查后，才能选择对应后端；所有常驻后端均不可用时，A 层主动唤起仍必须可用。
