package io.github.lijinhong11.supermines.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentUtils {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private ComponentUtils() {}

    public static Component deserialize(String input) {
        Component component1 = LEGACY.deserialize(input);
        return Constants.StringsAndComponents.RESET.append(component1);
    }

    public static String serialize(Component component) {
        return LEGACY.serialize(component);
    }

    public static String serializeLegacy(Component component) {
        return LEGACY.serialize(component);
    }
}
