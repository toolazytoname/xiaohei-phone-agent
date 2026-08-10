# Local-small-model guidance

[简体中文](local-small-model-guidance.zh-CN.md) · [Offline FAQ](conversation-offline-faq.md) · [Model channels](../README.md)

OC-010 makes the local-model boundary visible in the channel page. The current public APK contains no generative local-model weights and does not start llama.cpp or another runtime.

A future local-small model may suggest only classification, fixed FAQ, privacy rewrite, and offline explanation. It may not automatically enable itself, change any channel model, plan a task, issue a tool call, read private device data, invoke OpenCode/root, or suppress the visible selected-model state. Every model/channel change remains a user-controlled configuration change.

The static gate verifies this notice is visible and that it names the denied authorities. A real local runtime, memory/thermal budget, model license, and user acceptance remain separate work and are not implied by this task.
