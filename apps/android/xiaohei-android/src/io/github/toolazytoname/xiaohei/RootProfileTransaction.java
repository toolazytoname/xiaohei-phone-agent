package io.github.toolazytoname.xiaohei;

/** Pure transaction ledger for a fixed profile identity; no installer or device operation exists here. */
final class RootProfileTransaction {
    enum State { NEW, PRECHECKED, SNAPSHOTTED, APPLIED, ROLLED_BACK, REBOOT_VERIFIED, FAILED }
    enum Decision { ALLOW, DENY_STATE, DENY_PROFILE, DENY_DIGEST }
    private static final String PROFILE="xiaohei-device-profile-v1";
    private State state=State.NEW; private String snapshotDigest="";
    Decision precheck(String profile,String digest){if(state!=State.NEW)return Decision.DENY_STATE;if(!PROFILE.equals(profile))return Decision.DENY_PROFILE;if(!validDigest(digest))return Decision.DENY_DIGEST;state=State.PRECHECKED;return Decision.ALLOW;}
    Decision snapshot(String digest){if(state!=State.PRECHECKED)return Decision.DENY_STATE;if(!validDigest(digest))return Decision.DENY_DIGEST;snapshotDigest=digest;state=State.SNAPSHOTTED;return Decision.ALLOW;}
    Decision markApplied(){if(state!=State.SNAPSHOTTED)return Decision.DENY_STATE;state=State.APPLIED;return Decision.ALLOW;}
    Decision rollback(String digest){if(state!=State.APPLIED)return Decision.DENY_STATE;if(!snapshotDigest.equals(digest))return Decision.DENY_DIGEST;state=State.ROLLED_BACK;return Decision.ALLOW;}
    Decision verifyAfterReboot(String observedDigest){if(state!=State.APPLIED&&state!=State.ROLLED_BACK)return Decision.DENY_STATE;if(!validDigest(observedDigest))return Decision.DENY_DIGEST;state=State.REBOOT_VERIFIED;return Decision.ALLOW;}
    State state(){return state;}
    private static boolean validDigest(String value){return value!=null&&value.matches("[a-f0-9]{64}");}
}
