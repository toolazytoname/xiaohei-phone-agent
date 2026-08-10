package io.github.toolazytoname.xiaohei;
import java.util.HashSet;
import java.util.Set;
final class TaskPlanValidator {
    static final class Step { final String id, tool, dependsOn, key; final ToolCatalog.Risk risk; Step(String id,String tool,ToolCatalog.Risk risk,String dependsOn,String key){this.id=id;this.tool=tool;this.risk=risk;this.dependsOn=dependsOn;this.key=key;} }
    static String validate(Step[] steps) {
        if(steps==null||steps.length==0||steps.length>8)return "step_count";
        Set<String> ids=new HashSet<>(), keys=new HashSet<>();
        for(Step s:steps){ if(s==null||!ids.add(s.id)||!keys.add(s.key))return "duplicate"; if(!ToolCatalog.allowed(s.tool,s.risk))return "tool"; if(s.dependsOn!=null&&!s.dependsOn.isEmpty()&&!ids.contains(s.dependsOn))return "dependency"; }
        return "ok";
    }
}
