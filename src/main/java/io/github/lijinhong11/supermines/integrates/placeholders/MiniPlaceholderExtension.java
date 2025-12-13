package io.github.lijinhong11.supermines.integrates.placeholders;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MiniPlaceholderExtension {
    public static void register() {
        Expansion expansion = Expansion.builder("supermines")
                .audiencePlaceholder("bestRank", (a, args, ctx) -> {
                    if (args.hasNext()) {
                        String playerName = args.pop().value();
                        OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                        return Tag.selfClosingInserting(data.getRank().getBestValuedRank().getDisplayName());
                    } else {
                        OfflinePlayer p = (Player) a;
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
                        return Tag.selfClosingInserting(data.getRank().getBestValuedRank().getDisplayName());
                    }
                })
                .audiencePlaceholder("biggestRankLevel", (a, args, ctx) -> {
                    if (args.hasNext()) {
                        String playerName = args.pop().value();
                        OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                        return Tag.selfClosingInserting(Component.text(data.getRank().getBiggestRankLevel()));
                    } else {
                        OfflinePlayer p = (Player) a;
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
                        return Tag.selfClosingInserting(Component.text(data.getRank().getBiggestRankLevel()));
                    }
                })
                .audiencePlaceholder("minedBlocks", (a, args, ctx) -> {
                    if (args.hasNext()) {
                        String playerName = args.pop().value();
                        OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                        return Tag.selfClosingInserting(Component.text(data.getMinedBlocks()));
                    } else {
                        OfflinePlayer p = (Player) a;
                        PlayerData data =
                                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
                        return Tag.selfClosingInserting(Component.text(data.getMinedBlocks()));
                    }
                })
                .globalPlaceholder("mine_blocksbroken", (args, ctx) -> {
                    String mineId = args.popOr("missing_mine_id").value();
                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                    if (mine == null) return Tag.selfClosingInserting(Component.text("MINE_NOT_FOUND"));
                    return Tag.selfClosingInserting(Component.text(mine.getBlocksBroken()));
                })
                .globalPlaceholder("mine_resettime", (args, ctx) -> {
                    String mineId = args.popOr("missing_mine_id").value();
                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                    if (mine == null) return Tag.selfClosingInserting(Component.text("MINE_NOT_FOUND"));
                    int millis = (int) (SuperMines.getInstance().getTaskMaker().getMineUntilResetTime(mine) * 1000);
                    return Tag.selfClosingInserting(Component.text(NumberUtils.formatSeconds(null, millis)));
                })
                .globalPlaceholder("mine_blockpercent", (args, ctx) -> {
                    String mineId = args.popOr("missing_mine_id").value();
                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                    if (mine == null) return Tag.selfClosingInserting(Component.text("MINE_NOT_FOUND"));
                    int broken = mine.getBlocksBroken();
                    int total = mine.getArea().volume();
                    double percent = total == 0 ? 100d : ((double) (total - broken) / total) * 100;
                    return Tag.selfClosingInserting(Component.text(String.format("%.2f", percent)));
                })
                .globalPlaceholder("mine_minedpercent", (args, ctx) -> {
                    String mineId = args.popOr("missing_mine_id").value();
                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                    if (mine == null) return Tag.selfClosingInserting(Component.text("MINE_NOT_FOUND"));
                    int broken = mine.getBlocksBroken();
                    int total = mine.getArea().volume();
                    double percent = total == 0 ? 0d : ((double) broken / total) * 100;
                    return Tag.selfClosingInserting(Component.text(String.format("%.2f", percent)));
                })
                .globalPlaceholder("totalblocks", (args, ctx) -> {
                    String mineId = args.popOr("missing_mine_id").value();
                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                    if (mine == null) return Tag.selfClosingInserting(Component.text("MINE_NOT_FOUND"));
                    return Tag.selfClosingInserting(
                            Component.text(mine.getArea().volume()));
                })
                .build();

        expansion.register();
    }
}
