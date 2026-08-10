package io.github.toolazytoname.xiaohei;
public final class ToolOutcomeEvidenceGateTest { public static void main(String[] x) {
  expect(ToolOutcomeEvidenceGate.Decision.VERIFIED, gate().verify(true, o("com.android.settings",2)));
  expect(ToolOutcomeEvidenceGate.Decision.ADAPTER_FAILED, gate().verify(false,o("com.android.settings",2)));
  expect(ToolOutcomeEvidenceGate.Decision.STALE_OBSERVATION, gate().verify(true,o("com.android.settings",1)));
  expect(ToolOutcomeEvidenceGate.Decision.POSTCONDITION_MISMATCH, gate().verify(true,o("com.android.gallery3d",2)));
  ToolOutcomeEvidenceGate g=gate(); g.verify(true,o("com.android.settings",2)); expect(ToolOutcomeEvidenceGate.Decision.INVALID,g.verify(true,o("com.android.settings",3)));
  System.out.println("PASS tool-outcome-evidence adapter=not_enough fresh=required mismatch=deny retry=deny text=0 image=0 execution=0"); }
 static ToolOutcomeEvidenceGate gate(){return new ToolOutcomeEvidenceGate("com.android.settings",o("com.android.launcher",1));} static ToolOutcomeEvidenceGate.Observation o(String p,long s){return new ToolOutcomeEvidenceGate.Observation(p,s);} static void expect(Object a,Object b){if(a!=b)throw new AssertionError(String.valueOf(b));}}
