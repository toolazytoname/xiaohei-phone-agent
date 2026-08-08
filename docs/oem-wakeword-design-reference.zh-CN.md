# 原厂唤醒产品设计参考：保留什么，替换什么

[English](oem-wakeword-design-reference.md)

OnePlus 原厂“小布小布”客户端是本项目在 OnePlus 8T 上验证 DSP 低功耗路径的重要参考。它不是可直接移植的依赖；它提供的是经过真实手机产品打磨的交互分层。

## 应借鉴的设计

| 原厂能力 | 小黑的对应设计 | 原因 |
|---|---|---|
| 模型列表、启用状态、置信度阈值 | `Wakeword Profile`：词、后端、阈值、设备 profile、状态 | 用户必须知道当前到底在监听什么 |
| 每个模型绑定一个 Android `Intent` | `Action Adapter` + 明确动作预览 | “小布小布 → 打开相册”可成为小黑第一条安全纵切 |
| 命中成功/失败提示音与亮屏 | 可配置、可关闭的反馈策略 | 命中必须可感知；不能默默执行 |
| 无 Look-Ahead Buffer 时自动重新监听 | Broker 在事件消费后回到 `ARMED` | 一次命中不能让服务悄悄失效 |
| 用户训练与关键词设置入口 | 后续的“小黑小黑”模型 enrollment 页面 | 模型来源、训练状态和删除能力必须用户可见 |
| 识别事件与动作启动分开 | `Wakeword Broker → Policy → Android Action` | DSP 命中不是执行任意操作的授权 |

## 不能直接复用的实现

| 原厂实现 | 原因 | 小黑替代方案 |
|---|---|---|
| `sharedUserId=android.uid.system` 与 persistent app | Android 14/不同 ROM 上有系统稳定性和安全风险 | 独立 package、最小权限、显式启停 |
| Oplus wrapper / hidden framework API | ROM 专属、升级脆弱 | AOSP 公开入口或受控设备 profile 的 Adapter |
| 原厂 daemon、跨用户 observer、锁屏强制操作 | 职责过宽，普通产品不应默认拥有 | 仅在用户授权且平台支持时使用系统公开能力 |
| 原厂 APK、SVA 模型、私有库 | 不能公开再分发 | 用户从合法 OTA 本地提取；公开仓仅放源码和校验流程 |

## 第一条产品链路

```text
“小布小布”DSP 命中（后续替换“小黑小黑”）
             ↓
脱敏 wakeword-event.v1
             ↓
低风险策略：打开相册，不需要确认
             ↓
公开 Android Intent
             ↓
显示结果并回到 ARMED
```

当前 Android 纵切已经先实现同一后半段：用户明确启用基础模式后，手动事件会触发“打开相册”。它不录音、不申请联网或无障碍权限，也不宣称已经启用 DSP。
