# Xiaohei Android app / 小黑 Android 应用

This directory will contain the user-facing product shell: onboarding, permission explanations, independent wakeword/model/remote controls, action preview, confirmation, state, and redacted local history.

这里将承载用户可见的产品外壳：首次引导、权限说明、相互独立的唤醒词/模型/远端控制、动作预览、确认、状态和本地脱敏历史。

`xiaohei-android/` now contains the first runnable vertical slice: an explicit base-mode wake event is routed to the low-risk public **Open Gallery** intent, with the visible `OFF / ARMING / ARMED / TRIGGERED / LISTENING / THINKING / ACTING / ERROR` state surface. Its optional short command session is bounded and releases its recognizer after result/error; it is not a DSP claim.

`xiaohei-android/` 现包含第一条可运行纵切：基础模式中的明确唤醒事件会经由低风险、公开的“打开相册”Intent；界面展示 `OFF / ARMING / ARMED / TRIGGERED / ERROR`。它不宣称 DSP，也不录音。

`oneplus8t-dsp-companion/` 是单独的 OnePlus 8T 本机 SoundTrigger 预检组件。它只在匹配 ROM 的本地 `system_ext` 权限白名单下工作，当前不会加载模型或启动识别。
