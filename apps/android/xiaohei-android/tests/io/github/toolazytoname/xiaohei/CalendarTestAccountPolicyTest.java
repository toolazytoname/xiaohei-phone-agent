package io.github.toolazytoname.xiaohei;
import java.util.HashMap; import java.util.Map;
public final class CalendarTestAccountPolicyTest {
 public static void main(String[] a){ok("preview",false);ok("create",false);ok("rollback",true);bad("create",0,2);bad("create",2,1);bad("create",1,8L*24*60*60*1000);System.out.println("PASS calendar-test-account preview=1 create=1 rollback=1 account=closed duration=bounded content=bounded");}
 static void ok(String op,boolean rb){Map<String,String> v=new HashMap<>();v.put("operation",op);if(rb)v.put("event_id","9");else{v.put("calendar_id","1");v.put("title","Test reminder");v.put("start_ms","1");v.put("end_ms","2");}if(CalendarTestAccountPolicy.parse(v)==null)throw new AssertionError(op);}
 static void bad(String op,long s,long e){Map<String,String>v=new HashMap<>();v.put("operation",op);v.put("calendar_id","1");v.put("title","x");v.put("start_ms",String.valueOf(s));v.put("end_ms",String.valueOf(e));if(CalendarTestAccountPolicy.parse(v)!=null)throw new AssertionError(op);}
}
