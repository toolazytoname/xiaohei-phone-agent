package io.github.toolazytoname.xiaohei;

/**
 * Closed semantic operations for the user-enabled Accessibility service.
 * Text entry is intentionally unavailable until a visible, per-request confirmation surface
 * owns both the target and the text. Generic system approval controls are never operations.
 */
final class SemanticAccessibilityOperationPolicy {
    enum Operation { SELECT_EXACT_LABEL, SCROLL_FORWARD, SCROLL_BACKWARD, NAVIGATE_BACK, SET_TEXT }
    enum Decision { ALLOW, DENY_INVALID, DENY_GENERIC_APPROVAL, DENY_TEXT_ENTRY }

    static Decision assess(Operation operation, String label) {
        if (operation == null) return Decision.DENY_INVALID;
        if (operation == Operation.SET_TEXT) return Decision.DENY_TEXT_ENTRY;
        if (operation == Operation.SELECT_EXACT_LABEL) {
            if (label == null || label.trim().isEmpty() || label.length() > 120)
                return Decision.DENY_INVALID;
            return AgentPolicy.assess("", "", label) == AgentPolicy.Decision.ALLOW
                ? Decision.ALLOW : Decision.DENY_GENERIC_APPROVAL;
        }
        return label == null || label.isEmpty() ? Decision.ALLOW : Decision.DENY_INVALID;
    }

    private SemanticAccessibilityOperationPolicy() {}
}
