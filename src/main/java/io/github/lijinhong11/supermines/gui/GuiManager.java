package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiManager {
    public static void openMineList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mines.title"))
                .create();

        gui.setItem(6, 3, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)).asGuiItem(event -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)).asGuiItem(event -> gui.next()));

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
        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mine-management.title"))
                .rows(3)
                .create();
    }
}
