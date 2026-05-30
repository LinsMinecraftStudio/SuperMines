package io.github.lijinhong11.supermines.gui;

import io.github.lijinhong11.mittellib.gui.MittelGUI;
import io.github.lijinhong11.mittellib.gui.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.item.ButtonItem;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ListGUI {
    public static <T> void openList(
            Player p,
            Component title,
            Collection<T> objects,
            Function<T, ItemStack> itemFunction,
            Consumer<T> remove,
            Runnable add,
            Runnable back) {
        openList(p, title, objects, itemFunction, add, back, (e, t, gui) -> {
            remove.accept(t);
            add.run();
        });
    }

    public static <T> void openList(
            Player p,
            Component title,
            Collection<T> objects,
            Function<T, ItemStack> itemFunction,
            Runnable add,
            Runnable back,
            TriConsumer<InventoryClickEvent, T, PaginatedChestGUI> run) {
        MittelGUI.PagedChestBuilder builder = MittelGUI.pagedChestBuilder()
                .title(title)
                .size(54)
                .structure(
                        "xxxxxxxxx",
                        "xcccccccx",
                        "xcccccccx",
                        "xcccccccx",
                        "xcccccccx",
                        "xaxpxnxkx")
                .content('c')
                .previousPage('p', ButtonItem.unclickable(Constants.Items.PREVIOUS_PAGE.apply(p)))
                .nextPage('n', ButtonItem.unclickable(Constants.Items.NEXT_PAGE.apply(p)))
                .bind('x', ButtonItem.unclickable(Constants.Items.BACKGROUND))
                .bind('a', ButtonItem.clickable(Constants.Items.ADD.apply(p), (gui, e) -> {
                    add.run();
                    return false;
                }))
                .bind('k', ButtonItem.clickable(Constants.Items.BACK.apply(p), (gui, e) -> {
                    back.run();
                    return false;
                }));

        for (T t : objects) {
            ItemStack itemStack = itemFunction.apply(t);
            builder.addItem(ButtonItem.clickable(itemStack, (gui, e) -> {
                run.accept(e, t, (PaginatedChestGUI) gui);
                return false;
            }));
        }

        PaginatedChestGUI gui = builder.build();
        gui.open(p);
    }
}
