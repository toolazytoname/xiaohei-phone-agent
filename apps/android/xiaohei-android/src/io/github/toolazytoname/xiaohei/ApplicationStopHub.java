package io.github.toolazytoname.xiaohei;

import java.util.EnumMap;
import java.util.Map;

/** Process-wide explicit stop owners. Registration handles prevent Activity retention after destroy. */
final class ApplicationStopHub {
    interface Owner { boolean stop(); }
    interface Registration { void close(); }
    static final class Result { final int requested, stopped, failed; Result(int q,int s,int f){requested=q;stopped=s;failed=f;} boolean allReleased(){return failed==0;} }
    private static final Map<GlobalStopRegistry.Resource, Owner> OWNERS = new EnumMap<>(GlobalStopRegistry.Resource.class);
    private ApplicationStopHub() { }
    static synchronized Registration register(GlobalStopRegistry.Resource resource, Owner owner) {
        if (resource == null || owner == null || OWNERS.containsKey(resource)) return () -> { };
        OWNERS.put(resource, owner);
        return () -> unregister(resource, owner);
    }
    private static synchronized void unregister(GlobalStopRegistry.Resource resource, Owner owner) {
        if (OWNERS.get(resource) == owner) OWNERS.remove(resource);
    }
    static Result stopAll() {
        Map<GlobalStopRegistry.Resource, Owner> snapshot;
        synchronized (ApplicationStopHub.class) { snapshot = new EnumMap<>(OWNERS); OWNERS.clear(); }
        int stopped=0, failed=0; for (Owner owner : snapshot.values()) try { if(owner.stop())stopped++;else failed++; } catch(RuntimeException bad){failed++;}
        return new Result(stopped+failed, stopped, failed);
    }
}
