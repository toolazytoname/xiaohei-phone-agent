package io.github.toolazytoname.xiaohei;

import java.util.HashSet;
import java.util.Set;

/** In-memory authorization gate; the Android executor is deliberately not reachable without it. */
final class ToolGateway {
    static final class Token {
        final String id, task, tool; final long expiryMs; final boolean singleUse;
        Token(String id, String task, String tool, long expiryMs, boolean singleUse) {
            this.id=id; this.task=task; this.tool=tool; this.expiryMs=expiryMs; this.singleUse=singleUse;
        }
    }
    enum Decision { ALLOW, UNKNOWN_TOOL, RISK_MISMATCH, TOKEN_EXPIRED, TOKEN_REPLAY, TOKEN_SCOPE, IDEMPOTENCY_REPLAY }
    private final Set<String> usedTokens = new HashSet<>();
    private final Set<String> usedKeys = new HashSet<>();
    Decision authorize(String task, String tool, ToolCatalog.Risk risk, String key, Token token, long nowMs) {
        if (!ToolCatalog.allowed(tool, risk)) return ToolCatalog.risk(tool)==null ? Decision.UNKNOWN_TOOL : Decision.RISK_MISMATCH;
        if (token==null || !task.equals(token.task) || !tool.equals(token.tool)) return Decision.TOKEN_SCOPE;
        if (nowMs >= token.expiryMs) return Decision.TOKEN_EXPIRED;
        if (token.singleUse && usedTokens.contains(token.id)) return Decision.TOKEN_REPLAY;
        if (key==null || !usedKeys.add(task + "|" + key)) return Decision.IDEMPOTENCY_REPLAY;
        if (token.singleUse) usedTokens.add(token.id);
        return Decision.ALLOW;
    }
}
