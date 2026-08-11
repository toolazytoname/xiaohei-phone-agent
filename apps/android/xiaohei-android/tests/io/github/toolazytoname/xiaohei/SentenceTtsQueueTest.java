package io.github.toolazytoname.xiaohei;

public final class SentenceTtsQueueTest {
    public static void main(String[] args) {
        SentenceTtsQueue queue = new SentenceTtsQueue();
        SentenceTtsQueue.Next first = queue.replace("第一句。第二句！第三句？");
        require(first != null && first.sequence == 1 && first.sentence.equals("第一句。"));
        require(queue.pending() == 3);

        SentenceTtsQueue.Next second = queue.complete(first.generation);
        require(second != null && second.sequence == 2 && second.sentence.equals("第二句！"));
        require(queue.pending() == 2);

        SentenceTtsQueue.Next replacement = queue.replace("替换句。尾句。");
        require(replacement != null && replacement.sequence == 1);
        require(replacement.generation != second.generation);
        require(queue.complete(second.generation) == null);
        require(queue.pending() == 2);

        long cancelledGeneration = replacement.generation;
        queue.cancel();
        require(queue.generation() != cancelledGeneration);
        require(queue.pending() == 0);
        require(queue.complete(cancelledGeneration) == null);
        System.out.println("PASS sentence-tts first=immediate sequences=1..2 replacement=invalidates cancel=clears stale_completion=2 overlap=0 execution=0");
    }

    private static void require(boolean value) { if (!value) throw new AssertionError("unexpected queue state"); }
}
