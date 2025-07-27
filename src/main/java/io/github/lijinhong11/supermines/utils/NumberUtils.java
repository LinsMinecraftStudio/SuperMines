package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.supermines.SuperMines;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.command.CommandSender;

public class NumberUtils {
    private NumberUtils() {}

    public static boolean matchChance(double chancePercent) {
        return (chancePercent / 100) >= 1 || ThreadLocalRandom.current().nextDouble(1) < (chancePercent / 100);
    }

    public static String formatSeconds(int seconds) {
        return formatSeconds(null, seconds);
    }

    public static String formatSeconds(CommandSender cs, int totalSeconds) {
        String secondText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "second");
        String secondsText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "seconds");
        String minuteText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "minute");
        String minutesText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "minutes");
        String hourText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "hour");
        String hoursText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "hours");
        String dayText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "day");
        String daysText = SuperMines.getInstance().getLanguageManager().getMsg(cs, "days");

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" ").append(days == 1 ? dayText : daysText).append(" ");
        }
        if (hours > 0) {
            sb.append(hours)
                    .append(" ")
                    .append(hours == 1 ? hourText : hoursText)
                    .append(" ");
        }
        if (minutes > 0) {
            sb.append(minutes)
                    .append(" ")
                    .append(minutes == 1 ? minuteText : minutesText)
                    .append(" ");
        }
        if (seconds > 0 || sb.isEmpty()) {
            sb.append(seconds).append(" ").append(seconds == 1 ? secondText : secondsText);
        }

        return sb.toString().trim();
    }
}
