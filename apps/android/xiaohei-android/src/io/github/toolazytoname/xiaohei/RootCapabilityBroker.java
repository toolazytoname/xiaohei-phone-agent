package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** In-memory fixed-action root authorization core. It never invokes su, shell, or device APIs. */
final class RootCapabilityBroker {
    enum Action { READ_SERVICE_STATUS, READ_BATTERY_STATUS, READ_AUDIO_STATUS }
    enum Decision { ALLOW_ONCE, DENY_MISSING, DENY_TIER, DENY_SIGNATURE, DENY_ACTION, DENY_PARAMETERS, DENY_REPLAY }
    static final class Request { final String requestId, signer; final Action action; final String parameters; Request(String i,String s,Action a,String p){requestId=i;signer=s;action=a;parameters=p;} }
    static final class Result { final Decision decision; final int modelCalls=0, executionCalls=0; Result(Decision d){decision=d;} }
    private static final Set<String> SIGNERS=Collections.singleton("xiaohei-root-broker-v1");
    private final Set<String> consumed=new HashSet<>();
    synchronized Result authorize(Request r){
        if(r==null||r.requestId==null||r.signer==null||r.action==null||r.parameters==null)return new Result(Decision.DENY_MISSING);
        if(!SIGNERS.contains(r.signer))return new Result(Decision.DENY_SIGNATURE);
        if(!r.requestId.matches("root-request-[A-Za-z0-9-]{8,64}"))return new Result(Decision.DENY_ACTION);
        if(consumed.contains(r.requestId))return new Result(Decision.DENY_REPLAY);
        if(!"{}".equals(r.parameters))return new Result(Decision.DENY_PARAMETERS);
        consumed.add(r.requestId); return new Result(Decision.ALLOW_ONCE);
    }
}
