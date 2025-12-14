package io.github.lijinhong11.supermines.utils;

import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

public class ComponentUtils {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer
            .legacyAmpersand()
            .toBuilder()
            .hexColors()
            .build();

    private ComponentUtils() {}

    public static Component deserialize(String input) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            input = PlaceholderAPI.setPlaceholders(null, input);
        }

        Component result = LEGACY.deserialize(input);
        String mini = MiniMessage.miniMessage().serialize(result);
        return MiniMessage.miniMessage().deserialize(mini, MiniPlaceholders.getGlobalPlaceholders());
    }

    public static Component text(String input) {
        return Constants.StringsAndComponents.RESET.append(Component.text(input));
    }

    public static String serialize(Component component) {
        return LEGACY.serialize(component);
    }

    public static String serializeLegacy(Component component) {
        return LEGACY.serialize(component);
    }
}
