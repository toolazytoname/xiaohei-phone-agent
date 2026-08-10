package io.github.toolazytoname.xiaohei;
public final class FailureFingerprintTest { public static void main(String[] a) {
 String x=FailureFingerprint.of("task-1","android.observe","settings","v1","timeout");
 if(FailureFingerprint.canRecover(x,x,false)||!FailureFingerprint.canRecover(x,x+"2",false)||FailureFingerprint.canRecover(x,x+"2",true)) throw new AssertionError();
 System.out.println("PASS failure-fingerprint unchanged_retry=reject recovery_once=true"); }}
