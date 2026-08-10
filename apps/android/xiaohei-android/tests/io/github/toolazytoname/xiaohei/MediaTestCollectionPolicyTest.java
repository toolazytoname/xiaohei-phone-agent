package io.github.toolazytoname.xiaohei;

public final class MediaTestCollectionPolicyTest {
    public static void main(String[] args) {
        ok("query", null, null, null, MediaTestCollectionPolicy.Operation.QUERY);
        ok("copy", "42", "copy-01.jpg", null, MediaTestCollectionPolicy.Operation.COPY);
        ok("move", "8", "move_02.png", null, MediaTestCollectionPolicy.Operation.MOVE);
        ok("rollback", null, null, "0a1b2c3d-1234", MediaTestCollectionPolicy.Operation.ROLLBACK);
        deny("copy", "0", "copy.jpg", null); deny("copy", "4", "../escape.jpg", null);
        deny("move", "four", "file.jpg", null); deny("query", "1", null, null);
        deny("rollback", null, null, "BAD"); deny("delete", "1", "x.jpg", null);
        System.out.println("PASS media-test-collection query=1 copy=1 move=1 rollback=1 path=closed arbitrary_uri=0 batch=0");
    }
    private static void ok(String op, String source, String destination, String rollback, MediaTestCollectionPolicy.Operation expected) {
        MediaTestCollectionPolicy.Request request = MediaTestCollectionPolicy.parse(MediaTestCollectionPolicy.request(op, source, destination, rollback));
        if (request == null || request.operation != expected) throw new AssertionError(op);
    }
    private static void deny(String op, String source, String destination, String rollback) {
        if (MediaTestCollectionPolicy.parse(MediaTestCollectionPolicy.request(op, source, destination, rollback)) != null) throw new AssertionError(op);
    }
}
