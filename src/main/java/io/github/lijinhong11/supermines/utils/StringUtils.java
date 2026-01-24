package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.message.LanguageManager;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {
    private static final PlainTextComponentSerializer COMPONENT_PLAIN = PlainTextComponentSerializer.plainText();

    public static String parseLocation(@Nullable CommandSender cs, @NotNull Location loc) {
        MessageReplacement x = MessageReplacement.replace("%x%", String.valueOf(loc.getBlockX()));
        MessageReplacement y = MessageReplacement.replace("%y%", String.valueOf(loc.getBlockY()));
        MessageReplacement z = MessageReplacement.replace("%z%", String.valueOf(loc.getBlockZ()));

        return SuperMines.getInstance().getLanguageManager().getMsg(cs, "common.location-format", x, y, z);
    }

    public static String getBooleanStatus(@Nullable CommandSender cs, boolean b) {
        LanguageManager lm = SuperMines.getInstance().getLanguageManager();
        return b ? lm.getMsg(cs, "common.enabled") : lm.getMsg(cs, "common.disabled");
    }

    public static String parsePlaceholders(@NotNull String text) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(null, text);
        }

        Component result = Component.text(text);

        if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            String plain = COMPONENT_PLAIN.serialize(result);
            return COMPONENT_PLAIN.serialize(MiniMessage.miniMessage().deserialize(plain, MiniPlaceholders.getGlobalPlaceholders()));
        }

        return text;
    }
}
