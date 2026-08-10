package io.github.toolazytoname.xiaohei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Allocates empty task-private roots and resolves only safe relative paths below those roots.
 * It creates no OpenCode process, reads no task content, and accepts no caller-supplied root path.
 */
final class OpenCodeWorkspaceBoundary {
    static final int SCHEMA_VERSION = 1;
    static final String WORKSPACES_DIR = "xiaohei-opencode-tasks";
    static final String INPUT_DIR = "input";
    static final String OUTPUT_DIR = "output";
    static final String PERSISTENCE = "private_app_storage";
    static final String PATH_EXPOSURE = "none";

    enum Area { INPUT, OUTPUT }
    enum Code {
        CREATED,
        EXISTS,
        INVALID_TASK,
        INVALID_ROOT,
        WRONG_LEASE,
        INVALID_RELATIVE_PATH,
        PATH_ESCAPE,
        SYMLINK_REJECTED,
        IO_FAILURE
    }

    static final class Lease {
        final int schemaVersion;
        final String taskId;
        private final Path ownerRoot;
        private final Map<Area, Path> roots;

        private Lease(String taskId, Path ownerRoot, Path input, Path output) {
            this.schemaVersion = SCHEMA_VERSION;
            this.taskId = taskId;
            this.ownerRoot = ownerRoot;
            EnumMap<Area, Path> values = new EnumMap<>(Area.class);
            values.put(Area.INPUT, input);
            values.put(Area.OUTPUT, output);
            this.roots = Collections.unmodifiableMap(values);
        }

        Path rootFor(Area area) {
            return roots.get(area);
        }

        SafeMetadata safeMetadata() {
            return new SafeMetadata(roots.size());
        }
    }

    /** No task ID or filesystem path is safe for a public progress surface. */
    static final class SafeMetadata {
        final int areaCount;

        private SafeMetadata(int areaCount) {
            this.areaCount = areaCount;
        }
    }

    static final class Result {
        final Code code;
        final Lease lease;
        final Path path;
        final int processCalls;
        final int contentReads;
        final int contentWrites;

        private Result(Code code, Lease lease, Path path) {
            this.code = code;
            this.lease = lease;
            this.path = path;
            this.processCalls = 0;
            this.contentReads = 0;
            this.contentWrites = 0;
        }
    }

    private OpenCodeWorkspaceBoundary() {}

    static Result allocate(Path privateRoot, OpenCodeTaskProtocol.Task task) {
        if (privateRoot == null) return result(Code.INVALID_ROOT, null, null);
        if (!validTask(task)) return result(Code.INVALID_TASK, null, null);
        try {
            Path owner = privateRoot.toAbsolutePath().normalize();
            if (Files.exists(owner, LinkOption.NOFOLLOW_LINKS)
                    && Files.isSymbolicLink(owner)) return result(Code.SYMLINK_REJECTED, null, null);
            Files.createDirectories(owner);
            if (Files.isSymbolicLink(owner)) return result(Code.SYMLINK_REJECTED, null, null);
            Path base = owner.resolve(WORKSPACES_DIR).resolve(task.taskId).normalize();
            if (!base.startsWith(owner.resolve(WORKSPACES_DIR)))
                return result(Code.PATH_ESCAPE, null, null);
            if (Files.exists(base, LinkOption.NOFOLLOW_LINKS)) return result(Code.EXISTS, null, null);
            Path input = base.resolve(INPUT_DIR);
            Path output = base.resolve(OUTPUT_DIR);
            Files.createDirectories(input);
            Files.createDirectories(output);
            if (containsSymbolicLink(owner, input) || containsSymbolicLink(owner, output))
                return result(Code.SYMLINK_REJECTED, null, null);
            return result(Code.CREATED, new Lease(task.taskId, owner, input, output), null);
        } catch (IOException | SecurityException failure) {
            return result(Code.IO_FAILURE, null, null);
        }
    }

    static Result resolve(Lease lease, Area area, String relativePath) {
        if (lease == null || area == null || lease.ownerRoot == null || lease.rootFor(area) == null)
            return result(Code.WRONG_LEASE, null, null);
        if (!validRelativePath(relativePath)) return result(Code.INVALID_RELATIVE_PATH, null, null);
        try {
            Path root = lease.rootFor(area);
            if (containsSymbolicLink(lease.ownerRoot, root))
                return result(Code.SYMLINK_REJECTED, lease, null);
            Path candidate = root.resolve(relativePath).normalize();
            if (!candidate.startsWith(root)) return result(Code.PATH_ESCAPE, lease, null);
            if (containsSymbolicLink(root, candidate)) return result(Code.SYMLINK_REJECTED, lease, null);
            return result(Code.CREATED, lease, candidate);
        } catch (IOException | SecurityException failure) {
            return result(Code.IO_FAILURE, lease, null);
        }
    }

    private static boolean validTask(OpenCodeTaskProtocol.Task task) {
        return task != null && task.schemaVersion == OpenCodeTaskProtocol.SCHEMA_VERSION
                && task.taskId != null && task.taskId.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}")
                && task.audience == ToolCatalog.Audience.OPENCODE_GATEWAY
                && task.dryRun && task.requiresConfirmation
                && OpenCodeTaskProtocol.CONFIRMATION_STATE.equals(task.confirmationState)
                && OpenCodeTaskProtocol.EXECUTION_STATE.equals(task.executionState);
    }

    private static boolean validRelativePath(String value) {
        if (value == null || value.isEmpty() || value.length() > 512) return false;
        Path parsed;
        try { parsed = java.nio.file.Paths.get(value); }
        catch (RuntimeException invalid) { return false; }
        if (parsed.isAbsolute()) return false;
        for (Path part : parsed) {
            String name = part.toString();
            if (name.isEmpty() || ".".equals(name) || "..".equals(name)) return false;
        }
        return parsed.getNameCount() > 0;
    }

    private static boolean containsSymbolicLink(Path root, Path target) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedTarget = target.toAbsolutePath().normalize();
        if (!normalizedTarget.startsWith(normalizedRoot)) return true;
        Path current = normalizedRoot;
        if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) return true;
        Path relative = normalizedRoot.relativize(normalizedTarget);
        for (Path part : relative) {
            current = current.resolve(part);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current))
                return true;
        }
        return false;
    }

    private static Result result(Code code, Lease lease, Path path) {
        return new Result(code, lease, path);
    }
}
