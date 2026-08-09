package io.github.toolazytoname.xiaohei;

import java.time.Instant;
import java.util.UUID;

/** A redacted, in-memory form of contracts/wakeword-event.v1.schema.json. */
final class WakewordEvent {
    enum Source { APP_BUTTON, DSP, CPU_KWS }

    final String eventId;
    final Source source;
    final String keywordId;
    final int confidence;
    final boolean captureAvailable;
    final Instant occurredAt;

    WakewordEvent(Source source, String keywordId, int confidence, boolean captureAvailable) {
        this.eventId = UUID.randomUUID().toString();
        this.source = source;
        this.keywordId = keywordId;
        this.confidence = confidence;
        this.captureAvailable = captureAvailable;
        this.occurredAt = Instant.now();
    }
}
