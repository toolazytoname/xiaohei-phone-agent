package io.github.toolazytoname.xiaohei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Projects adapter-supplied state into a fixed, content-free root diagnostic result. */
final class RootReadOnlyDiagnostics {
    enum Category { SERVICE, PORT, BATTERY, AUDIO, PACKAGE, PROFILE }
    enum State { AVAILABLE, UNAVAILABLE, UNKNOWN }
    static final class Snapshot {
        private final Map<Category, State> states = new EnumMap<>(Category.class);
        Snapshot state(Category category, State state) { states.put(category, state); return this; }
        State stateFor(Category category) { State state = states.get(category); return state == null ? State.UNKNOWN : state; }
    }
    static final class Entry {
        final Category category; final State state; final String label;
        Entry(Category category, State state) { this.category = category; this.state = state; this.label = labelFor(category); }
    }
    static final class Result {
        final List<Entry> entries; final int modelCalls = 0, executionCalls = 0;
        Result(List<Entry> entries) { this.entries = Collections.unmodifiableList(entries); }
    }
    private static final Category[] SERVICE = {Category.SERVICE, Category.PORT, Category.PACKAGE, Category.PROFILE};
    private static final Category[] BATTERY = {Category.BATTERY};
    private static final Category[] AUDIO = {Category.AUDIO};
    static Result project(RootCapabilityBroker.Action action, Snapshot snapshot) {
        if (action == null || snapshot == null) throw new IllegalArgumentException("fixed action and snapshot required");
        Category[] categories = action == RootCapabilityBroker.Action.READ_SERVICE_STATUS ? SERVICE
                : action == RootCapabilityBroker.Action.READ_BATTERY_STATUS ? BATTERY : AUDIO;
        List<Entry> entries = new ArrayList<>();
        for (Category category : categories) entries.add(new Entry(category, snapshot.stateFor(category)));
        return new Result(entries);
    }
    private static String labelFor(Category category) {
        switch (category) {
            case SERVICE: return "service-status";
            case PORT: return "port-status";
            case BATTERY: return "battery-status";
            case AUDIO: return "audio-status";
            case PACKAGE: return "package-status";
            case PROFILE: return "profile-status";
            default: throw new IllegalArgumentException("unknown category");
        }
    }
}
