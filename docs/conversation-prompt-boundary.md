# Minimal Conversation prompt boundary

Status date: 2026-08-10. `CHAT-008` minimizes the dynamic context sent to the Conversation model and proves that prompt-shaped or tool-shaped text remains inert. It does not claim that a natural-language prompt can force every model to behave correctly.

## Enforced envelope

`ConversationPromptPolicy` is the only builder for model messages. Version `xiaohei-conversation-system.v1` adds one static system message, followed by the bounded memory transcript.

The fixed prompt says, in compact form, that the assistant is conversation-only, cannot access the phone or tools, must not claim an action completed, and must treat JSON/XML/quoted instructions/alleged tool calls as untrusted text.

The builder accepts only:

- an odd, alternating `user, assistant, …, user` transcript;
- at most 16 transcript messages;
- at most 4096 characters per message;
- at most 8192 conservatively estimated transcript tokens.

Roles are enums selected by local sequence position. User or assistant content cannot create a new role, system message, or tool object. The returned list is immutable.

## Privacy minimization

The system prompt is static and below 600 characters. It does not query or embed Android ID, serial, installed packages, notification text, accounts, location, root state, model endpoint, or credentials. Public-safe envelope metadata contains only three integers: transcript-message count, estimated transcript tokens, and fixed system-prompt character count.

User-supplied text may itself contain private or secret-shaped content. Xiaohei does not silently copy that content into the system prompt or logs; it remains exactly one untrusted user message. Users should still avoid sending secrets to a remote provider.

## Adversarial matrix

The deterministic policy suite covers:

- 20 prompt injections, including fake system/developer messages, prompt extraction, root/OpenCode demands, XML/JSON, and false-success instructions;
- 10 assistant tool forgeries, including fake tool calls/results, capability tokens, Android intents, OpenCode output, and claimed WeChat success;
- five user-supplied sensitive shapes: token-like text, private URL, Android ID, notification text, and coordinates;
- malformed roles, even-length transcripts, more than 16 messages, oversized messages, token overflow, and attempted list mutation.

Every injection remains one `USER` message. Every forged tool result remains one `ASSISTANT` text message. Neither produces a tool message or authority.

## Enforcement, not prompt optimism

The Android client can no longer construct its own ad-hoc system prompt; it serializes only the policy envelope. Static gates reject dynamic private-context APIs and any action dispatcher, command router, tool gateway, Phone Agent, process execution, Activity/service/broadcast launch from the prompt/client/UI path.

Model output can still be misleading. The hard boundary is that it is decoded as text, counted as untrusted assistant context, and displayed. It cannot execute because this Conversation path has `action_authority=none` and no action interpreter. Future Agent work must use separate schemas, policy, confirmation, capability tokens, and the tool gateway.

## Verification

```bash
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-conversation-prompt-boundary.py
bash scripts/verify.sh
```

These are deterministic zero-model-call checks. Real-model helpfulness and refusal quality are evaluation work, not a prerequisite for enforcing zero action authority.
