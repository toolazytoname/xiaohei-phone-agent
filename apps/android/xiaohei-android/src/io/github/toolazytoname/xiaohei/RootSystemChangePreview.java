package io.github.toolazytoname.xiaohei;

/** Fixed system-change dry-run preview; it never applies a system change. */
final class RootSystemChangePreview {
    enum Decision { PREVIEW_READY, DENY_MISSING, DENY_TARGET, DENY_DIGEST, DENY_CONFIRMATION, DENY_EXPIRED }
    static final class Request { final String target,beforeDigest,afterDigest; final boolean freshConfirmation,expired; Request(String t,String b,String a,boolean c,boolean e){target=t;beforeDigest=b;afterDigest=a;freshConfirmation=c;expired=e;} }
    static final class Result { final Decision decision; final String summary; final int applyCalls=0,executionCalls=0; Result(Decision d,String s){decision=d;summary=s;} }
    static Result preview(Request r){if(r==null||r.target==null||r.beforeDigest==null||r.afterDigest==null)return new Result(Decision.DENY_MISSING,"");if(!"xiaohei-device-profile-v1".equals(r.target))return new Result(Decision.DENY_TARGET,"");if(!valid(r.beforeDigest)||!valid(r.afterDigest)||r.beforeDigest.equals(r.afterDigest))return new Result(Decision.DENY_DIGEST,"");if(!r.freshConfirmation)return new Result(Decision.DENY_CONFIRMATION,"");if(r.expired)return new Result(Decision.DENY_EXPIRED,"");return new Result(Decision.PREVIEW_READY,"profile-digest-change");}
    private static boolean valid(String v){return v.matches("[a-f0-9]{64}");}
}
