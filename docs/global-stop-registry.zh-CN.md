# 全局停止注册表

`GlobalStopRegistry` 是可见全局停止请求的本地 fan-out 核心。运行资源所有者必须显式登记八个封闭类别之一：语音、DSP、CPU 唤醒、对话、Phone Agent、工具执行、OpenCode 或 root。

停止时，每个已登记所有者只被调用一次。全部确认停止时结果是 `STOPPED`；只要有一个返回 false 或抛异常，结果就是 `STOP_FAILED`，`allResourcesReleased` 仍为 false。注册表不会按名称发现、启动、发信号或杀死任何资源；它拒绝停止后的新登记与重复停止，避免隐式重试循环。

该注册表尚未接入全部 App、通知、语音或微件入口，也不能证明平台资源已归零。UX-005 在完成这些集成和设备证据前保持 `VERIFY`。
