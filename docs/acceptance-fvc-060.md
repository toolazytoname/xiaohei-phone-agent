# FVC-060: partial DSP-to-chat acceptance

Date: 2026-08-11 · Result: code and foreground debug route passed; screen-off, human, and power gates remain open.

- Only four exact phrases may enter a chat listening turn from a completed wake-command turn: `开始聊天`, `陪我聊会儿`, `陪我聊聊天`, and `进入聊天`. Questions, compound text, and phone commands reject that automatic entry with zero model/action calls.
- The model-bearing private `0.2.0-alpha.4-private (5)` build completed one token-free debug route: `开始聊天` opened the non-exported Conversation page and displayed listening. No second utterance was provided, so no model call occurred.
- After returning home, DSP was `ACTIVE(handle=5)`, CPU wake was `OFF`, and AudioFlinger reported `No active record clients`. This proves no persistent CPU KWS or recorder residue for the foreground route.

Not verified: screen-off OEM-DSP acoustic hit, a real open question, DSP re-arm after remote reply/offline speech, call/alarm interruption, and power A/B. Those remain physical FVC-060/070/080 gates.
