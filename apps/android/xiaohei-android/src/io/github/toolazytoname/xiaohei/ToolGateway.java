package io.github.toolazytoname.xiaohei;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/** Loopback/same-UID capability gate. It authorizes one call but never executes an adapter. */
final class ToolGateway {
    static final int SCHEMA_VERSION = 1;
    static final long MIN_TOKEN_TTL_MS = 1000L;
    static final long MAX_TOKEN_TTL_MS = 30000L;
    static final int MAX_ACTIVE_TOKENS = 16;
    static final int MAX_REPLAY_RECORDS = 256;

    enum Decision {
        ISSUED,
        ALLOW,
        NON_LOOPBACK,
        PEER_UID_MISMATCH,
        CONFIRMATION_REQUIRED,
        CONFIRMATION_REPLAY,
        CONFIRMATION_SCOPE,
        UNKNOWN_TOOL,
        VERSION_MISMATCH,
        RISK_MISMATCH,
        AUDIENCE_MISMATCH,
        INVALID_CALL,
        INVALID_WINDOW,
        CAPACITY,
        TOKEN_MISSING,
        TOKEN_EXPIRED,
        CLOCK_ROLLBACK,
        TOKEN_REPLAY,
        TOKEN_SCOPE,
        IDEMPOTENCY_REPLAY
    }

    static final class Peer {
        final String localAddress;
        final String remoteAddress;
        final int ownerUid;
        final int peerUid;

        Peer(String localAddress, String remoteAddress, int ownerUid, int peerUid) {
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
            this.ownerUid = ownerUid;
            this.peerUid = peerUid;
        }
    }

    static final class Call {
        final String taskId;
        final String requestId;
        final String planId;
        final String callId;
        final String tool;
        final int toolVersion;
        final ToolCatalog.Risk risk;
        final ToolCatalog.Audience audience;
        final Map<String, String> arguments;
        final String idempotencyKey;
        final long requestedAtElapsedMs;
        final boolean publicLogSafe;

        Call(String taskId, String requestId, String planId, String callId, String tool,
                int toolVersion, ToolCatalog.Risk risk, ToolCatalog.Audience audience,
                Map<String, String> arguments, String idempotencyKey,
                long requestedAtElapsedMs, boolean publicLogSafe) {
            this.taskId = taskId;
            this.requestId = requestId;
            this.planId = planId;
            this.callId = callId;
            this.tool = tool;
            this.toolVersion = toolVersion;
            this.risk = risk;
            this.audience = audience;
            this.arguments = arguments == null ? null
                    : Collections.unmodifiableMap(new HashMap<>(arguments));
            this.idempotencyKey = idempotencyKey;
            this.requestedAtElapsedMs = requestedAtElapsedMs;
            this.publicLogSafe = publicLogSafe;
        }
    }

    static final class Token {
        final int schemaVersion;
        final String tokenId;
        final String confirmationId;
        final String taskId;
        final String requestId;
        final String planId;
        final String callId;
        final String tool;
        final int toolVersion;
        final ToolCatalog.Risk risk;
        final ToolCatalog.Audience audience;
        final String callDigest;
        final long issuedAtElapsedMs;
        final long expiresAtElapsedMs;
        final long ttlMs;
        final boolean singleUse;
        final String persistence;
        final boolean publicLogSafe;

        private Token(String tokenId, FreshConfirmationGate.CapabilityReceipt receipt,
                Call call, String callDigest, long issuedAtElapsedMs, long expiresAtElapsedMs) {
            this.schemaVersion = SCHEMA_VERSION;
            this.tokenId = tokenId;
            this.confirmationId = receipt.confirmationId;
            this.taskId = call.taskId;
            this.requestId = call.requestId;
            this.planId = call.planId;
            this.callId = call.callId;
            this.tool = call.tool;
            this.toolVersion = call.toolVersion;
            this.risk = call.risk;
            this.audience = call.audience;
            this.callDigest = callDigest;
            this.issuedAtElapsedMs = issuedAtElapsedMs;
            this.expiresAtElapsedMs = expiresAtElapsedMs;
            this.ttlMs = expiresAtElapsedMs - issuedAtElapsedMs;
            this.singleUse = true;
            this.persistence = "memory_only";
            this.publicLogSafe = false;
        }
    }

    static final class Result {
        final Decision decision;
        final Token token;
        final int modelCalls;
        final int actionCalls;
        final int executionCalls;

        private Result(Decision decision, Token token) {
            this.decision = decision;
            this.token = token;
            this.modelCalls = 0;
            this.actionCalls = 0;
            this.executionCalls = 0;
        }
    }

    interface TokenIdSource {
        String nextId();
    }

    private static final class SecureTokenIdSource implements TokenIdSource {
        private final SecureRandom random = new SecureRandom();

        @Override public String nextId() {
            byte[] value = new byte[16];
            random.nextBytes(value);
            char[] result = new char[4 + value.length * 2];
            result[0] = 'c'; result[1] = 'a'; result[2] = 'p'; result[3] = '-';
            char[] digits = "0123456789abcdef".toCharArray();
            for (int index = 0; index < value.length; index++) {
                int unsigned = value[index] & 0xff;
                result[4 + index * 2] = digits[unsigned >>> 4];
                result[5 + index * 2] = digits[unsigned & 0x0f];
            }
            return new String(result);
        }
    }

    private static final class Grant {
        final Token token;

        private Grant(Token token) {
            this.token = token;
        }
    }

    private final TokenIdSource tokenIds;
    private final Map<String, Grant> active = new HashMap<>();
    private final Set<String> spentTokens = new HashSet<>();
    private final Set<String> spentIdempotencyKeys = new HashSet<>();

    ToolGateway() {
        this(new SecureTokenIdSource());
    }

    ToolGateway(TokenIdSource tokenIds) {
        if (tokenIds == null) throw new IllegalArgumentException("token id source required");
        this.tokenIds = tokenIds;
    }

    synchronized Result issue(Peer peer, FreshConfirmationGate.Result confirmation,
            Call call, long nowMs, long ttlMs) {
        Decision peerDecision = validatePeer(peer);
        if (peerDecision != null) return result(peerDecision);
        Decision callDecision = validateCall(call);
        if (callDecision != null) return result(callDecision);
        if (nowMs < 0 || ttlMs < MIN_TOKEN_TTL_MS || ttlMs > MAX_TOKEN_TTL_MS
                || nowMs > Long.MAX_VALUE - ttlMs) return result(Decision.INVALID_WINDOW);
        if (call.requestedAtElapsedMs > nowMs || nowMs - call.requestedAtElapsedMs > 60000L)
            return result(Decision.INVALID_CALL);
        if (active.size() >= MAX_ACTIVE_TOKENS
                || active.size() + spentTokens.size() >= MAX_REPLAY_RECORDS
                || spentIdempotencyKeys.size() >= MAX_REPLAY_RECORDS)
            return result(Decision.CAPACITY);
        if (confirmation == null || confirmation.code != FreshConfirmationGate.Code.ALLOW_ONCE)
            return result(Decision.CONFIRMATION_REQUIRED);
        FreshConfirmationGate.CapabilityReceipt receipt = confirmation.takeCapabilityReceipt();
        if (receipt == null) return result(Decision.CONFIRMATION_REPLAY);
        if (!receipt.taskId.equals(call.taskId) || !receipt.requestId.equals(call.requestId)
                || !receipt.planId.equals(call.planId)) return result(Decision.CONFIRMATION_SCOPE);
        String tokenId = nextUniqueTokenId();
        if (tokenId == null) return result(Decision.CAPACITY);
        Token token = new Token(tokenId, receipt, call, digest(tokenId, call), nowMs, nowMs + ttlMs);
        active.put(tokenId, new Grant(token));
        return new Result(Decision.ISSUED, token);
    }

    synchronized Result authorizeAndConsume(Peer peer, Call call, Token presented, long nowMs) {
        Decision peerDecision = validatePeer(peer);
        if (peerDecision != null) return result(peerDecision);
        if (presented == null || !validLongId(presented.tokenId)) return result(Decision.TOKEN_MISSING);
        Grant grant = active.get(presented.tokenId);
        if (grant == null) return result(spentTokens.contains(presented.tokenId)
                ? Decision.TOKEN_REPLAY : Decision.TOKEN_MISSING);
        Token token = grant.token;
        if (nowMs < token.issuedAtElapsedMs) return revoke(token, Decision.CLOCK_ROLLBACK);
        if (nowMs >= token.expiresAtElapsedMs) return revoke(token, Decision.TOKEN_EXPIRED);
        Decision callDecision = validateCall(call);
        if (callDecision != null) return revoke(token, callDecision);
        if (!token.callDigest.equals(digest(token.tokenId, call)))
            return revoke(token, Decision.TOKEN_SCOPE);
        String idempotencyIdentity = call.taskId + "|" + call.idempotencyKey;
        if (spentIdempotencyKeys.contains(idempotencyIdentity))
            return revoke(token, Decision.IDEMPOTENCY_REPLAY);
        active.remove(token.tokenId);
        spentTokens.add(token.tokenId);
        spentIdempotencyKeys.add(idempotencyIdentity);
        return result(Decision.ALLOW);
    }

    synchronized int revokeAll() {
        for (String tokenId : active.keySet()) spentTokens.add(tokenId);
        int count = active.size();
        active.clear();
        return count;
    }

    synchronized int activeCount() {
        return active.size();
    }

    private Result revoke(Token token, Decision decision) {
        active.remove(token.tokenId);
        spentTokens.add(token.tokenId);
        return result(decision);
    }

    private String nextUniqueTokenId() {
        for (int attempt = 0; attempt < 4; attempt++) {
            String candidate = tokenIds.nextId();
            if (validTokenId(candidate) && !active.containsKey(candidate)
                    && !spentTokens.contains(candidate)) return candidate;
        }
        return null;
    }

    private static Decision validatePeer(Peer peer) {
        if (peer == null || !loopbackLiteral(peer.localAddress)
                || !loopbackLiteral(peer.remoteAddress)) return Decision.NON_LOOPBACK;
        if (peer.ownerUid < 0 || peer.peerUid < 0 || peer.ownerUid != peer.peerUid)
            return Decision.PEER_UID_MISMATCH;
        return null;
    }

    private static Decision validateCall(Call call) {
        if (call == null || !validLongId(call.taskId) || !validLongId(call.requestId)
                || !validLongId(call.planId) || !validLongId(call.callId)
                || !validIdempotencyKey(call.idempotencyKey) || call.arguments == null
                || call.arguments.size() > 32 || call.requestedAtElapsedMs < 0
                || call.publicLogSafe) return Decision.INVALID_CALL;
        for (Map.Entry<String, String> entry : call.arguments.entrySet()) {
            if (entry.getKey() == null || !entry.getKey().matches("[a-z][a-z0-9_]{0,63}")
                    || entry.getValue() == null || entry.getValue().length() > 1024)
                return Decision.INVALID_CALL;
        }
        ToolCatalog.Descriptor descriptor = ToolCatalog.lookup(call.tool, call.toolVersion);
        if (descriptor == null) return ToolCatalog.risk(call.tool) == null
                ? Decision.UNKNOWN_TOOL : Decision.VERSION_MISMATCH;
        if (descriptor.risk != call.risk) return Decision.RISK_MISMATCH;
        if (descriptor.audience != call.audience) return Decision.AUDIENCE_MISMATCH;
        return null;
    }

    private static boolean loopbackLiteral(String value) {
        if (value == null) return false;
        String normalized = value.trim().toLowerCase();
        if (normalized.startsWith("[") && normalized.endsWith("]"))
            normalized = normalized.substring(1, normalized.length() - 1);
        if ("::1".equals(normalized) || "0:0:0:0:0:0:0:1".equals(normalized)) return true;
        String[] parts = normalized.split("\\.", -1);
        if (parts.length != 4 || !"127".equals(parts[0])) return false;
        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) return false;
            for (int index = 0; index < part.length(); index++)
                if (part.charAt(index) < '0' || part.charAt(index) > '9') return false;
            int number = Integer.parseInt(part);
            if (number < 0 || number > 255) return false;
        }
        return true;
    }

    private static boolean validLongId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}");
    }

    private static boolean validIdempotencyKey(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{15,127}");
    }

    private static boolean validTokenId(String value) {
        return value != null && value.matches("cap-[a-f0-9]{32}");
    }

    private static String digest(String tokenId, Call call) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, call.taskId); append(canonical, call.requestId);
        append(canonical, call.planId); append(canonical, call.callId);
        append(canonical, call.tool); append(canonical, String.valueOf(call.toolVersion));
        append(canonical, call.risk.name().toLowerCase(Locale.ROOT));
        append(canonical, call.audience.name().toLowerCase(Locale.ROOT));
        append(canonical, call.idempotencyKey);
        append(canonical, String.valueOf(call.requestedAtElapsedMs));
        append(canonical, String.valueOf(call.publicLogSafe));
        for (Map.Entry<String, String> entry : new TreeMap<>(call.arguments).entrySet()) {
            append(canonical, entry.getKey()); append(canonical, entry.getValue());
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest((tokenId + "|" + canonical).getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[hash.length * 2];
            char[] digits = "0123456789abcdef".toCharArray();
            for (int index = 0; index < hash.length; index++) {
                int unsigned = hash[index] & 0xff;
                hex[index * 2] = digits[unsigned >>> 4];
                hex[index * 2 + 1] = digits[unsigned & 0x0f];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static void append(StringBuilder target, String value) {
        target.append(value.length()).append(':').append(value);
    }

    private static Result result(Decision decision) {
        return new Result(decision, null);
    }
}
