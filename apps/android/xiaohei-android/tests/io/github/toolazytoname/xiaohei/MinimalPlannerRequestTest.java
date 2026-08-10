package io.github.toolazytoname.xiaohei;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public final class MinimalPlannerRequestTest {
    public static void main(String[] args) {
        MinimalPlannerRequest request = MinimalPlannerRequest.create(8, 60000);
        require(request != null && request.dryRun && request.stepBudget == 8
                && request.timeoutMs == 60000 && request.catalogVersion == 1,
                "valid bounded envelope");
        require(MinimalPlannerRequest.create(0, 1000) == null, "zero steps rejected");
        require(MinimalPlannerRequest.create(9, 1000) == null, "too many steps rejected");
        require(MinimalPlannerRequest.create(1, 999) == null, "short timeout rejected");
        require(MinimalPlannerRequest.create(1, 60001) == null, "long timeout rejected");

        Set<String> fields = new HashSet<>();
        for (Field field : MinimalPlannerRequest.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) fields.add(field.getName());
        }
        require(fields.size() == 5 && fields.contains("action") && fields.contains("dryRun")
                && fields.contains("stepBudget") && fields.contains("timeoutMs")
                && fields.contains("catalogVersion"), "fixed five-field envelope");
        System.out.println("PASS minimal-planner-request bounds=4 fields=5 transport=0 execution=0");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
