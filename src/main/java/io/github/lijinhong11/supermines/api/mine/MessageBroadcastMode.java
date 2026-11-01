package io.github.lijinhong11.supermines.api.mine;

import java.util.Locale;

public enum MessageBroadcastMode {
    ALL,
    IN_WORLD,
    IN_MINE,
    SILENT;

    public boolean shouldSend(boolean sameWorld, boolean insideMine) {
        return switch (this) {
            case ALL -> true;
            case IN_WORLD -> sameWorld;
            case IN_MINE -> sameWorld && insideMine;
            case SILENT -> false;
        };
    }

    public MessageBroadcastMode next() {
        return switch (this) {
            case ALL -> IN_WORLD;
            case IN_WORLD -> IN_MINE;
            case IN_MINE -> SILENT;
            case SILENT -> ALL;
        };
    }

    public static MessageBroadcastMode parse(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Reads enum names, accepting booleans only for compatibility with older configurations.
     */
    public static MessageBroadcastMode fromConfig(Object value, MessageBroadcastMode fallback) {
        if (value instanceof Boolean legacy) return legacy ? ALL : IN_MINE;
        if (value instanceof String name) {
            try {
                return parse(name);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return fallback;
    }
}
