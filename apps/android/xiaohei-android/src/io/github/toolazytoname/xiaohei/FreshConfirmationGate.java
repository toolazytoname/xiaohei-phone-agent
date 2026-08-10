package io.github.toolazytoname.xiaohei;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** In-memory one-use confirmation bound to exact scope and visible unlocked user gesture. */
final class FreshConfirmationGate {
    static final int SCHEMA_VERSION = 1;
    static final long MIN_TTL_MS = 1000L;
    static final long MAX_TTL_MS = 60000L;

    enum Source { LOCAL_USER_GESTURE, ASSISTANT_TEXT }

    enum Code {
        ISSUED,
        ALLOW_ONCE,
        UNTRUSTED_SOURCE,
        INVALID_SCOPE,
        INVALID_WINDOW,
        DEVICE_DENIED,
        MISSING,
        CANCELLED,
        EXPIRED,
        CLOCK_ROLLBACK,
        TASK_CHANGED,
        REQUEST_CHANGED,
        PLAN_CHANGED,
        TARGET_CHANGED,
        CONTENT_CHANGED
    }

    static final class Scope {
        final String taskId;
        final String requestId;
        final String planId;
        final String target;
        final String content;

        Scope(String taskId, String requestId, String planId, String target, String content) {
            this.taskId = taskId;
            this.requestId = requestId;
            this.planId = planId;
            this.target = target;
            this.content = content;
        }
    }

    static final class DeviceState {
        final boolean unlocked;
        final boolean interactive;
        final boolean foreground;

        DeviceState(boolean unlocked, boolean interactive, boolean foreground) {
            this.unlocked = unlocked;
            this.interactive = interactive;
            this.foreground = foreground;
        }

        boolean eligible() {
            return unlocked && interactive && foreground;
        }
    }

    /** Public status deliberately contains no identifiers, target, content, or digest. */
    static final class SafeStatus {
        final boolean active;
        final long remainingMs;
        final Code lastCode;

        private SafeStatus(boolean active, long remainingMs, Code lastCode) {
            this.active = active;
            this.remainingMs = remainingMs;
            this.lastCode = lastCode;
        }
    }

    static final class Result {
        final Code code;
        final int modelCalls;
        final int actionCalls;
        private CapabilityReceipt capabilityReceipt;

        private Result(Code code, CapabilityReceipt capabilityReceipt) {
            this.code = code;
            this.modelCalls = 0;
            this.actionCalls = 0;
            this.capabilityReceipt = capabilityReceipt;
        }

        /** Package-private one-time exchange; no text, target, content, or digest leaves the gate. */
        synchronized CapabilityReceipt takeCapabilityReceipt() {
            CapabilityReceipt current = capabilityReceipt;
            capabilityReceipt = null;
            return current;
        }
    }

    static final class CapabilityReceipt {
        final String confirmationId;
        final String taskId;
        final String requestId;
        final String planId;

        private CapabilityReceipt(Grant grant) {
            this.confirmationId = grant.confirmationId;
            this.taskId = grant.taskId;
            this.requestId = grant.requestId;
            this.planId = grant.planId;
        }
    }

    private static final class Grant {
        final String confirmationId;
        final String taskId;
        final String requestId;
        final String planId;
        final String targetDigest;
        final String contentDigest;
        final long issuedAtMs;
        final long expiresAtMs;

        private Grant(String confirmationId, Scope scope, long issuedAtMs, long expiresAtMs) {
            this.confirmationId = confirmationId;
            this.taskId = scope.taskId;
            this.requestId = scope.requestId;
            this.planId = scope.planId;
            this.targetDigest = digest(confirmationId, scope.target);
            this.contentDigest = digest(confirmationId, scope.content);
            this.issuedAtMs = issuedAtMs;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private Grant active;
    private Code lastCode = Code.MISSING;

    synchronized Result issue(Source source, String confirmationId, Scope scope,
            long nowMs, long ttlMs, DeviceState device) {
        if (source != Source.LOCAL_USER_GESTURE) return result(Code.UNTRUSTED_SOURCE);
        if (device == null || !device.eligible()) return invalidate(Code.DEVICE_DENIED);
        if (!validLongId(confirmationId) || !validScope(scope))
            return invalidate(Code.INVALID_SCOPE);
        if (nowMs < 0 || ttlMs < MIN_TTL_MS || ttlMs > MAX_TTL_MS
                || nowMs > Long.MAX_VALUE - ttlMs) return invalidate(Code.INVALID_WINDOW);
        active = new Grant(confirmationId, scope, nowMs, nowMs + ttlMs);
        return result(Code.ISSUED);
    }

    synchronized Result authorizeAndConsume(Scope current, long nowMs, DeviceState device) {
        Grant grant = active;
        if (grant == null) return result(Code.MISSING);
        if (device == null || !device.eligible()) return invalidate(Code.DEVICE_DENIED);
        if (nowMs < grant.issuedAtMs) return invalidate(Code.CLOCK_ROLLBACK);
        if (nowMs >= grant.expiresAtMs) return invalidate(Code.EXPIRED);
        if (!validScope(current)) return invalidate(Code.INVALID_SCOPE);
        if (!grant.taskId.equals(current.taskId)) return invalidate(Code.TASK_CHANGED);
        if (!grant.requestId.equals(current.requestId)) return invalidate(Code.REQUEST_CHANGED);
        if (!grant.planId.equals(current.planId)) return invalidate(Code.PLAN_CHANGED);
        if (!grant.targetDigest.equals(digest(grant.confirmationId, current.target)))
            return invalidate(Code.TARGET_CHANGED);
        if (!grant.contentDigest.equals(digest(grant.confirmationId, current.content)))
            return invalidate(Code.CONTENT_CHANGED);
        CapabilityReceipt receipt = new CapabilityReceipt(grant);
        active = null;
        return result(Code.ALLOW_ONCE, receipt);
    }

    synchronized Result cancel() {
        return invalidate(Code.CANCELLED);
    }

    synchronized SafeStatus status(long nowMs) {
        if (active == null) return new SafeStatus(false, 0, lastCode);
        if (nowMs < active.issuedAtMs) {
            invalidate(Code.CLOCK_ROLLBACK);
            return new SafeStatus(false, 0, lastCode);
        }
        if (nowMs >= active.expiresAtMs) {
            invalidate(Code.EXPIRED);
            return new SafeStatus(false, 0, lastCode);
        }
        return new SafeStatus(true, active.expiresAtMs - nowMs, lastCode);
    }

    private Result invalidate(Code code) {
        active = null;
        return result(code);
    }

    private Result result(Code code) {
        return result(code, null);
    }

    private Result result(Code code, CapabilityReceipt receipt) {
        lastCode = code;
        return new Result(code, receipt);
    }

    private static boolean validScope(Scope scope) {
        return scope != null && validLongId(scope.taskId) && validLongId(scope.requestId)
                && validLongId(scope.planId) && validText(scope.target, 256)
                && validText(scope.content, 4096);
    }

    private static boolean validLongId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}");
    }

    private static boolean validText(String value, int maxCodePoints) {
        return value != null && !value.trim().isEmpty()
                && value.codePointCount(0, value.length()) <= maxCodePoints;
    }

    private static String digest(String confirmationId, String value) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (confirmationId.length() + ":" + confirmationId
                    + value.length() + ":" + value).getBytes(StandardCharsets.UTF_8);
            byte[] hash = sha256.digest(bytes);
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
}
