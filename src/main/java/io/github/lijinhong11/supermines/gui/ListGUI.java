package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListGUI {
    public static <T> void openList(Player p, Component title, Collection<T> objects, Function<T, ItemStack> itemFunction, Consumer<T> remove, Runnable add, Runnable back) {
        openList(p, title, objects, itemFunction, add, back, (e, t, gui) -> {
            remove.accept(t);
            gui.removePageItem(itemFunction.apply(t));
        });
    }

    public static <T> void openList(Player p, Component title, Collection<T> objects, Function<T, ItemStack> itemFunction, Runnable add, Runnable back, TriConsumer<InventoryClickEvent, T, PaginatedGui> run) {
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .title(title)
                .create();

        for (T t : objects) {
            gui.addItem(ItemBuilder.from(itemFunction.apply(t)).asGuiItem(e -> {
                run.accept(e, t, gui);
                e.setCancelled(true);
            }));
        }

        gui.getFiller()
                .fillBetweenPoints(
                        6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));

        GuiManager.putItem(6, 1, gui, ItemBuilder.from(Constants.Items.ADD.apply(p)), e -> add.run());
        GuiManager.putItem(6, 3, gui, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)), e -> gui.previous());
        GuiManager.putItem(6, 7, gui, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)), e -> gui.next());
        GuiManager.putItem(6, 9, gui, ItemBuilder.from(Constants.Items.BACK.apply(p)), e -> back.run());

        gui.open(p);
    }
}
