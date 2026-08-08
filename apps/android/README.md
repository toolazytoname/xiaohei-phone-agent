# Xiaohei Android app / 小黑 Android 应用

This directory will contain the user-facing product shell: onboarding, permission explanations, independent wakeword/model/remote controls, action preview, confirmation, state, and redacted local history.

这里将承载用户可见的产品外壳：首次引导、权限说明、相互独立的唤醒词/模型/远端控制、动作预览、确认、状态和本地脱敏历史。

`xiaohei-android/` now contains the first runnable vertical slice: an explicit base-mode wake event is routed to the low-risk public **Open Gallery** intent, with the `OFF / ARMING / ARMED / TRIGGERED / ERROR` state surface. It is not a DSP claim and does not record audio.

`xiaohei-android/` 现包含第一条可运行纵切：基础模式中的明确唤醒事件会经由低风险、公开的“打开相册”Intent；界面展示 `OFF / ARMING / ARMED / TRIGGERED / ERROR`。它不宣称 DSP，也不录音。
