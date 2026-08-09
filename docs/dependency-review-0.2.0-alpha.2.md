# Dependency review — 0.2.0-alpha.2

Date: 2026-08-09

The exact local combined APK identifies ONNX Runtime `1.27.0` from the embedded arm64 binary. Native hashes were:

- `libonnxruntime.so`: `994848008526a934dfb579ac773b00e5867929234852b061005d45aacaee9533`
- `libsherpa-onnx-jni.so`: `4ebe7c5c52a27f22cf713ffe37867da7e44dea921b8c1428048993074b0fcb6c`

Pinned build inputs:

- sherpa-onnx 1.13.4 Chinese 14M ASR APK: `7d5680a287e73c6095105ef79d0e38c070a36c78b961a7f5c2b353fc166f922d`
- sherpa-onnx 1.13.4 Chinese KWS APK: `1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f`

The reproducible OSV query returned zero known entries for PyPI coordinates `onnxruntime@1.27.0` and `sherpa-onnx@1.13.4` on this date. Coordinate mapping from Android native bundles to PyPI is an engineering inference; zero OSV entries does not mean zero vulnerabilities. The exact APK also passed the local payload, permission, exported-component, credential-pattern, and native-inventory gate. Upstream ONNX Runtime release notes describe ongoing security hardening, and sherpa-onnx is the upstream runtime source.

Sources: [OSV API](https://google.github.io/osv.dev/api/), [ONNX Runtime releases](https://github.com/microsoft/onnxruntime/releases), [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx).

Open gates: independent malware-engine review, production signing provenance, and explicit redistribution approval for both Chinese model bundles. These remain release blockers.
