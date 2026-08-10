#!/usr/bin/env python3
"""Static enforcement for CHAT-010 independent TTS selection and secret ownership."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
CONFIG = (JAVA / "TtsChannelConfig.java").read_text(encoding="utf-8")
ACTIVITY = (JAVA / "ModelConfigActivity.java").read_text(encoding="utf-8")
SECRETS = (JAVA / "SecureSecretStore.java").read_text(encoding="utf-8")
BACKUP = (JAVA / "ModelChannelBackup.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/TtsChannelConfigTest.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL TTS channel boundary: {message}")


for key in ("tts_provider", "tts_relay_endpoint", "tts_voice"):
    require(CONFIG.count(f'"{key}"') == 1, f"one owned key: {key}")

for provider in ('OFF("off")', 'SYSTEM("system")', 'RELAY("relay")'):
    require(provider in CONFIG, f"provider declared: {provider}")

for identifier in (
    "tts-provider-selector",
    "tts-relay-endpoint",
    "tts-voice-id",
    "tts-relay-token",
    "clear-tts-relay-token",
):
    require(ACTIVITY.count(f'"{identifier}"') == 1, f"stable UI identifier: {identifier}")

require('TTS_RELAY("tts_relay", "xiaohei.tts_relay.token.v1")' in SECRETS,
        "independent Keystore slot")
require("SecureSecretStore.Slot.TTS_RELAY" in ACTIVITY, "UI uses independent TTS secret slot")
require("xiaohei-model-channels.v3" in BACKUP and "xiaohei-model-channels.v2" in BACKUP,
        "v3 backup and v2 restore compatibility")
require("tts_relay_token" not in BACKUP.lower() and "tts_relay.token" not in BACKUP.lower(),
        "backup format never names or carries TTS token")

save = ACTIVITY[ACTIVITY.index("private void save()") : ACTIVITY.index("private void exportBackup()")]
for forbidden in (
    "SystemTtsAdapter",
    "TextToSpeech",
    ".speak(",
    "ConversationClient",
    "PhoneAgentClient",
    "startActivity(",
    "startService(",
    "startForegroundService(",
    "sendBroadcast(",
):
    require(forbidden not in save, f"save/selection has no side effect: {forbidden}")

require("conversation_unchanged=true agent_unchanged=true side_effects=0" in TEST,
        "isolation regression is declared")
require("ChannelProfileConfig.fingerprint" in TEST and "TtsChannelConfig.fingerprint" in TEST,
        "isolation compares all channel fingerprints")

print("PASS TTS channel boundary providers=3 keystore_slots=1 backup_token=0 selection_side_effects=0")
