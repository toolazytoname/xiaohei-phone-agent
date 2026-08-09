# Offline-model redistribution review

Status: **not approved for public binary redistribution** as of 2026-08-09. This review is a release gate, not legal advice.

## Inputs under review

| Input | Pinned build input | Upstream evidence | Release conclusion |
|---|---|---|---|
| sherpa-onnx runtime | `1.13.4`, used from official Android APK inputs | Next-gen Kaldi/sherpa-onnx code is Apache-2.0. | Runtime source licensing can be tracked separately from model-weight rights. |
| Chinese streaming ASR | `sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23` | The referenced Hugging Face model card labels this repository `apache-2.0`; sherpa documents that it was trained on WenetSpeech. | Do not infer final binary redistribution approval from the card alone; preserve the exact card/revision and complete an owner review. |
| Chinese KWS | `sherpa-onnx-kws-zipformer-wenetspeech-3.3M-2024-01-01` | sherpa's KWS Android page says code is Apache-2.0 **but models from different frameworks require checking the selected model's license**. The WenetSpeech project directs users to read its license and apply for access. | **Blocked:** no public binary redistribution until an owner records explicit rights for this exact model or replaces it with an approved model. |

## Enforced build policy

`build.sh` refuses a `release` variant that embeds ASR or KWS assets unless the release operator explicitly sets `XIAOHEI_MODEL_REDISTRIBUTION_APPROVED=1`. That is not a magic license switch: it is a deliberate acknowledgement that the exact model-rights review has been completed and recorded outside the public source tree.

Source-only/debug acceptance builds remain useful for private device validation, but are not a public binary distribution path.

## Required approval record

Before setting the approval variable for a public release, maintain a private record containing:

1. Exact asset name, SHA-256, upstream URL, version/revision, and acquisition date.
2. Applicable model-weight license and any dataset/training-data restrictions.
3. Commercial, modification, attribution, notice, and redistribution analysis.
4. Approval owner, date, scope, and a replacement/withdrawal plan.
5. The final SBOM and public notice text.

Official sources consulted:

- [sherpa-onnx model documentation](https://k2-fsa.github.io/sherpa/onnx/pretrained_models/index.html)
- [ASR model card](https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-zh-14M-2023-02-23)
- [KWS Android licensing note](https://k2-fsa.github.io/sherpa/onnx/kws/apk.html)
- [WenetSpeech project](https://github.com/wenet-e2e/wenetspeech)
