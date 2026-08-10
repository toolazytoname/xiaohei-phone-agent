package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** In-memory fixed-action root authorization core. It never invokes su, shell, or device APIs. */
final class RootCapabilityBroker {
    enum Action { READ_SERVICE_STATUS, READ_BATTERY_STATUS, READ_AUDIO_STATUS }
    enum Decision { ALLOW_ONCE, DENY_MISSING, DENY_TIER, DENY_SIGNATURE, DENY_ACTION, DENY_PARAMETERS, DENY_REPLAY, DENY_REVOKED }
    static final class Request { final String requestId, signer; final Action action; final String parameters; Request(String i,String s,Action a,String p){requestId=i;signer=s;action=a;parameters=p;} }
    static final class Result { final Decision decision; final int modelCalls=0, executionCalls=0; Result(Decision d){decision=d;} }
    static final class AuditRecord { final int sequence; final String action; final Decision decision; AuditRecord(int sequence, Action action, Decision decision){this.sequence=sequence;this.action=action==null?"unknown":action.name().toLowerCase();this.decision=decision;} }
    private static final Set<String> SIGNERS=Collections.singleton("xiaohei-root-broker-v1");
    private final Set<String> consumed=new HashSet<>();
    private final List<AuditRecord> audit=new ArrayList<>();
    private boolean revoked;
    synchronized Result authorize(Request r){
        Action action=r==null?null:r.action;
        if(revoked)return finish(action,Decision.DENY_REVOKED);
        if(r==null||r.requestId==null||r.signer==null||r.action==null||r.parameters==null)return finish(action,Decision.DENY_MISSING);
        if(!SIGNERS.contains(r.signer))return finish(action,Decision.DENY_SIGNATURE);
        if(!r.requestId.matches("root-request-[A-Za-z0-9-]{8,64}"))return finish(action,Decision.DENY_ACTION);
        if(consumed.contains(r.requestId))return finish(action,Decision.DENY_REPLAY);
        if(!"{}".equals(r.parameters))return finish(action,Decision.DENY_PARAMETERS);
        consumed.add(r.requestId); return finish(action,Decision.ALLOW_ONCE);
    }
    synchronized void revokeAll(){revoked=true;consumed.clear();}
    synchronized boolean revoked(){return revoked;}
    synchronized List<AuditRecord> auditSnapshot(){return Collections.unmodifiableList(new ArrayList<>(audit));}
    private Result finish(Action action,Decision decision){audit.add(new AuditRecord(audit.size()+1,action,decision));return new Result(decision);}
}
