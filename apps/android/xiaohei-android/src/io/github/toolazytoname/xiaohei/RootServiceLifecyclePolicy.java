package io.github.toolazytoname.xiaohei;

/** Fixed-target lifecycle preflight. It authorizes no process signal and only emits dry-run decisions. */
final class RootServiceLifecyclePolicy {
    enum Decision { ALLOW_DRY_RUN, DENY_MISSING, DENY_ACTION, DENY_PACKAGE, DENY_PROCESS, DENY_PID, DENY_PORT, DENY_CONFIRMATION }
    enum Action { START, STOP }
    static final class Target {
        final String packageName, processName; final int pid, port;
        Target(String packageName, String processName, int pid, int port) { this.packageName=packageName; this.processName=processName; this.pid=pid; this.port=port; }
    }
    static final class Request { final Action action; final Target expected, observed; final boolean freshConfirmation;
        Request(Action action, Target expected, Target observed, boolean freshConfirmation) { this.action=action; this.expected=expected; this.observed=observed; this.freshConfirmation=freshConfirmation; } }
    static final class Result { final Decision decision; final int signalCalls=0, executionCalls=0; Result(Decision decision){this.decision=decision;} }
    static Result preflight(Request request) {
        if(request==null||request.action==null||request.expected==null||request.observed==null)return new Result(Decision.DENY_MISSING);
        if(request.action!=Action.STOP)return new Result(Decision.DENY_ACTION);
        if(!request.freshConfirmation)return new Result(Decision.DENY_CONFIRMATION);
        if(!sameText(request.expected.packageName,request.observed.packageName))return new Result(Decision.DENY_PACKAGE);
        if(!sameText(request.expected.processName,request.observed.processName))return new Result(Decision.DENY_PROCESS);
        if(request.expected.pid<=0||request.expected.pid!=request.observed.pid)return new Result(Decision.DENY_PID);
        if(request.expected.port<1||request.expected.port>65535||request.expected.port!=request.observed.port)return new Result(Decision.DENY_PORT);
        return new Result(Decision.ALLOW_DRY_RUN);
    }
    private static boolean sameText(String left,String right){return left!=null&&left.equals(right)&&left.length()<=96;}
}
