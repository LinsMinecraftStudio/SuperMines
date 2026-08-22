package io.github.lijinhong11.supermines.gui;

import io.github.lijinhong11.mittellib.gui.inventory.MittelGUI;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class ListGUI {
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
                .structure("XXXXXXXXX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XAXPXNXKX")
                .content('C')
                .previousPage('P', ButtonItem.unclickable(Constants.Items.PREVIOUS_PAGE.apply(p)))
                .nextPage('N', ButtonItem.unclickable(Constants.Items.NEXT_PAGE.apply(p)))
                .bind('X', ButtonItem.BACKGROUND)
                .bind('A', ButtonItem.clickable(Constants.Items.ADD.apply(p), (gui, e) -> {
                    add.run();
                    return false;
                }))
                .bind('K', ButtonItem.clickable(Constants.Items.BACK.apply(p), (gui, e) -> {
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
