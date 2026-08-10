package io.github.toolazytoname.xiaohei;
public final class RootReadOnlyDiagnosticsTest { public static void main(String[] args) {
  RootReadOnlyDiagnostics.Snapshot snapshot=new RootReadOnlyDiagnostics.Snapshot()
    .state(RootReadOnlyDiagnostics.Category.SERVICE,RootReadOnlyDiagnostics.State.AVAILABLE)
    .state(RootReadOnlyDiagnostics.Category.PORT,RootReadOnlyDiagnostics.State.UNAVAILABLE)
    .state(RootReadOnlyDiagnostics.Category.PACKAGE,RootReadOnlyDiagnostics.State.AVAILABLE)
    .state(RootReadOnlyDiagnostics.Category.PROFILE,RootReadOnlyDiagnostics.State.UNKNOWN)
    .state(RootReadOnlyDiagnostics.Category.BATTERY,RootReadOnlyDiagnostics.State.AVAILABLE)
    .state(RootReadOnlyDiagnostics.Category.AUDIO,RootReadOnlyDiagnostics.State.UNAVAILABLE);
  RootReadOnlyDiagnostics.Result service=RootReadOnlyDiagnostics.project(RootCapabilityBroker.Action.READ_SERVICE_STATUS,snapshot);
  RootReadOnlyDiagnostics.Result battery=RootReadOnlyDiagnostics.project(RootCapabilityBroker.Action.READ_BATTERY_STATUS,snapshot);
  RootReadOnlyDiagnostics.Result audio=RootReadOnlyDiagnostics.project(RootCapabilityBroker.Action.READ_AUDIO_STATUS,snapshot);
  if(service.entries.size()!=4||battery.entries.size()!=1||audio.entries.size()!=1)throw new AssertionError();
  if(!"service-status".equals(service.entries.get(0).label)||service.entries.get(1).state!=RootReadOnlyDiagnostics.State.UNAVAILABLE)throw new AssertionError();
  if(service.modelCalls!=0||service.executionCalls!=0)throw new AssertionError();
  try{RootReadOnlyDiagnostics.project(null,snapshot);throw new AssertionError();}catch(IllegalArgumentException expected){}
  try{RootReadOnlyDiagnostics.project(RootCapabilityBroker.Action.READ_AUDIO_STATUS,null);throw new AssertionError();}catch(IllegalArgumentException expected){}
  System.out.println("PASS RootReadOnlyDiagnosticsTest categories=6 bounded_entries=4 content_fields=0 execution_paths=0");
}}
