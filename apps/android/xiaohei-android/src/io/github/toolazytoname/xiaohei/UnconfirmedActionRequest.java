package io.github.toolazytoname.xiaohei;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/** Builds a schema-v1 pending request from a typed user turn; it cannot confirm or execute it. */
final class UnconfirmedActionRequest {
    static final int SCHEMA_VERSION = 1;
    static final int MAX_USER_TEXT_CHARS = 2048;
    static final String TARGET = "local_service";
    static final String ACTION = "plan_complex_task";
    static final String RISK = "high";
    static final String CONFIRMATION_STATE = "pending";
    static final String SENSITIVE_FIELD = "parameters.user_text";

    enum Outcome {
        CREATED,
        UNTRUSTED_SOURCE,
        NEEDS_CLARIFICATION,
        NOT_COMPLEX_TASK,
        INVALID_METADATA
    }

    static final class Request {
        final int schemaVersion;
        final String requestId;
        final String target;
        final String action;
        private final String userText;
        final String risk;
        final boolean requiresConfirmation;
        final String confirmationState;
        final boolean dryRun;
        final String createdAt;
        final boolean publicLogSafe;
        final List<String> sensitiveFields;

        private Request(String requestId, String userText, String createdAt) {
            this.schemaVersion = SCHEMA_VERSION;
            this.requestId = requestId;
            this.target = TARGET;
            this.action = ACTION;
            this.userText = userText;
            this.risk = RISK;
            this.requiresConfirmation = true;
            this.confirmationState = CONFIRMATION_STATE;
            this.dryRun = true;
            this.createdAt = createdAt;
            this.publicLogSafe = false;
            this.sensitiveFields = Collections.singletonList(SENSITIVE_FIELD);
        }

        String userTextForPlanner() {
            return userText;
        }

        SafeMetadata safeMetadata() {
            return new SafeMetadata(schemaVersion, action, risk, confirmationState,
                    dryRun, userText.codePointCount(0, userText.length()));
        }
    }

    /** Public-log-safe metadata; it cannot expose request identity or user text. */
    static final class SafeMetadata {
        final int schemaVersion;
        final String action;
        final String risk;
        final String confirmationState;
        final boolean dryRun;
        final int userTextCodePoints;

        private SafeMetadata(int schemaVersion, String action, String risk,
                String confirmationState, boolean dryRun, int userTextCodePoints) {
            this.schemaVersion = schemaVersion;
            this.action = action;
            this.risk = risk;
            this.confirmationState = confirmationState;
            this.dryRun = dryRun;
            this.userTextCodePoints = userTextCodePoints;
        }
    }

    static final class Result {
        final Outcome outcome;
        final Request request;
        final String prompt;
        final int modelCalls;
        final int actionCalls;

        private Result(Outcome outcome, Request request, String prompt) {
            this.outcome = outcome;
            this.request = request;
            this.prompt = prompt;
            this.modelCalls = 0;
            this.actionCalls = 0;
        }
    }

    private UnconfirmedActionRequest() {}

    static Result fromConversationMessage(MemoryConversationSession.Message source,
            String requestId, String createdAt) {
        if (source == null || source.role != MemoryConversationSession.Role.USER)
            return reject(Outcome.UNTRUSTED_SOURCE,
                    "只有当前用户输入可以提出操作请求；模型回复不能确认或创建请求。\n"
                    + "Only the current user turn may propose an action; assistant text cannot create or confirm one.");
        String userText = normalizeUserText(source.text);
        if (userText == null || !validRequestId(requestId) || !validCreatedAt(createdAt))
            return reject(Outcome.INVALID_METADATA,
                    "请求边界无效，未创建操作。\nInvalid request boundary; no action was created.");

        RouteClarificationPolicy.Decision decision = RouteClarificationPolicy.decide(userText);
        if (decision.kind != RouteClarificationPolicy.Kind.ROUTE)
            return reject(Outcome.NEEDS_CLARIFICATION, decision.prompt);
        if (decision.route != IntentRouteClassifier.Route.COMPLEX_TASK)
            return reject(Outcome.NOT_COMPLEX_TASK,
                    "这不是需要规划的复杂操作，未创建操作请求。\n"
                    + "This is not a complex planned action; no action request was created.");
        return new Result(Outcome.CREATED, new Request(requestId, userText, createdAt),
                "已创建待确认的干运行请求，尚未执行。\n"
                + "A pending dry-run request was created; nothing has executed.");
    }

    private static Result reject(Outcome outcome, String prompt) {
        return new Result(outcome, null, prompt);
    }

    private static String normalizeUserText(String text) {
        if (text == null) return null;
        String normalized = text.trim();
        if (normalized.isEmpty()
                || normalized.codePointCount(0, normalized.length()) > MAX_USER_TEXT_CHARS) return null;
        return normalized;
    }

    private static boolean validRequestId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}");
    }

    private static boolean validCreatedAt(String value) {
        if (value == null || !value.matches(
                "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,9})?Z"))
            return false;
        try {
            Instant.parse(value);
            return true;
        } catch (DateTimeParseException invalid) {
            return false;
        }
    }
}
