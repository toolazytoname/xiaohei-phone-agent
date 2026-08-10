package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.List;

/**
 * Creates a private, pending OpenCode task proposal from an already typed complex user request.
 * It has no command, workspace, process, network, Android, root, or execution path.
 */
final class OpenCodeTaskProtocol {
    static final int SCHEMA_VERSION = 1;
    static final int MAX_INSTRUCTION_CODE_POINTS = 2048;
    static final String SOURCE = "typed_user";
    static final String CONFIRMATION_STATE = "pending";
    static final String EXECUTION_STATE = "not_started";

    enum Kind { PROJECT_SUMMARY, TEST_DIAGNOSIS, CONTROLLED_FILE_ORGANIZATION }
    enum Code { CREATED, INVALID_SOURCE, INVALID_KIND, INVALID_ID, INVALID_REQUEST }

    static final class Task {
        final int schemaVersion;
        final String taskId;
        final String requestId;
        final String planId;
        final Kind kind;
        final String source;
        private final String instruction;
        final boolean dryRun;
        final boolean requiresConfirmation;
        final String confirmationState;
        final String executionState;
        final ToolCatalog.Audience audience;
        final boolean publicLogSafe;
        final List<String> sensitiveFields;

        private Task(String taskId, String requestId, String planId, Kind kind, String instruction) {
            this.schemaVersion = SCHEMA_VERSION;
            this.taskId = taskId;
            this.requestId = requestId;
            this.planId = planId;
            this.kind = kind;
            this.source = SOURCE;
            this.instruction = instruction;
            this.dryRun = true;
            this.requiresConfirmation = true;
            this.confirmationState = CONFIRMATION_STATE;
            this.executionState = EXECUTION_STATE;
            this.audience = ToolCatalog.Audience.OPENCODE_GATEWAY;
            this.publicLogSafe = false;
            this.sensitiveFields = Collections.singletonList("instruction");
        }

        String instructionForExecutor() {
            return instruction;
        }

        SafeMetadata safeMetadata() {
            return new SafeMetadata(schemaVersion, kind, source, dryRun, requiresConfirmation,
                    confirmationState, executionState, audience, instruction.codePointCount(0, instruction.length()));
        }
    }

    /** Public-log-safe metadata deliberately excludes task/request/plan IDs and instruction text. */
    static final class SafeMetadata {
        final int schemaVersion;
        final Kind kind;
        final String source;
        final boolean dryRun;
        final boolean requiresConfirmation;
        final String confirmationState;
        final String executionState;
        final ToolCatalog.Audience audience;
        final int instructionCodePoints;

        private SafeMetadata(int schemaVersion, Kind kind, String source, boolean dryRun,
                boolean requiresConfirmation, String confirmationState, String executionState,
                ToolCatalog.Audience audience, int instructionCodePoints) {
            this.schemaVersion = schemaVersion;
            this.kind = kind;
            this.source = source;
            this.dryRun = dryRun;
            this.requiresConfirmation = requiresConfirmation;
            this.confirmationState = confirmationState;
            this.executionState = executionState;
            this.audience = audience;
            this.instructionCodePoints = instructionCodePoints;
        }
    }

    static final class Result {
        final Code code;
        final Task task;
        final int modelCalls;
        final int actionCalls;
        final int executionCalls;

        private Result(Code code, Task task) {
            this.code = code;
            this.task = task;
            this.modelCalls = 0;
            this.actionCalls = 0;
            this.executionCalls = 0;
        }
    }

    private OpenCodeTaskProtocol() {}

    static Result create(UnconfirmedActionRequest.Request request, String taskId,
            String planId, Kind kind) {
        if (request == null) return result(Code.INVALID_SOURCE);
        if (kind == null) return result(Code.INVALID_KIND);
        if (!validLongId(taskId) || !validLongId(planId)) return result(Code.INVALID_ID);
        if (!validRequest(request)) return result(Code.INVALID_REQUEST);
        return new Result(Code.CREATED, new Task(taskId, request.requestId, planId, kind,
                request.userTextForPlanner()));
    }

    private static boolean validRequest(UnconfirmedActionRequest.Request request) {
        String instruction = request.userTextForPlanner();
        return request.schemaVersion == UnconfirmedActionRequest.SCHEMA_VERSION
                && validLongId(request.requestId)
                && UnconfirmedActionRequest.TARGET.equals(request.target)
                && UnconfirmedActionRequest.ACTION.equals(request.action)
                && UnconfirmedActionRequest.RISK.equals(request.risk)
                && request.requiresConfirmation
                && UnconfirmedActionRequest.CONFIRMATION_STATE.equals(request.confirmationState)
                && request.dryRun && !request.publicLogSafe
                && request.sensitiveFields.equals(Collections.singletonList(
                        UnconfirmedActionRequest.SENSITIVE_FIELD))
                && instruction != null && !instruction.isEmpty()
                && instruction.codePointCount(0, instruction.length()) <= MAX_INSTRUCTION_CODE_POINTS;
    }

    private static boolean validLongId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}");
    }

    private static Result result(Code code) {
        return new Result(code, null);
    }
}
