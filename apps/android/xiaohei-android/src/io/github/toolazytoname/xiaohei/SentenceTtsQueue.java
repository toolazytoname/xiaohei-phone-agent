package io.github.toolazytoname.xiaohei;

import java.util.ArrayDeque;

/** Generation-bound sentence queue; cancellation makes all older completion events inert. */
final class SentenceTtsQueue {
    static final class Next {
        final long generation;
        final int sequence;
        final String sentence;
        Next(long generation, int sequence, String sentence) {
            this.generation = generation;
            this.sequence = sequence;
            this.sentence = sentence;
        }
    }

    private final ArrayDeque<String> queued = new ArrayDeque<>();
    private long generation;
    private int sequence;
    private boolean speaking;

    synchronized Next replace(String text) {
        queued.clear();
        speaking = false;
        sequence = 0;
        generation++;
        if (text == null) return null;
        for (String raw : text.split("(?<=[。！？.!?])")) {
            String sentence = raw.trim();
            if (!sentence.isEmpty()) queued.add(sentence);
        }
        return next();
    }

    synchronized Next complete(long completedGeneration) {
        if (completedGeneration != generation || !speaking) return null;
        speaking = false;
        return next();
    }

    synchronized void cancel() {
        queued.clear();
        speaking = false;
        sequence = 0;
        generation++;
    }

    synchronized int pending() { return queued.size() + (speaking ? 1 : 0); }
    synchronized long generation() { return generation; }

    private Next next() {
        if (speaking || queued.isEmpty()) return null;
        speaking = true;
        return new Next(generation, ++sequence, queued.remove());
    }
}
