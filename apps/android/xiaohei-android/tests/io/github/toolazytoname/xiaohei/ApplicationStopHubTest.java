package io.github.toolazytoname.xiaohei;
public final class ApplicationStopHubTest {
 public static void main(String[]x){int[] calls={0};ApplicationStopHub.Registration a=ApplicationStopHub.register(GlobalStopRegistry.Resource.CONVERSATION,()->{calls[0]++;return true;});ApplicationStopHub.Registration b=ApplicationStopHub.register(GlobalStopRegistry.Resource.TOOL,()->false);a.close();ApplicationStopHub.Result r=ApplicationStopHub.stopAll();if(r.requested!=1||r.failed!=1||calls[0]!=0)throw new AssertionError();b.close();System.out.println("PASS application-stop-hub explicit=2 unregister=1 stop_once=1 failed=visible retention=0");}
}
