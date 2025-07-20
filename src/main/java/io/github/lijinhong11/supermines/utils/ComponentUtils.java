package io.github.lijinhong11.supermines.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class ComponentUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private ComponentUtils() {}

    public static Component deserialize(String input) {
        Component component1 = LEGACY.deserialize(input);
        String legacyParsed = MINI_MESSAGE.serialize(component1);
        return MINI_MESSAGE.deserialize(legacyParsed);
    }

    public static String serialize(Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    public static List<? extends Component> parseToComponentList(String... lore) {
        return parseToComponentList(List.of(lore));
    }

    public static List<? extends Component> parseToComponentList(List<String> lore) {
        return lore.stream().map(ComponentUtils::deserialize).toList();
    }
}
