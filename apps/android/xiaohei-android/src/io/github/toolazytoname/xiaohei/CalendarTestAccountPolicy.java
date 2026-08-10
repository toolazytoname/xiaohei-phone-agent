package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.Map;

/** Closed calendar request grammar; it never accepts account names or arbitrary content. */
final class CalendarTestAccountPolicy {
    static final String ACCOUNT_NAME = "xiaohei-test";
    static final long MAX_DURATION_MS = 7L * 24 * 60 * 60 * 1000;
    enum Operation { PREVIEW, CREATE, ROLLBACK }
    static final class Request { final Operation operation; final long calendarId, startMs, endMs, eventId; final String title;
        Request(Operation o,long c,long s,long e,long id,String t){operation=o;calendarId=c;startMs=s;endMs=e;eventId=id;title=t;} }
    private CalendarTestAccountPolicy() { }
    static Request parse(Map<String,String> raw) {
        Map<String,String> v=raw==null?Collections.<String,String>emptyMap():raw; for(String k:v.keySet()) if(!"operation".equals(k)&&!"calendar_id".equals(k)&&!"title".equals(k)&&!"start_ms".equals(k)&&!"end_ms".equals(k)&&!"event_id".equals(k)) return null;
        String op=value(v,"operation"); if("rollback".equals(op)) { long id=num(value(v,"event_id")); return v.size()==2&&id>0?new Request(Operation.ROLLBACK,0,0,0,id,""):null; }
        if(!"preview".equals(op)&&!"create".equals(op)) return null; long calendar=num(value(v,"calendar_id")), start=num(value(v,"start_ms")), end=num(value(v,"end_ms")); String title=value(v,"title");
        if(v.size()!=5||calendar<=0||start<=0||end<=start||end-start>MAX_DURATION_MS||!title.matches("[^\\r\\n]{1,120}"))return null;
        return new Request("preview".equals(op)?Operation.PREVIEW:Operation.CREATE,calendar,start,end,0,title.trim());
    }
    private static long num(String v){try{return Long.parseLong(v);}catch(RuntimeException bad){return 0;}}
    private static String value(Map<String,String> v,String k){String x=v.get(k);return x==null?"":x;}
}
