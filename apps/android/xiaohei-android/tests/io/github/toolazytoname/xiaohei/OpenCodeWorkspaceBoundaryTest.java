package io.github.toolazytoname.xiaohei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class OpenCodeWorkspaceBoundaryTest {
    private static final String REQUEST_ID = "request-workspace-public-0001";
    private static final String PLAN_ID = "plan-workspace-public-0001";
    private static final String CREATED_AT = "2026-08-10T14:10:00Z";

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("xiaohei-oc-workspace-test-");
        try {
            taskRootsArePrivateAndDistinct(root);
            traversalAndAbsolutePathsFailClosed(root);
            symbolicLinksAndCrossTaskPathsFailClosed(root);
            invalidInputsFailClosed(root);
            System.out.println("PASS OpenCodeWorkspaceBoundaryTest leases=2 safe_paths=4 traversal=7 "
                    + "symlink=3 cross_task=2 content_reads=0 content_writes=0 process_calls=0");
        } finally {
            deleteTree(root);
        }
    }

    private static void taskRootsArePrivateAndDistinct(Path root) {
        OpenCodeWorkspaceBoundary.Result first = OpenCodeWorkspaceBoundary.allocate(root, task("job-workspace-public-0001"));
        expect(OpenCodeWorkspaceBoundary.Code.CREATED, first, "first lease");
        check(Files.isDirectory(first.lease.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT)), "missing input root");
        check(Files.isDirectory(first.lease.rootFor(OpenCodeWorkspaceBoundary.Area.OUTPUT)), "missing output root");
        check(first.lease.safeMetadata().areaCount == 2, "safe metadata");
        OpenCodeWorkspaceBoundary.Result duplicate = OpenCodeWorkspaceBoundary.allocate(root, task("job-workspace-public-0001"));
        expect(OpenCodeWorkspaceBoundary.Code.EXISTS, duplicate, "duplicate lease");
        OpenCodeWorkspaceBoundary.Result second = OpenCodeWorkspaceBoundary.allocate(root, task("job-workspace-public-0002"));
        expect(OpenCodeWorkspaceBoundary.Code.CREATED, second, "second lease");
        check(!first.lease.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT)
                .equals(second.lease.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT)), "task roots shared");
        for (String path : new String[] {"report.txt", "nested/report.txt", "中文/报告.txt", "a-b_c.1"}) {
            OpenCodeWorkspaceBoundary.Result resolved = OpenCodeWorkspaceBoundary.resolve(
                    first.lease, OpenCodeWorkspaceBoundary.Area.OUTPUT, path);
            expect(OpenCodeWorkspaceBoundary.Code.CREATED, resolved, "safe path " + path);
            check(resolved.path.startsWith(first.lease.rootFor(OpenCodeWorkspaceBoundary.Area.OUTPUT)),
                    "safe path escaped");
            assertNoSideEffects(resolved);
        }
    }

    private static void traversalAndAbsolutePathsFailClosed(Path root) {
        OpenCodeWorkspaceBoundary.Lease lease = OpenCodeWorkspaceBoundary.allocate(root,
                task("job-workspace-public-0003")).lease;
        String absolute = root.resolve("outside.txt").toAbsolutePath().toString();
        String[] rejected = {"../outside", "nested/../../outside", ".", "./inside", "", absolute, "/tmp/outside"};
        for (String path : rejected) {
            OpenCodeWorkspaceBoundary.Result result = OpenCodeWorkspaceBoundary.resolve(
                    lease, OpenCodeWorkspaceBoundary.Area.INPUT, path);
            check(result.code == OpenCodeWorkspaceBoundary.Code.INVALID_RELATIVE_PATH
                    || result.code == OpenCodeWorkspaceBoundary.Code.PATH_ESCAPE,
                    "unsafe path accepted: " + path + " => " + result.code);
            assertNoSideEffects(result);
        }
    }

    private static void symbolicLinksAndCrossTaskPathsFailClosed(Path root) throws Exception {
        OpenCodeWorkspaceBoundary.Lease first = OpenCodeWorkspaceBoundary.allocate(root,
                task("job-workspace-public-0004")).lease;
        OpenCodeWorkspaceBoundary.Lease second = OpenCodeWorkspaceBoundary.allocate(root,
                task("job-workspace-public-0005")).lease;
        Path outside = Files.createDirectories(root.resolve("outside"));
        Path link = first.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT).resolve("link");
        Files.createSymbolicLink(link, outside);
        for (String path : new String[] {"link", "link/private.txt", "link/nested/secret.txt"}) {
            expect(OpenCodeWorkspaceBoundary.Code.SYMLINK_REJECTED,
                    OpenCodeWorkspaceBoundary.resolve(first, OpenCodeWorkspaceBoundary.Area.INPUT, path),
                    "symlink path " + path);
        }
        expect(OpenCodeWorkspaceBoundary.Code.INVALID_RELATIVE_PATH,
                OpenCodeWorkspaceBoundary.resolve(first, OpenCodeWorkspaceBoundary.Area.INPUT,
                        "../" + second.taskId + "/input/private.txt"), "cross task traversal");
        expect(OpenCodeWorkspaceBoundary.Code.INVALID_RELATIVE_PATH,
                OpenCodeWorkspaceBoundary.resolve(first, OpenCodeWorkspaceBoundary.Area.INPUT,
                        second.rootFor(OpenCodeWorkspaceBoundary.Area.INPUT).toString()), "cross task absolute");
    }

    private static void invalidInputsFailClosed(Path root) throws Exception {
        expect(OpenCodeWorkspaceBoundary.Code.INVALID_ROOT,
                OpenCodeWorkspaceBoundary.allocate(null, task("job-workspace-public-0006")), "null root");
        expect(OpenCodeWorkspaceBoundary.Code.INVALID_TASK,
                OpenCodeWorkspaceBoundary.allocate(root, null), "null task");
        expect(OpenCodeWorkspaceBoundary.Code.WRONG_LEASE,
                OpenCodeWorkspaceBoundary.resolve(null, OpenCodeWorkspaceBoundary.Area.INPUT, "safe.txt"),
                "null lease");
        Path linkRoot = root.resolve("linked-private-root");
        Files.createSymbolicLink(linkRoot, Files.createDirectories(root.resolve("real-private-root")));
        expect(OpenCodeWorkspaceBoundary.Code.SYMLINK_REJECTED,
                OpenCodeWorkspaceBoundary.allocate(linkRoot, task("job-workspace-public-0007")), "symlink root");
    }

    private static OpenCodeTaskProtocol.Task task(String taskId) {
        UnconfirmedActionRequest.Result request = UnconfirmedActionRequest.fromConversationMessage(
                new MemoryConversationSession.Message(MemoryConversationSession.Role.USER, "帮我整理项目文件"),
                REQUEST_ID, CREATED_AT);
        check(request.outcome == UnconfirmedActionRequest.Outcome.CREATED, "source request");
        OpenCodeTaskProtocol.Result result = OpenCodeTaskProtocol.create(request.request, taskId, PLAN_ID,
                OpenCodeTaskProtocol.Kind.CONTROLLED_FILE_ORGANIZATION);
        check(result.code == OpenCodeTaskProtocol.Code.CREATED, "protocol task");
        return result.task;
    }

    private static void expect(OpenCodeWorkspaceBoundary.Code expected,
            OpenCodeWorkspaceBoundary.Result result, String message) {
        check(result != null && result.code == expected, message + " actual="
                + (result == null ? "null" : result.code));
        assertNoSideEffects(result);
    }

    private static void assertNoSideEffects(OpenCodeWorkspaceBoundary.Result result) {
        check(result.processCalls == 0 && result.contentReads == 0 && result.contentWrites == 0,
                "workspace boundary caused side effect");
    }

    private static void deleteTree(Path root) throws IOException {
        Files.walk(root).sorted(Comparator.reverseOrder()).forEach(path -> {
            try { Files.deleteIfExists(path); }
            catch (IOException failure) { throw new AssertionError("temp cleanup", failure); }
        });
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
