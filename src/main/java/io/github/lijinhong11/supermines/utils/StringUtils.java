package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {
    public static String parseLocation(@Nullable CommandSender cs, @NotNull Location loc) {
        MessageReplacement x = MessageReplacement.replace("%x%", String.valueOf(loc.getBlockX()));
        MessageReplacement y = MessageReplacement.replace("%y%", String.valueOf(loc.getBlockY()));
        MessageReplacement z = MessageReplacement.replace("%z%", String.valueOf(loc.getBlockZ()));

        return SuperMines.getInstance().getLanguageManager().getMsg(cs, "location-format", x, y, z);
    }
}
