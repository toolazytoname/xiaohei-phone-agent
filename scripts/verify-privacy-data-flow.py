#!/usr/bin/env python3
"""Guard the documented current privacy boundaries against accidental omission."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DOC_CASES = (
    (ROOT / "docs/privacy-data-flow.md", (
        "Microphone audio", "Speech transcript and chat text", "Screenshots / visual recovery",
        "Notifications and message targets", "Location", "Files, photos, contacts, and calendar",
        "agent-trace.v1.jsonl", "No raw PCM/audio file", "Never sent to a model",
    )),
    (ROOT / "docs/privacy-data-flow.zh-CN.md", (
        "麦克风音频", "语音转写和聊天文本", "截图/视觉恢复", "通知和消息目标", "位置",
        "文件、照片、联系人和日历", "agent-trace.v1.jsonl", "不发送音频", "绝不把图片发送给模型",
    )),
)
SOURCE_CASES = (
    (ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/XiaoheiRecognitionService.java",
     ("maximumMs = 8000", "audio.release()", "transcript_chars=")),
    (ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AgentTraceStore.java",
     ("MAX_BYTES = 256 * 1024", "static void clear", "never stores snapshots, model prompts, or secrets")),
    (ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ConversationPrivacyPolicy.java",
     ("NOTIFICATIONS", "LOCATION", "PRIVATE_MEDIA", "CREDENTIALS")),
    (ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SecureSecretStore.java",
     ("AndroidKeyStore", "AES/GCM/NoPadding", "static void clear")),
)

for path, required in DOC_CASES + SOURCE_CASES:
    text = path.read_text(encoding="utf-8")
    missing = [item for item in required if item not in text]
    if missing:
        raise SystemExit(f"FAIL privacy-data-flow {path.name}: missing {', '.join(missing)}")

manifest = (ROOT / "apps/android/xiaohei-android/AndroidManifest.xml").read_text(encoding="utf-8")
if "ACCESS_FINE_LOCATION" in manifest or "ACCESS_COARSE_LOCATION" in manifest:
    raise SystemExit("FAIL privacy-data-flow manifest declares a location permission")

print("PASS privacy-data-flow categories=6 bilingual=true source-boundaries=4 location_permission=absent")
