# 全局停止注册表

`GlobalStopRegistry` 是可见全局停止请求的本地 fan-out 核心。运行资源所有者必须显式登记八个封闭类别之一：语音、DSP、CPU 唤醒、对话、Phone Agent、工具执行、OpenCode 或 root。

停止时，每个已登记所有者只被调用一次。全部确认停止时结果是 `STOPPED`；只要有一个返回 false 或抛异常，结果就是 `STOP_FAILED`，`allResourcesReleased` 仍为 false。注册表不会按名称发现、启动、发信号或杀死任何资源；它拒绝停止后的新登记与重复停止，避免隐式重试循环。

主页按钮及其状态通知 `global_stop` Intent 对 `MainActivity` 持有的三个所有者使用此注册表：语音、DSP 和 CPU 唤醒。Conversation 登记在途请求/TTS owner；受权工具执行登记取消信号；Phone Agent 仅在任务待处理时登记，并在完成、停止或无障碍服务销毁时释放句柄。Phone Agent owner 复用既有 `stopInternal` 路径，不启动或强杀服务。

OpenCode、root、语音命令/微件入口以及平台级零资源证明仍未独立接线。UX-005 因此保持 `VERIFY`：源码接线不是设备证据，全局停止点击也不能声称已停止未登记运行时。
