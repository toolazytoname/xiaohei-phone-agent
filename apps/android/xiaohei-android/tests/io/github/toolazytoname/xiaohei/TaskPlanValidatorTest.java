package io.github.toolazytoname.xiaohei;
public final class TaskPlanValidatorTest { public static void main(String[] a){
 TaskPlanValidator.Step x=new TaskPlanValidator.Step("s1","android.open_gallery",ToolCatalog.Risk.LOW,"","k1");
 if(!"ok".equals(TaskPlanValidator.validate(new TaskPlanValidator.Step[]{x})))throw new AssertionError();
 if(!"tool".equals(TaskPlanValidator.validate(new TaskPlanValidator.Step[]{new TaskPlanValidator.Step("s1","root.shell",ToolCatalog.Risk.HIGH,"","k1")})))throw new AssertionError();
 if(!"dependency".equals(TaskPlanValidator.validate(new TaskPlanValidator.Step[]{new TaskPlanValidator.Step("s1","android.open_gallery",ToolCatalog.Risk.LOW,"s2","k1")})))throw new AssertionError();
 System.out.println("PASS task-plan allowed=1 unknown=deny cycle=deny");}}
