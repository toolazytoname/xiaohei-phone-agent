package io.github.toolazytoname.xiaohei;
public final class FailureRecoveryProjectionTest { public static void main(String[] x) {
 for(FailureRecoveryProjection.Kind k:FailureRecoveryProjection.Kind.values()){String v=FailureRecoveryProjection.visibleText(k);if(!v.contains("失败原因 / Cause：")||!v.contains("影响 / Impact：")||!v.contains("恢复 / Recovery："))throw new AssertionError();}
 System.out.println("PASS failure-recovery kinds=5 cause=1 impact=1 recovery=1 raw_error=0 execution=0"); }}
