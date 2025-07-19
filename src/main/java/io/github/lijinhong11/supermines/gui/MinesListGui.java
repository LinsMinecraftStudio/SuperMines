package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.entity.Player;

public class MinesListGui {
    public static void open(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mines.title"))
                .create();

        gui.setItem(6, 3, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)).asGuiItem(event -> gui.previous()));
        gui.setItem(6, 7, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)).asGuiItem(event -> gui.next()));

        gui.getFiller().fillBetweenPoints(6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem());

        for (Mine mine : SuperMines.getInstance().getMineManager().getAll()) {
            if (mine.ge)
        }
    }
}
