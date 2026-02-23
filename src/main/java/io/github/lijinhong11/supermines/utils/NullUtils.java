package io.github.lijinhong11.supermines.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NullUtils {
    public static @NotNull String tryString(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return "null";
        }

        return s;
    }
}
