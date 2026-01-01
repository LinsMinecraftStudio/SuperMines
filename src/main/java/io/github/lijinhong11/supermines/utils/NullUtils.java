package io.github.lijinhong11.supermines.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NullUtils {
    public static @Nullable <T> T tryAnyNotNull(T obj, T obj2) {
        return obj == null ? obj2 : obj;
    }

    public static @NotNull String tryString(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return "";
        }

        return s;
    }
}
