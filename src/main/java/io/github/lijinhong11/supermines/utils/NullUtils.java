package io.github.lijinhong11.supermines.utils;

public class NullUtils {
    public static <T> T tryAnyNotNull(T obj, T obj2) {
        return obj == null ? obj2 : obj;
    }

    public static String tryString(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        return s;
    }
}
