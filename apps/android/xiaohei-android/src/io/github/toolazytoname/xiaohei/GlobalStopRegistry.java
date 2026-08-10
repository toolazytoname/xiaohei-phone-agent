package io.github.toolazytoname.xiaohei;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fail-closed in-memory fan-out for explicitly registered runtime owners. It never starts,
 * discovers, or kills a resource by name; a failed stop is visible and never reported as stopped.
 */
final class GlobalStopRegistry {
    enum Resource { VOICE, DSP, CPU_WAKE, CONVERSATION, PHONE_AGENT, TOOL, OPENCODE, ROOT }
    enum State { IDLE, STOPPING, STOPPED, FAILED }
    enum Code { STOPPED, NOTHING_ACTIVE, ALREADY_TERMINAL, REGISTRATION_DENIED, STOP_FAILED }

    interface Owner { boolean stop(); }

    static final class Result {
        final Code code;
        final int requested;
        final int stopped;
        final int failed;
        final boolean allResourcesReleased;
        private Result(Code code, int requested, int stopped, int failed) {
            this.code = code;
            this.requested = requested;
            this.stopped = stopped;
            this.failed = failed;
            this.allResourcesReleased = code == Code.STOPPED || code == Code.NOTHING_ACTIVE;
        }
    }

    private final Map<Resource, Owner> owners = new EnumMap<>(Resource.class);
    private State state = State.IDLE;

    synchronized State state() { return state; }

    synchronized boolean register(Resource resource, Owner owner) {
        if (state != State.IDLE || resource == null || owner == null || owners.containsKey(resource)) return false;
        owners.put(resource, owner);
        return true;
    }

    synchronized Result stopAll() {
        if (state == State.STOPPED || state == State.FAILED || state == State.STOPPING)
            return new Result(Code.ALREADY_TERMINAL, 0, 0, 0);
        if (owners.isEmpty()) {
            state = State.STOPPED;
            return new Result(Code.NOTHING_ACTIVE, 0, 0, 0);
        }
        state = State.STOPPING;
        int stopped = 0;
        int failed = 0;
        for (Owner owner : owners.values()) {
            try {
                if (owner.stop()) stopped++; else failed++;
            } catch (RuntimeException ignored) {
                failed++;
            }
        }
        owners.clear();
        state = failed == 0 ? State.STOPPED : State.FAILED;
        return new Result(failed == 0 ? Code.STOPPED : Code.STOP_FAILED, stopped + failed, stopped, failed);
    }
}
