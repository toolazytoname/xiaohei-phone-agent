package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Closed request grammar for the app's disposable MediaStore test collection. */
final class MediaTestCollectionPolicy {
    static final String RELATIVE_PATH = "Pictures/XiaoheiTest/";
    static final int MAX_NAME_LENGTH = 96;

    enum Operation { QUERY, COPY, MOVE, ROLLBACK }
    enum Code { OK, UNKNOWN_FIELD, INVALID_OPERATION, INVALID_SOURCE, INVALID_DESTINATION, INVALID_ROLLBACK }

    static final class Request {
        final Operation operation;
        final long sourceId;
        final String destinationName;
        final String rollbackId;
        private Request(Operation operation, long sourceId, String destinationName, String rollbackId) {
            this.operation = operation;
            this.sourceId = sourceId;
            this.destinationName = destinationName;
            this.rollbackId = rollbackId;
        }
    }

    private MediaTestCollectionPolicy() { }

    static Request parse(Map<String, String> raw) {
        Map<String, String> values = raw == null ? Collections.<String, String>emptyMap() : raw;
        for (String key : values.keySet()) {
            if (!"operation".equals(key) && !"source_id".equals(key)
                    && !"destination_name".equals(key) && !"rollback_id".equals(key)) return null;
        }
        Operation op;
        try { op = Operation.valueOf(value(values, "operation").toUpperCase(java.util.Locale.ROOT)); }
        catch (RuntimeException invalid) { return null; }
        if (op == Operation.QUERY) return values.size() == 1 ? new Request(op, 0, "", "") : null;
        if (op == Operation.ROLLBACK) {
            String id = value(values, "rollback_id");
            return values.size() == 2 && id.matches("[a-z0-9-]{8,64}") ? new Request(op, 0, "", id) : null;
        }
        long source;
        try { source = Long.parseLong(value(values, "source_id")); } catch (RuntimeException invalid) { return null; }
        String name = value(values, "destination_name");
        if (values.size() != 3 || source <= 0 || !safeName(name)) return null;
        return new Request(op, source, name, "");
    }

    static Code validate(Map<String, String> raw) {
        if (raw == null) return Code.INVALID_OPERATION;
        for (String key : raw.keySet()) if (!"operation".equals(key) && !"source_id".equals(key)
                && !"destination_name".equals(key) && !"rollback_id".equals(key)) return Code.UNKNOWN_FIELD;
        String op = value(raw, "operation");
        if (!"query".equals(op) && !"copy".equals(op) && !"move".equals(op) && !"rollback".equals(op)) return Code.INVALID_OPERATION;
        if ("query".equals(op)) return raw.size() == 1 ? Code.OK : Code.UNKNOWN_FIELD;
        if ("rollback".equals(op)) return raw.size() == 2 && value(raw, "rollback_id").matches("[a-z0-9-]{8,64}") ? Code.OK : Code.INVALID_ROLLBACK;
        try { if (Long.parseLong(value(raw, "source_id")) <= 0) return Code.INVALID_SOURCE; }
        catch (RuntimeException invalid) { return Code.INVALID_SOURCE; }
        return raw.size() == 3 && safeName(value(raw, "destination_name")) ? Code.OK : Code.INVALID_DESTINATION;
    }

    static Map<String, String> request(String operation, String sourceId, String destinationName, String rollbackId) {
        Map<String, String> values = new HashMap<>();
        values.put("operation", operation);
        if (sourceId != null) values.put("source_id", sourceId);
        if (destinationName != null) values.put("destination_name", destinationName);
        if (rollbackId != null) values.put("rollback_id", rollbackId);
        return values;
    }

    private static String value(Map<String, String> values, String key) { String value = values.get(key); return value == null ? "" : value; }
    private static boolean safeName(String value) {
        return value != null && value.length() >= 1 && value.length() <= MAX_NAME_LENGTH
                && value.matches("[A-Za-z0-9][A-Za-z0-9._-]*") && !".".equals(value) && !"..".equals(value);
    }
}
