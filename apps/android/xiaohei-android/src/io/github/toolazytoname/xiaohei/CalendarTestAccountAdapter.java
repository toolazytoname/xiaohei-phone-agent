package io.github.toolazytoname.xiaohei;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.provider.CalendarContract;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/** Calendar adapter restricted to a manually provisioned xiaohei-test account. */
final class CalendarTestAccountAdapter implements ToolExecutionCoordinator.Adapter {
    private final ContentResolver resolver; private final Set<Long> created = new HashSet<>();
    CalendarTestAccountAdapter(Context context) { resolver=context.getApplicationContext().getContentResolver(); }
    @Override public ToolExecutionCoordinator.AdapterResponse execute(ToolGateway.Call call, ToolExecutionCoordinator.CancellationSignal cancel) throws ToolExecutionCoordinator.AdapterFailure {
        CalendarTestAccountPolicy.Request r=call==null||!"android.calendar_test_account".equals(call.tool)?null:CalendarTestAccountPolicy.parse(call.arguments);
        if(r==null||cancel.isCancelled())throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.INVALID_OUTPUT);
        if(r.operation==CalendarTestAccountPolicy.Operation.ROLLBACK) return rollback(r.eventId);
        if(!verified(r.calendarId))throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE);
        Map<String,String> out=new HashMap<>(); out.put("operation",r.operation.name().toLowerCase(java.util.Locale.ROOT)); out.put("duration_ms",String.valueOf(r.endMs-r.startMs));
        if(r.operation==CalendarTestAccountPolicy.Operation.PREVIEW)return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS,out);
        ContentValues v=new ContentValues(); v.put(CalendarContract.Events.CALENDAR_ID,r.calendarId);v.put(CalendarContract.Events.TITLE,r.title);v.put(CalendarContract.Events.DTSTART,r.startMs);v.put(CalendarContract.Events.DTEND,r.endMs);v.put(CalendarContract.Events.EVENT_TIMEZONE,TimeZone.getDefault().getID());v.put(CalendarContract.Events.HAS_ALARM,0);
        android.net.Uri uri=resolver.insert(CalendarContract.Events.CONTENT_URI,v);if(uri==null)throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE);long id=android.content.ContentUris.parseId(uri);created.add(id);out.put("rollback_id",String.valueOf(id));return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS,out);
    }
    private boolean verified(long id){try(android.database.Cursor c=resolver.query(CalendarContract.Calendars.CONTENT_URI,new String[]{CalendarContract.Calendars._ID},CalendarContract.Calendars._ID+"=? AND "+CalendarContract.Calendars.ACCOUNT_NAME+"=?",new String[]{String.valueOf(id),CalendarTestAccountPolicy.ACCOUNT_NAME},null)){return c!=null&&c.moveToFirst();}}
    private ToolExecutionCoordinator.AdapterResponse rollback(long id)throws ToolExecutionCoordinator.AdapterFailure{if(!created.remove(id)||resolver.delete(android.content.ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,id),null,null)!=1)throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE);Map<String,String> out=new HashMap<>();out.put("rolled_back","true");return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS,out);}
}
