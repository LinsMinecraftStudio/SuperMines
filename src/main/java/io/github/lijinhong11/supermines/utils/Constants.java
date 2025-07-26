package io.github.lijinhong11.supermines.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Constants {
    private Constants() {}

    public static class StringsAndComponents {
        public static final String DATABASE_FILE = "data/data.db";
        public static final String ID_PATTERN = "^[a-zA-Z0-9_-]+$";
        public static final Component RESET = Component.empty().decoration(TextDecoration.ITALIC, false);
    }

    public static class Keys {
        public static final NamespacedKey WAND_KEY = new NamespacedKey(SuperMines.getInstance(), "supermines_wand");
    }

    public static class Items {
        public static final Material DEFAULT_MINE_ICON = Material.STONE;
        public static final ItemStack BACKGROUND = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.space()).build();

        public static final Function<Player, ItemStack> WAND = player -> ItemBuilder.from(Material.BLAZE_ROD)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "wand.name"))
                .lore(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "wand.lore"))
                .pdc(c -> c.set(Keys.WAND_KEY, PersistentDataType.BOOLEAN, true))
                .build();

        //functional buttons
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
        public static final Function<Player, ItemStack> BACK = player -> ItemBuilder.from(Material.ARROW)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.back"))
                .build();
        public static final Function<Player, ItemStack> CLOSE = player -> ItemBuilder.from(Material.BARRIER)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.close"))
                .build();

        /* general gui & common */
        public static final Function<Player, ItemStack> MINES = player -> ItemBuilder.from(Material.STONE)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.mines.title"))
                .build();
        public static final Function<Player, ItemStack> TREASURES = player -> ItemBuilder.from(Material.CHEST)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.treasures.title"))
                .build();
        public static final Function<Player, ItemStack> RANKS = player -> ItemBuilder.from(Material.CHEST)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(player, "gui.ranks.title"))
                .build();
        public static final BiFunction<Player, Identified, ItemStack> SET_DISPLAY_NAME = (p, i) -> ItemBuilder.from(Material.NAME_TAG)
                .name(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.set_display_name"))
                .lore(SuperMines
                        .getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(p, "gui.set_display_name.lore", MessageReplacement.replace("name", i.getRawDisplayName())))
                .build();
    }

    public static class Permission {
        public static final String TREASURES = "supermines.treasures";
        public static final String RANKS = "supermines.ranks";

        public static final String BLOCK_GENERATE = "supermines.block_generates";
        public static final String CREATE = "supermines.create";
        public static final String GUI = "supermines.gui";
        public static final String LIST = "supermines.list";
        public static final String POS_SET = "supermines.setpos";
        public static final String REDEFINE = "supermines.redefine";
        public static final String REMOVE = "supermines.delete";
        public static final String RESET = "supermines.reset";
        public static final String RESET_WARNINGS = "supermines.reset_warnings";
        public static final String SET_DISPLAY_NAME = "supermines.set_display_name";
        public static final String SET_DISPLAY_ICON = "supermines.set_display_icon";
        public static final String SET_REQUIRED_LEVEL = "supermines.set_required_level";

        public static final String BYPASS_RANK = "supermines.bypass.rank";
    }
}
