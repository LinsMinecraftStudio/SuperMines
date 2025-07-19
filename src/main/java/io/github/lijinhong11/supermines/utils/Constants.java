package io.github.lijinhong11.supermines.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.lijinhong11.supermines.SuperMines;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class Constants {
    private Constants() {}

    public static class Components {
        public static final Component RESET = Component.empty().decoration(TextDecoration.ITALIC, false);
    }

    public static class Items {
        public static final Function<Player, ItemStack> PREVIOUS_PAGE = player -> ItemBuilder.from(Material.PAPER)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.previous"))
                .build();

        public static final Function<Player, ItemStack> NEXT_PAGE = player -> ItemBuilder.from(Material.PAPER)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.next"))
                .build();

        public static final ItemStack DEFAULT_MINE_ICON = new ItemStack(Material.STONE);
        public static final ItemStack BACKGROUND = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.space()).build();
    }

    public static class Permission {
        public static final String BYPASS_NO_PLACE = "supermines.bypass.no-place";
        public static final String BYPASS_RANK = "supermines.bypass.rank";
        public static final String CREATE = "supermines.create";
        public static final String REDEFINE = "supermines.redefine";
        public static final String REMOVE = "supermines.delete";
    }
}
