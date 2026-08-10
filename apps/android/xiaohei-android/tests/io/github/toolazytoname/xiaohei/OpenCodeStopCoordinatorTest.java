package io.github.toolazytoname.xiaohei;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public final class OpenCodeStopCoordinatorTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("xiaohei-oc-stop-");
        try { stopsRegisteredResourcesAndLease(root); rejectsWrongAndRepeatedStop(root); recursiveCleanupDoesNotFollowLinks(root);
            System.out.println("PASS OpenCodeStopCoordinatorTest stop=1 resources=3 revoked=0 workspace=2 recursive=1 symlink_safe=1 wrong=1 repeated=1 process_paths=test_handles_only");
        } finally { Files.walk(root).sorted(Comparator.reverseOrder()).forEach(path -> { try { Files.deleteIfExists(path); } catch (Exception e) { throw new AssertionError(e); } }); }
    }
    private static void stopsRegisteredResourcesAndLease(Path root) {
        Fixture f = fixture(root, "job-stop-public-0001");
        OpenCodeStopCoordinator coordinator = new OpenCodeStopCoordinator();
        Handle process = new Handle(OpenCodeStopCoordinator.ResourceKind.PROCESS, true);
        Handle listener = new Handle(OpenCodeStopCoordinator.ResourceKind.LISTENER, true);
        Handle tmux = new Handle(OpenCodeStopCoordinator.ResourceKind.TMUX, true);
        check(coordinator.register(f.task, f.lease, f.cancellation, new ToolGateway(), Arrays.asList(process, listener, tmux)), "register");
        OpenCodeStopCoordinator.Result result = coordinator.stop(f.task.taskId);
        check(result.code == OpenCodeStopCoordinator.Code.STOPPED && result.cancelledWorkers == 1 && result.stoppedResources == 3 && result.workspaceReleased, "stop");
        check(process.stops == 1 && listener.stops == 1 && tmux.stops == 1, "all resources");
        check(!Files.exists(f.lease.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT)), "workspace released");
    }
    private static void rejectsWrongAndRepeatedStop(Path root) {
        Fixture f = fixture(root, "job-stop-public-0002");
        OpenCodeStopCoordinator coordinator = new OpenCodeStopCoordinator();
        check(coordinator.register(f.task, f.lease, f.cancellation, new ToolGateway(), Collections.emptyList()), "register 2");
        check(coordinator.stop("job-stop-public-other").code == OpenCodeStopCoordinator.Code.WRONG_TASK, "wrong task");
        check(coordinator.stop(f.task.taskId).code == OpenCodeStopCoordinator.Code.STOPPED, "clean stop");
        check(coordinator.stop(f.task.taskId).code == OpenCodeStopCoordinator.Code.NOTHING_ACTIVE, "repeated stop");
    }
    private static void recursiveCleanupDoesNotFollowLinks(Path root) throws Exception {
        Fixture f = fixture(root, "job-stop-public-0003");
        Path input = f.lease.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT);
        Files.createDirectories(input.resolve("nested"));
        Files.write(input.resolve("nested/work.txt"), Arrays.asList("private"));
        Path outside = Files.createDirectories(root.resolve("outside-stop-proof"));
        Path outsideFile = outside.resolve("keep.txt");
        Files.write(outsideFile, Arrays.asList("keep"));
        Files.createSymbolicLink(input.resolve("outside-link"), outside);
        check(OpenCodeWorkspaceBoundary.release(f.lease).code == OpenCodeWorkspaceBoundary.Code.CREATED, "recursive release");
        check(!Files.exists(input) && Files.exists(outsideFile), "link not followed");
    }
    private static Fixture fixture(Path root, String id) {
        UnconfirmedActionRequest.Result request = UnconfirmedActionRequest.fromConversationMessage(new MemoryConversationSession.Message(MemoryConversationSession.Role.USER, "帮我整理项目文件"), "request-stop-public-0001", "2026-08-10T15:10:00Z");
        OpenCodeTaskProtocol.Result task = OpenCodeTaskProtocol.create(request.request, id, "plan-stop-public-0001", OpenCodeTaskProtocol.Kind.CONTROLLED_FILE_ORGANIZATION);
        OpenCodeWorkspaceBoundary.Result lease = OpenCodeWorkspaceBoundary.allocate(root, task.task);
        check(task.code == OpenCodeTaskProtocol.Code.CREATED && lease.code == OpenCodeWorkspaceBoundary.Code.CREATED, "fixture");
        return new Fixture(task.task, lease.lease, new OpenCodeBoundedRunner.Cancellation());
    }
    private static final class Fixture { final OpenCodeTaskProtocol.Task task; final OpenCodeWorkspaceBoundary.Lease lease; final OpenCodeBoundedRunner.Cancellation cancellation; Fixture(OpenCodeTaskProtocol.Task t, OpenCodeWorkspaceBoundary.Lease l, OpenCodeBoundedRunner.Cancellation c) { task=t; lease=l; cancellation=c; } }
    private static final class Handle implements OpenCodeStopCoordinator.Resource { final OpenCodeStopCoordinator.ResourceKind kind; final boolean answer; int stops; Handle(OpenCodeStopCoordinator.ResourceKind k, boolean a) { kind=k; answer=a; } public OpenCodeStopCoordinator.ResourceKind kind() { return kind; } public boolean stop() { stops++; return answer; } }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
