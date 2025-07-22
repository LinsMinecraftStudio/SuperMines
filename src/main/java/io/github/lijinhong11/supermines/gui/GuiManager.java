package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiManager {
    public static void openGeneral(Player p) {
        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .rows(3)
                .create();

        gui.setItem(2, 3, ItemBuilder.from(Constants.Items.MINES.apply(p)).asGuiItem(e -> {
            gui.close(p);
            openMineList(p);
        }));

        gui.setItem(2, 5, ItemBuilder.from(Constants.Items.TREASURES.apply(p)).asGuiItem(e -> {
            gui.close(p);
            openTreasureList(p);
        }));

        gui.setItem(2, 7, ItemBuilder.from(Constants.Items.RANKS.apply(p)).asGuiItem(e -> {
            gui.close(p);
            openRankList(p);
        }));
    }

    public static void openMineList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mines.title"))
                .create();

        gui.setItem(6, 3, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)).asGuiItem(event -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)).asGuiItem(event -> gui.next()));
        gui.setItem(6, 9, ItemBuilder.from(Constants.Items.BACK.apply(p)).asGuiItem(e -> openGeneral(p)));

        gui.getFiller().fillBetweenPoints(6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem());

        for (Mine mine : SuperMines.getInstance().getMineManager().getAll()) {
            Material mat = mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon();
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(mine.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getMineInfo(p, mine))
                    .asGuiItem(e -> {
                        gui.close(e.getWhoClicked());
                        openMineManagementGui(p, mine);
                    });
            gui.addItem(guiItem);
        }

        gui.open(p);
    }

    public static void openMineManagementGui(Player p, Mine mine) {
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mine-management.title", mineName))
                .rows(6)
                .create();

        GuiFiller filler = gui.getFiller();
        filler.fillBorder(ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));
    }

    public static void openTreasureList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasures.title"))
                .create();

        gui.setItem(6, 3, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)).asGuiItem(event -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)).asGuiItem(event -> gui.next()));
        gui.setItem(6, 9, ItemBuilder.from(Constants.Items.BACK.apply(p)).asGuiItem(e -> openGeneral(p)));

        gui.getFiller().fillBetweenPoints(6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem());

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAll()) {
            Material mat = Material.CHEST;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(treasure.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getTreasureInfo(p, treasure))
                    .asGuiItem(e -> {
                        gui.close(e.getWhoClicked());
                        openTreasureManagementGui(p, treasure);
                    });

            gui.addItem(guiItem);
        }
    }

    public static void openTreasureManagementGui(Player p, Treasure treasure) {
        MessageReplacement treasureName = MessageReplacement.replace("%treasure%", treasure.getRawDisplayName());

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasure-management.title", treasureName))
                .rows(6)
                .create();
    }
}
