package io.github.toolazytoname.xiaohei;

public final class SemanticAccessibilityOperationPolicyTest {
    public static void main(String[] args) {
        allow(SemanticAccessibilityOperationPolicy.Operation.SELECT_EXACT_LABEL, "网络和互联网");
        allow(SemanticAccessibilityOperationPolicy.Operation.SCROLL_FORWARD, null);
        allow(SemanticAccessibilityOperationPolicy.Operation.SCROLL_BACKWARD, null);
        allow(SemanticAccessibilityOperationPolicy.Operation.NAVIGATE_BACK, null);
        deny(SemanticAccessibilityOperationPolicy.Operation.SELECT_EXACT_LABEL, "允许",
            SemanticAccessibilityOperationPolicy.Decision.DENY_GENERIC_APPROVAL);
        deny(SemanticAccessibilityOperationPolicy.Operation.SELECT_EXACT_LABEL, "下一步",
            SemanticAccessibilityOperationPolicy.Decision.DENY_GENERIC_APPROVAL);
        deny(SemanticAccessibilityOperationPolicy.Operation.SET_TEXT, "hello",
            SemanticAccessibilityOperationPolicy.Decision.DENY_TEXT_ENTRY);
        deny(SemanticAccessibilityOperationPolicy.Operation.SCROLL_FORWARD, "extra",
            SemanticAccessibilityOperationPolicy.Decision.DENY_INVALID);
        deny(null, null, SemanticAccessibilityOperationPolicy.Decision.DENY_INVALID);
        System.out.println("PASS semantic-accessibility operation allow=4 deny=5");
    }

    private static void allow(SemanticAccessibilityOperationPolicy.Operation operation, String label) {
        deny(operation, label, SemanticAccessibilityOperationPolicy.Decision.ALLOW);
    }

    private static void deny(SemanticAccessibilityOperationPolicy.Operation operation, String label,
            SemanticAccessibilityOperationPolicy.Decision expected) {
        SemanticAccessibilityOperationPolicy.Decision actual =
            SemanticAccessibilityOperationPolicy.assess(operation, label);
        if (actual != expected) throw new AssertionError("expected=" + expected + " actual=" + actual);
    }
}
