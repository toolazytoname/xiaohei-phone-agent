# 离线模型再分发审查

状态：截至 2026-08-09，**未批准公开二进制再分发**。本页是发布门禁，不是法律意见。

## 审查输入

| 输入 | 固定构建输入 | 上游证据 | 发布结论 |
|---|---|---|---|
| sherpa-onnx runtime | `1.13.4`，来自官方 Android APK 输入 | Next-gen Kaldi/sherpa-onnx 代码为 Apache-2.0。 | Runtime 源码许可应与模型权重权利分开跟踪。 |
| 中文流式 ASR | `sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23` | 对应 Hugging Face 模型卡标注 `apache-2.0`；sherpa 文档说明它训练于 WenetSpeech。 | 不能只据模型卡推断最终二进制再分发已批准；须保存精确 card/revision 并完成权利人审查。 |
| 中文 KWS | `sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01` | sherpa 的 KWS Android 页面说明代码为 Apache-2.0，**但不同框架模型必须检查所选模型许可**。WenetSpeech 项目要求先阅读许可并申请访问。 | **阻断：**在权利人对该精确模型记录明确权利，或替换为已批准模型前，不得公开再分发。 |

## 强制构建策略

当 `release` variant 嵌入 ASR 或 KWS 资产时，`build.sh` 会拒绝构建，除非发布操作人显式设置 `XIAOHEI_MODEL_REDISTRIBUTION_APPROVED=1`。这不是“自动获得许可”的开关，而是对“精确模型权利审查已完成并在公开源码树外记录”的有意确认。

源码/ debug 验收构建仍可用于私有设备验证，但绝不是公开二进制发布路径。

## 批准记录必须包含

公开 release 设置批准变量前，私有记录必须包含：

1. 精确资产名、SHA-256、上游 URL、版本/revision 与取得日期。
2. 模型权重许可及训练数据/数据集限制。
3. 商业使用、修改、署名、notice 和再分发分析。
4. 批准人、日期、范围，以及替换/撤回方案。
5. 最终 SBOM 与公开 notice 文本。

已查阅的官方来源：

- [sherpa-onnx 模型文档](https://k2-fsa.github.io/sherpa/onnx/pretrained_models/index.html)
- [ASR 模型卡](https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23)
- [KWS Android 许可提示](https://k2-fsa.github.io/sherpa/onnx/kws/apk.html)
- [WenetSpeech 项目](https://github.com/wenet-e2e/wenetspeech)
