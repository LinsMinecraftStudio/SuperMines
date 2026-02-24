package io.github.lijinhong11.supermines.integrates.placeholders;

import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PlaceholderAPIExtension extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "supermines";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mmmjjkx (lijinhong11)";
    }

    @Override
    public @NotNull String getVersion() {
        return SuperMines.getInstance().getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("bestrank")) {
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                return ComponentUtils.serializeLegacy(
                        data.getRank().getBestValuedRank().getDisplayName());
            } else if (args[0].equalsIgnoreCase("biggestranklevel")) {
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                return String.valueOf(data.getRank().getBiggestRankLevel());
            } else if (args[0].equalsIgnoreCase("minedblocks")) {
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                return String.valueOf(data.getMinedBlocks());
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("bestrank")) {
                String playerName = args[1];
                OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                return ComponentUtils.serializeLegacy(
                        data.getRank().getBestValuedRank().getDisplayName());
            } else if (args[0].equalsIgnoreCase("biggestranklevel")) {
                String playerName = args[1];
                OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                return String.valueOf(data.getRank().getBiggestRankLevel());
            } else if (args[0].equalsIgnoreCase("minedblocks")) {
                String playerName = args[1];
                OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                return String.valueOf(data.getMinedBlocks());
            } else if (args[0].equalsIgnoreCase("hasrank")) {
                String rank = args[1];
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                return String.valueOf(data.getRank().matchRank(rank));
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
                                    (SuperMines.getInstance().getTaskMaker().getMineUntilResetTime(mine) / 1000));
                        }
                        case "blockpercent" -> {
                            int broken = mine.getBlocksBroken();
                            int total = mine.getArea().volume();
                            double percent = total == 0 ? 100d : ((double) (total - broken) / total) * 100;
                            return String.format("%.2f", percent);
                        }
                        case "minedpercent" -> {
                            int broken = mine.getBlocksBroken();
                            int total = mine.getArea().volume();
                            double percent = total == 0 ? 0d : ((double) broken / total) * 100;
                            return String.format("%.2f", percent);
                        }
                        case "totalblocks" -> {
                            return String.valueOf(mine.getArea().volume());
                        }
                        default -> {
                            return "INVALID_ARGUMENT";
                        }
                    }
                } else {
                    return "MINE_NOT_FOUND";
                }
            } else if (args[0].equalsIgnoreCase("hasrank")) {
                String rank = args[1];
                OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[2]);
                PlayerData data =
                        SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                return String.valueOf(data.getRank().matchRank(rank));
            }
        }

        return null;
    }
}
