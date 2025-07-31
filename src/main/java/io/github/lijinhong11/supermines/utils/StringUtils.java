package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.message.LanguageManager;
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

        return SuperMines.getInstance().getLanguageManager().getMsg(cs, "common.location-format", x, y, z);
    }

    public static String getBooleanStatus(@Nullable CommandSender cs, boolean b) {
        LanguageManager lm = SuperMines.getInstance().getLanguageManager();
        return b ? lm.getMsg(cs, "common.enabled") : lm.getMsg(cs, "common.disabled");
    }
}
