package io.github.toolazytoname.xiaohei;

public final class AsrProviderTest {
    public static void main(String[] args) {
        require(AsrProvider.fromId("local_command_14m") == AsrProvider.LOCAL_COMMAND_14M, "command id");
        require(AsrProvider.fromId("local_conversation_candidate") == AsrProvider.LOCAL_CONVERSATION_CANDIDATE, "conversation id");
        require(AsrProvider.fromId("android_system") == AsrProvider.ANDROID_SYSTEM, "system id");
        require(AsrProvider.fromId("LOCAL_COMMAND_14M") == null, "unknown rejects");
        System.out.println("PASS asr-provider explicit_ids=3 unknown=reject auto_download=0");
    }
    private static void require(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
