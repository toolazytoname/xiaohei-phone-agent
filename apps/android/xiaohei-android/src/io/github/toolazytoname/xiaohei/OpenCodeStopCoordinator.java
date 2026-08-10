package io.github.toolazytoname.xiaohei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Global-stop boundary for one registered OpenCode task. Real OS resources remain adapter-owned. */
final class OpenCodeStopCoordinator {
    enum ResourceKind { PROCESS, LISTENER, TMUX }
    enum Code { STOPPED, NOTHING_ACTIVE, WRONG_TASK, CLEANUP_FAILED }

    interface Resource {
        ResourceKind kind();
        boolean stop();
    }

    static final class Result {
        final Code code;
        final int cancelledWorkers;
        final int revokedTokens;
        final int stoppedResources;
        final boolean workspaceReleased;
        final boolean publicLogSafe = true;
        private Result(Code code, int cancelledWorkers, int revokedTokens, int stoppedResources,
                boolean workspaceReleased) {
            this.code = code;
            this.cancelledWorkers = cancelledWorkers;
            this.revokedTokens = revokedTokens;
            this.stoppedResources = stoppedResources;
            this.workspaceReleased = workspaceReleased;
        }
    }

    private String activeTaskId;
    private OpenCodeBoundedRunner.Cancellation cancellation;
    private OpenCodeWorkspaceBoundary.Lease lease;
    private ToolGateway gateway;
    private List<Resource> resources = Collections.emptyList();

    synchronized boolean register(OpenCodeTaskProtocol.Task task, OpenCodeWorkspaceBoundary.Lease value,
            OpenCodeBoundedRunner.Cancellation signal, ToolGateway valueGateway, List<Resource> handles) {
        if (activeTaskId != null || task == null || value == null || signal == null || valueGateway == null
                || !task.taskId.equals(value.taskId) || handles == null || handles.size() > 3) return false;
        for (Resource handle : handles) if (handle == null || handle.kind() == null) return false;
        activeTaskId = task.taskId;
        lease = value;
        cancellation = signal;
        gateway = valueGateway;
        resources = Collections.unmodifiableList(new ArrayList<>(handles));
        return true;
    }

    synchronized Result stop(String taskId) {
        if (activeTaskId == null) return new Result(Code.NOTHING_ACTIVE, 0, 0, 0, true);
        if (taskId == null || !taskId.equals(activeTaskId)) return new Result(Code.WRONG_TASK, 0, 0, 0, false);
        int workers = cancellation.cancel(OpenCodeBoundedRunner.CancelReason.GLOBAL_STOP) ? 1 : 0;
        int revoked = gateway.revokeAll();
        int stopped = 0;
        boolean clean = true;
        for (Resource resource : resources) { if (resource.stop()) stopped++; else clean = false; }
        OpenCodeWorkspaceBoundary.Result released = OpenCodeWorkspaceBoundary.release(lease);
        clean = clean && released.code == OpenCodeWorkspaceBoundary.Code.CREATED;
        clear();
        return new Result(clean ? Code.STOPPED : Code.CLEANUP_FAILED, workers, revoked, stopped,
                released.code == OpenCodeWorkspaceBoundary.Code.CREATED);
    }

    private void clear() { activeTaskId = null; cancellation = null; lease = null; gateway = null; resources = Collections.emptyList(); }
}
