# Root 破坏性请求拒绝

[English](root-destructive-denial.md) · [root 边界](root-capability-boundary.zh-CN.md) · [状态](../STATUS.md)

`ROOT-009` 为原始 root 形态请求增加纵深 fail-closed 拒绝策略。破坏性命令、宽泛/系统/穿越/通配路径和秘密/支付/规避材料分别得到拒绝决定；其余输入也以未知拒绝。它不解析或执行 shell 文本。

固定 root broker 不接收自由命令或路径参数，因此该策略不新增任何表面。它是防止未来 adapter 意外扩权的永久回归语料库；其中没有 `su`、shell、文件系统、网络、模型或设备 API。
