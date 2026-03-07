package io.github.lijinhong11.supermines.integrates.placeholders;

import io.github.lijinhong11.mittellib.hook.placeholder.UniversalPlaceholderExpansion;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.NumberUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SuperMinesPlaceholders extends UniversalPlaceholderExpansion {
    @Override
    public @NotNull String identifier() {
        return "supermines";
    }

    @Override
    public @NotNull String author() {
        return "mmmjjkx (lijinhong11)";
    }

    @Override
    public @NotNull String version() {
        return SuperMines.getInstance().getDescription().getVersion();
    }

    public SuperMinesPlaceholders() {
        registerPlaceholder("bestrank", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            PlayerData data =
                    SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(viewer.getUniqueId());

            return ComponentUtils.serialize(data.getRank().getBestValuedRank().getDisplayName());
        });

        registerPlaceholder("bestrank", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 1) return null;

            OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return ComponentUtils.serialize(data.getRank().getBestValuedRank().getDisplayName());
        });

        registerPlaceholder("biggestranklevel", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            PlayerData data =
                    SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(viewer.getUniqueId());

            return String.valueOf(data.getRank().getBiggestRankLevel());
        });

        registerPlaceholder("biggestranklevel", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 1) return null;

            OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return String.valueOf(data.getRank().getBiggestRankLevel());
        });

        registerPlaceholder("minedblocks", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            PlayerData data =
                    SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(viewer.getUniqueId());

            return String.valueOf(data.getMinedBlocks());
        });

        registerPlaceholder("minedblocks", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 1) return null;

            OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return String.valueOf(data.getMinedBlocks());
        });

        registerPlaceholder("hasrank", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            if (args.length < 1) return null;

            PlayerData data =
                    SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(viewer.getUniqueId());

            return String.valueOf(data.getRank().matchRank(args[0]));
        });

        registerPlaceholder("hasrank", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 2) return null;

            String rank = args[0];
            OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return String.valueOf(data.getRank().matchRank(rank));
        });

        registerPlaceholder("mine", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 2) return null;

            String mineId = args[0];
            String type = args[1];

            Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);

            if (mine == null) return "MINE_NOT_FOUND";

            switch (type) {
                case "blocksbroken" -> {
                    return String.valueOf(mine.getBlocksBroken());
                }
                case "resettime" -> {
                    return NumberUtils.formatSeconds(
                            null, (int) (SuperMines.getInstance().getTaskMaker().getMineUntilResetTime(mine) / 1000));
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
        });
    }
}
