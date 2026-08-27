package io.github.lijinhong11.supermines.integrates.placeholders;

import io.github.lijinhong11.mittellib.hook.placeholder.UniversalPlaceholderExpansion;
import io.github.lijinhong11.mittellib.utils.NumberUtils;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
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
            OfflinePlayer p = args.length > 0 ? Bukkit.getOfflinePlayer(args[0]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return ComponentUtils.serialize(data.getRank().getBestValuedRank().getDisplayName());
        });

        registerPlaceholder("biggestranklevel", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            OfflinePlayer p = args.length > 0 ? Bukkit.getOfflinePlayer(args[0]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return String.valueOf(data.getRank().getBiggestRankLevel());
        });

        registerPlaceholder("minedblocks", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            OfflinePlayer p = args.length > 0 ? Bukkit.getOfflinePlayer(args[0]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());

            return String.valueOf(data.getMinedBlocks());
        });

        registerPlaceholder("treasures_got", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            if (args.length < 1) return null;
            OfflinePlayer p = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
            return String.valueOf(data.getTreasuresGot(args[0]));
        });

        registerPlaceholder("regenpoints_mined", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            if (args.length < 1) return null;
            OfflinePlayer p = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
            return String.valueOf(data.getRegenPointMining(args[0]));
        });

        registerPlaceholder("regenpoints_total_mined", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            if (args.length < 1) return null;
            OfflinePlayer p = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : viewer;
            if (p == null) return null;

            PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
            return String.valueOf(data.getRegenPointTotalMining(args[0]));
        });

        registerPlaceholder("regenpoints_resettime", PlaceholderType.GLOBAL, (viewer, target, args) -> {
            if (args.length < 1) return null;

            RegenPoint rp = SuperMines.getInstance().getRegenPointManager().getRegenPoint(args[0]);
            if (rp == null) {
                return "REGEN_POINT_NOT_FOUND";
            }

            long remaining = SuperMines.getInstance().getRegenPointManager().getUntilResetTime(rp);
            if (remaining < 0) {
                return SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsg(viewer == null ? null : viewer.getPlayer(), "regen-point.generated");
            }
            return NumberUtils.formatSeconds(null, (int) (Math.max(0L, remaining) / 1000L));
        });

        registerPlaceholder("hasrank", PlaceholderType.AUDIENCE, (viewer, target, args) -> {
            if (args.length < 1) return null;

            String rank = args[0];
            OfflinePlayer p = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : viewer;
            if (p == null) return null;

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
