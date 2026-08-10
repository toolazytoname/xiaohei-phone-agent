# Semantic app adapters

`TOOL-011` defines a closed revision-1 registry of 15 reviewed package targets. It supports only exact-label selection, one scroll, or one back action. The registry revision is the compatibility revision of Xiaohei's adapter contract; it does **not** assert that a particular installed app UI version has been verified.

Failure is explainable and fail-closed: unknown package, unsupported adapter revision, unsupported action, or denied target. A changed page with no exact label must stop rather than guess, and generic approvals such as “Allow” or “Next” remain denied.

Automatic text entry, app-private APIs, message sending, and generic confirmation controls are absent. The registry has no gateway caller and does not collect UI trees, screenshots, or app content. A per-app/version/device matrix remains necessary before a descriptor can be treated as device-validated.

## 语义应用适配器

`TOOL-011` 定义了 15 个受审包名的封闭 revision-1 注册表，只支持精确标签选择、一次滚动或一次返回。该 revision 是小黑适配器契约的兼容版本，**不**代表某个已安装 App 的 UI 版本已经验证。

失败会以未知包名、适配器 revision 不支持、动作不支持或目标被拒绝的形式明确返回，并且全部 fail-closed。页面变化而找不到精确标签时必须停止，不能猜测；“允许”“下一步”等通用批准仍被拒绝。

注册表没有自动文本填入、App 私有 API、消息发送或通用确认控制，也没有网关调用方，不采集 UI 树、截图或 App 内容。在设备上将任一描述符视为已验证之前，仍必须完成按 App/版本的真机矩阵。
