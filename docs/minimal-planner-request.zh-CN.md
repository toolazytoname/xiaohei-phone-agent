# 最小规划请求

`MinimalPlannerRequest` 是为未来远端规划适配器预留的固定信封。它的实例字段只有 `action`、`dryRun`、`stepBudget`、`timeoutMs` 和 `catalogVersion`。

其中不包含用户正文、界面数据、本地路径、图片、请求标识或凭据，也没有网络、工具或 Android 执行能力。边界与 `TaskPlanValidator` 共用：1–8 个步骤、1,000–60,000 毫秒。

这不表示远端规划器已经接通。只有真实适配器被证明只传输这个信封后，PLAN-002 才能从 `VERIFY` 变为完成。
