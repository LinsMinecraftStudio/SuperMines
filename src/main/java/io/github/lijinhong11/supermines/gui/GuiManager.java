package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.chat.ChatInput;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiManager {
    public static void openGeneral(Player p) {
        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .rows(3)
                .create();

        gui.setItem(2, 3, ItemBuilder.from(Constants.Items.MINES.apply(p)).asGuiItem(e -> {
            e.setCancelled(true);
            openMineList(p);
        }));
        gui.setItem(2, 5, ItemBuilder.from(Constants.Items.TREASURES.apply(p)).asGuiItem(e -> {
            e.setCancelled(true);
            openTreasureList(p);
        }));
        gui.setItem(2, 7, ItemBuilder.from(Constants.Items.RANKS.apply(p)).asGuiItem(e -> {
            e.setCancelled(true);
            openRankList(p);
        }));

        gui.open(p);
    }

    public static void openMineList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mines.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            Material mat = mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon();
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(mine.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getMineInfo(p, mine))
                    .asGuiItem(e -> {
                        e.setCancelled(true);
                        openMineManagementGui(p, mine);
                    });
            gui.addItem(guiItem);
        }

        gui.open(p);
    }

    public static void openMineManagementGui(Player p, Mine mine) {
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.mine-management.title", mineName))
                .rows(6)
                .create();

        placeCommon(p, gui, mine, mine.getDisplayIcon(), () -> openMineManagementGui(p, mine));

        gui.setItem(
                3,
                4,
                ItemBuilder.from(Constants.Items.SET_DISPLAY_ICON.apply(p, mine.getDisplayIcon()))
                        .asGuiItem(e -> {
                            Material m = openMaterialChooser(p, mt -> true, () -> openMineManagementGui(p, mine));
                            mine.setDisplayIcon(m);

                            openMineManagementGui(p, mine);
                        }));
    }

    public static void openTreasureList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasures.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            Material mat = Material.CHEST;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(treasure.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getTreasureInfo(p, treasure))
                    .asGuiItem(e -> openTreasureManagementGui(p, treasure));

            gui.addItem(guiItem);
        }
    }

    public static void openTreasureManagementGui(Player p, Treasure treasure) {
        MessageReplacement treasureName = MessageReplacement.replace("%treasure%", treasure.getRawDisplayName());

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.title", treasureName))
                .rows(6)
                .create();

        placeCommon(p, gui, treasure, Material.CHEST, () -> openTreasureManagementGui(p, treasure));
    }

    public static void openRankList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.ranks.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));
        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            Material mat = Material.NAME_TAG;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(rank.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getRankInfo(p, rank))
                    .asGuiItem(e -> openRankManagementGui(p, rank));
            gui.addItem(guiItem);
        }
    }

    public static void openRankManagementGui(Player p, Rank rank) {
        MessageReplacement rankName = MessageReplacement.replace("%rank%", rank.getRawDisplayName());

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.rank-management.title", rankName))
                .rows(6)
                .create();

        placeCommon(p, gui, rank, Material.NAME_TAG, () -> openRankManagementGui(p, rank));

        gui.setItem(
                3,
                4,
                ItemBuilder.from(Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel()))
                        .asGuiItem(e -> {
                            e.setCancelled(true);
                            if (!p.hasPermission(Constants.Permission.RANKS)) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "no-permission");
                                return;
                            }

                            gui.close(p);

                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(p, "gui.rank-management.setlevel.prompt");
                            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                                if (result.equalsIgnoreCase("##CANCEL")) {
                                    return;
                                }

                                try {
                                    int lvl = Integer.parseUnsignedInt(result);
                                    rank.setLevel(lvl);
                                } catch (NumberFormatException ex) {
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(p, "gui.rank-management.setlevel.invalid-level");
                                }

                                openRankManagementGui(p, rank);
                            });
                        }));
    }

    /* common methods */
    private static void fillPageButtons(Player p, PaginatedGui gui, Runnable reopen) {
        gui.getFiller()
                .fillBetweenPoints(
                        6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));

        gui.setItem(6, 1, ItemBuilder.from(Constants.Items.CLOSE.apply(p)).asGuiItem(e -> {
            e.setCancelled(true);
            gui.close(p);
        }));

        gui.setItem(
                6, 3, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)).asGuiItem(e -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)).asGuiItem(e -> gui.next()));
        gui.setItem(6, 9, ItemBuilder.from(Constants.Items.BACK.apply(p)).asGuiItem(e -> reopen.run()));
    }

    private static <T extends Identified> void placeCommon(
            Player p, Gui gui, T object, Material icon, Runnable reopen) {
        gui.setItem(
                2,
                5,
                ItemBuilder.from(icon)
                        .name(object.getDisplayName())
                        .lore(ComponentUtils.deserialize("&7&lID: " + object.getId()))
                        .asGuiItem(e -> e.setCancelled(true)));

        gui.setItem(
                3,
                2,
                ItemBuilder.from(Constants.Items.SET_DISPLAY_NAME.apply(p, object))
                        .asGuiItem(e -> {
                            e.setCancelled(true);
                            if (!p.hasPermission(Constants.Permission.SET_DISPLAY_NAME)) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "no-permission");
                                return;
                            }

                            gui.close(p);

                            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.set-display-name.prompt");
                            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                                if (result.equalsIgnoreCase("##CANCEL")) {
                                    return;
                                }

                                object.setDisplayName(ComponentUtils.deserialize(result));
                            });

                            reopen.run();
                        }));

        GuiFiller filler = gui.getFiller();
        filler.fillBorder(ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));
    }

    private static Material openMaterialChooser(Player p, Predicate<Material> predicate, Runnable reopen) {
        PaginatedGui gui = Gui.paginated()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.material-chooser.title"))
                .rows(6)
                .create();

        fillPageButtons(p, gui, reopen);

        AtomicReference<Material> selected = new AtomicReference<>();

        for (Material material : Material.values()) {
            if (material.isAir()) {
                continue;
            }

            if (!predicate.test(material)) {
                continue;
            }

            GuiItem guiItem = ItemBuilder.from(material).asGuiItem(e -> {
                e.setCancelled(true);
                selected.set(material);
                reopen.run();
            });

            gui.addItem(guiItem);
        }

        gui.open(p);

        return selected.get();
    }
}
