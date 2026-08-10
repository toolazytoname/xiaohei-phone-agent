# Bounded in-memory Conversation session

Status date: 2026-08-10. `CHAT-006` provides the reusable context boundary for future follow-up dialog. It does not enable multi-turn UI, speech, tools, planning, or actions by itself.

## Runtime contract

`MemoryConversationSession` is a pure-Java, process-local object aligned with `conversation-session.v1`:

- 1–8 completed user/assistant turns;
- 64–8192 estimated transcript tokens;
- 1 second–15 minutes total lifetime;
- exactly one in-flight turn;
- `action_authority=none` and memory-only transcript handling.

The token count is a conservative, provider-independent guard, not a billing tokenizer: each message is charged at least its Unicode code-point count and at least one token per four UTF-8 bytes. Provider-side `max_tokens` remains a separate transport cap.

## State and clearing rules

`beginTurn` appends one normalized user message only when all three budgets allow it. `completeTurn` appends one assistant reply. `abortTurn` removes a pending user message after a failed request so a changed retry does not inherit a phantom turn.

The session closes and releases all internal transcript references when:

- the next text would exceed the token budget;
- the configured turn limit is reached;
- the total monotonic deadline is reached or the clock moves backwards;
- the user cancels;
- an invalid assistant reply would otherwise leave an ambiguous pending turn.

The caller receives an explicit reason. Repeated operations on a closed session return `CLOSED` without replacing that terminal reason. Public-log-safe status exposes only counts, budgets, lifecycle flags, and the reason—never message text.

Java cannot guarantee physical zeroization of immutable `String` objects or copies already held by a caller. “Cleared” therefore means that the session releases its own references and exposes no restoration mechanism; it is not a memory-forensics claim.

## No restart restoration

The implementation imports no Android persistence API, database, file serializer, parcel, or logger. It has no static transcript collection. A new process can only create a new empty instance; there is no method that accepts or restores transcript text from storage.

## Verification

Twelve deterministic cases cover invalid bounds, ordered history, turn/input/output token limits, deadline and clock rollback, cancellation, failed-request rollback, concurrent-turn denial, immutable request views, conservative Chinese counting, and a fresh-instance restart simulation.

Run:

```bash
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-conversation-memory-boundary.py
bash scripts/verify.sh
```

The static gate also reads the JSON contract, compares all six runtime min/max constants, and rejects persistence or transcript-logging paths.

## Next integration

`CHAT-007` may add a 3–8-turn half-duplex follow-up window by converting the immutable request view into model messages. It must not persist transcript text, bypass terminal outcomes, or reuse a session after model/profile switch, lock, global stop, or timeout.
