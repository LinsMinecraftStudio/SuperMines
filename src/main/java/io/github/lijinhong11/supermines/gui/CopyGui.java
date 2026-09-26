package io.github.lijinhong11.supermines.gui;

import io.github.lijinhong11.mittellib.gui.dialog.impl.input.TextInputDialog;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class CopyGui {
    private CopyGui() {}

    static void openTreasureCopySource(Player p) {
        PaginatedChestGUI gui =
                GuiManager.buildPagedGUI(p, "gui.treasures.copy_source_title", () -> GuiManager.openTreasureList(p));
        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            ItemStack item = new ItemStack(Material.CHEST);
            item.editMeta(meta -> meta.displayName(treasure.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                TextInputDialog.create(
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .getMsgComponent(p, "gui.treasures.copy_title"),
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .getMsgComponent(p, "gui.treasures.copy_label"),
                                newId -> {
                                    if (!newId.matches(Constants.ID_PATTERN)
                                            || SuperMines.getInstance()
                                                            .getTreasureManager()
                                                            .getTreasure(newId)
                                                    != null) {
                                        SuperMines.getInstance()
                                                .getLanguageManager()
                                                .sendMessage(p, "command.invalid-id");
                                        openTreasureCopySource(p);
                                        return;
                                    }
                                    Treasure copy = treasure.copy(newId);
                                    SuperMines.getInstance()
                                            .getTreasureManager()
                                            .addTreasure(copy);
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(
                                                    p,
                                                    "command.treasures.create.copy.success",
                                                    MessageReplacement.replace("%treasure%", copy.getRawDisplayName()));
                                    GuiManager.openTreasureList(p);
                                })
                        .show(p);
                return false;
            }));
        }
        gui.open(p);
    }

    static void openRankCopySource(Player p) {
        PaginatedChestGUI gui =
                GuiManager.buildPagedGUI(p, "gui.ranks.copy_source_title", () -> GuiManager.openRankList(p));
        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            ItemStack item = new ItemStack(Material.NAME_TAG);
            item.editMeta(meta -> meta.displayName(rank.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                TextInputDialog.create(
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .getMsgComponent(p, "gui.ranks.copy_title"),
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .getMsgComponent(p, "gui.ranks.copy_label"),
                                newId -> {
                                    if (!newId.matches(Constants.ID_PATTERN)
                                            || SuperMines.getInstance()
                                                            .getRankManager()
                                                            .getRank(newId)
                                                    != null) {
                                        SuperMines.getInstance()
                                                .getLanguageManager()
                                                .sendMessage(p, "command.invalid-id");
                                        openRankCopySource(p);
                                        return;
                                    }
                                    Rank copy = rank.copy(newId);
                                    SuperMines.getInstance().getRankManager().addRank(copy);
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(
                                                    p,
                                                    "command.ranks.create.copy",
                                                    MessageReplacement.replace("%rank%", copy.getRawDisplayName()));
                                    GuiManager.openRankList(p);
                                })
                        .show(p);
                return false;
            }));
        }
        gui.open(p);
    }
}
