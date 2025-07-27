package io.github.lijinhong11.supermines.integrates;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIExtension extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "supermines";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mmmjjkx";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("rank")) {
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                return ChatColor.translateAlternateColorCodes(
                        '&', ComponentUtils.serializeLegacy(data.getRank().getDisplayName()));
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("rank")) {
                String playerName = args[1];
                OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                return ChatColor.translateAlternateColorCodes(
                        '&', ComponentUtils.serializeLegacy(data.getRank().getDisplayName()));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("mine")) {
                String mineId = args[1];
                Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                if (mine != null) {
                    switch (args[2]) {
                        case "blocksbroken" -> {
                            return String.valueOf(mine.getBlocksBroken());
                        }
                        case "resettime" -> {
                            return NumberUtils.formatSeconds(null, (int)
                                    (SuperMines.getInstance().getTaskMaker().getMineUntilResetTime(mine) * 1000));
                        }
                        case "blockpercent" -> {
                            int broken = mine.getBlocksBroken();
                            int total = mine.getArea().volume();
                            return String.format("%.2f", (double) (broken / total * 100));
                        }
                        case "totalblocks" -> {
                            return String.valueOf(mine.getArea().volume());
                        }
                    }
                } else {
                    return "MINE_NOT_FOUND";
                }
            }
        }

        return "";
    }
}
