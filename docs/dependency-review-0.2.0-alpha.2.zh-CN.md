# 依赖复核 — 0.2.0-alpha.2

日期：2026-08-09

精确的本地组合 APK 从 arm64 二进制中识别出 ONNX Runtime `1.27.0`。Native 哈希：

- `libonnxruntime.so`：`994848008526a934dfb579ac773b00e5867929234852b061005d45aacaee9533`
- `libsherpa-onnx-jni.so`：`4ebe7c5c52a27f22cf713ffe37867da7e44dea921b8c1428048993074b0fcb6c`

固定构建输入：

- sherpa-onnx 1.13.4 中文 14M ASR APK：`7d5680a287e73c6095105ef79d0e38c070a36c78b961a7f5c2b353fc166f922d`
- sherpa-onnx 1.13.4 中文 KWS APK：`1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f`

可复现 OSV 查询在当天对 PyPI 坐标 `onnxruntime@1.27.0` 和 `sherpa-onnx@1.13.4` 均返回 0 条已知记录。Android native 包映射到 PyPI 坐标属于工程推断；OSV 为 0 绝不等于没有漏洞。精确 APK 还通过本地 payload、权限、导出组件、凭据特征和 native 清单门禁。上游 ONNX Runtime Release Notes 持续记录安全加固，sherpa-onnx 仓库是运行时来源。

来源：[OSV API](https://google.github.io/osv.dev/api/)、[ONNX Runtime Releases](https://github.com/microsoft/onnxruntime/releases)、[sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)。

仍缺独立恶意软件引擎复核、正式签名来源证明，以及两个中文模型包的明确再分发批准；这些继续阻断公开 Release。
