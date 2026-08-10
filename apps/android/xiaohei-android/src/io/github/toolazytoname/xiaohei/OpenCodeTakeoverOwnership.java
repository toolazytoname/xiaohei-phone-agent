package io.github.toolazytoname.xiaohei;

/** Transfers display/control ownership only; it cannot start, resume, or duplicate a task. */
final class OpenCodeTakeoverOwnership {
    enum Owner { LOCAL, WEB, NONE }
    enum Decision { TAKEN_OVER, RETURNED, DENIED, ALREADY_OWNED, TERMINAL }
    private Owner owner = Owner.LOCAL;
    private boolean terminal;

    Owner owner() { return owner; }
    Decision takeOver(String verifiedWebSession) {
        if (terminal) return Decision.TERMINAL;
        if (!valid(verifiedWebSession)) return Decision.DENIED;
        if (owner == Owner.WEB) return Decision.ALREADY_OWNED;
        owner = Owner.WEB;
        return Decision.TAKEN_OVER;
    }
    Decision returnToLocal(String verifiedWebSession) {
        if (terminal) return Decision.TERMINAL;
        if (!valid(verifiedWebSession) || owner != Owner.WEB) return Decision.DENIED;
        owner = Owner.LOCAL;
        return Decision.RETURNED;
    }
    void markTerminal() { terminal = true; owner = Owner.NONE; }
    private static boolean valid(String value) { return value != null && value.matches("web-[A-Za-z0-9._:-]{8,63}"); }
}
