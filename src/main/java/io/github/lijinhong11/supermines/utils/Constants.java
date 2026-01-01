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
    public static final int PERCENTAGE_MAX = 100;
    public static final int PERCENTAGE_MIN = 0;

    private Constants() {
    }

    public static class Texts {
        public static final String DATABASE_FILE = "data/data.db";
        public static final String ID_PATTERN = "^[a-zA-Z0-9_-]+$";
        public static final Component RESET = Component.empty().decoration(TextDecoration.ITALIC, false);
    }

    public static class Keys {
        public static final NamespacedKey WAND_KEY = new NamespacedKey(SuperMines.getInstance(), "supermines_wand");

        private Keys() {
        }
    }

    public static class Items {
        public static final Material DEFAULT_MINE_ICON = Material.STONE;
        public static final ItemStack BACKGROUND = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.space())
                .build();
        public static final Function<Player, ItemStack> WAND = player -> ItemBuilder.from(Material.BLAZE_ROD)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "wand.name"))
                .lore(SuperMines.getInstance().getLanguageManager().getMsgComponentList(player, "wand.lore"))
                .pdc(c -> c.set(Keys.WAND_KEY, PersistentDataType.BOOLEAN, true))
                .build();
        // functional buttons
        public static final Function<Player, ItemStack> PREVIOUS_PAGE = player -> ItemBuilder.from(Material.PAPER)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.previous"))
                .build();
        public static final Function<Player, ItemStack> NEXT_PAGE = player -> ItemBuilder.from(Material.PAPER)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.next"))
                .build();
        public static final Function<Player, ItemStack> BACK = player -> ItemBuilder.from(Material.ARROW)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.back"))
                .build();
        public static final Function<Player, ItemStack> CLOSE = player -> ItemBuilder.from(Material.BARRIER)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.close"))
                .build();
        /* general gui & common */
        public static final Function<Player, ItemStack> MINES = player -> ItemBuilder.from(Material.STONE)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.mines.title"))
                .build();
        public static final Function<Player, ItemStack> TREASURES = player -> ItemBuilder.from(Material.CHEST)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.treasures.title"))
                .build();
        public static final Function<Player, ItemStack> RANKS = player -> ItemBuilder.from(Material.NAME_TAG)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.ranks.title"))
                .build();
        public static final BiFunction<Player, Identified, ItemStack> SET_DISPLAY_NAME = (p, i) -> ItemBuilder.from(
                        Material.NAME_TAG)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.set_display_name.name"))
                .lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(
                                p,
                                "gui.set_display_name.lore",
                                MessageReplacement.replace("%name%", i.getRawDisplayName())))
                .build();
        public static final Function<Player, ItemStack> ADD = p -> ItemBuilder.from(Material.PAPER)
                .name(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.add"))
                .build();
        // mine management
        public static final BiFunction<Player, Material, ItemStack> SET_DISPLAY_ICON =
                (p, m) -> ItemBuilder.from(Material.STONE)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.mine-management.set_display_icon.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.mine-management.set_display_icon.lore",
                                        MessageReplacement.replace("%material%", m.toString())))
                        .build();
        public static final BiFunction<Player, Integer, ItemStack> SET_REGEN_SECONDS =
                (p, i) -> ItemBuilder.from(Material.CLOCK)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.mine-management.set_regen_seconds.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.mine-management.set_regen_seconds.lore",
                                        MessageReplacement.replace("%seconds%", String.valueOf(i))))
                        .build();
        public static final BiFunction<Player, Boolean, ItemStack> ONLY_FILL_AIR =
                (p, b) -> ItemBuilder.from(Material.BARRIER)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.mine-management.only_fill_air.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.mine-management.only_fill_air.lore",
                                        MessageReplacement.replace("%status%", StringUtils.getBooleanStatus(p, b))))
                        .build();
        public static final BiFunction<Player, Integer, ItemStack> SET_REQUIRED_RANK_LEVEL =
                (p, i) -> ItemBuilder.from(Material.NAME_TAG)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.mine-management.set_required_lvl.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.mine-management.set_required_lvl.lore",
                                        MessageReplacement.replace("%level%", String.valueOf(i))))
                        .build();
        public static final Function<Player, ItemStack> BLOCK_SPAWN_ENTRIES = p -> ItemBuilder.from(Material.COAL_ORE)
                .name(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.mine-management.block_spawn_entries.name"))
                .build();
        // treasure management
        public static final BiFunction<Player, Double, ItemStack> SET_CHANCE =
                (p, i) -> ItemBuilder.from(Material.BRUSH)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.treasure-management.set_chance.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.treasure-management.set_chance.lore",
                                        MessageReplacement.replace("%chance%", String.valueOf(i))))
                        .build();
        public static final Function<Player, ItemStack> MATCHED_MATERIALS = p -> ItemBuilder.from(Material.STONE)
                .name(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.matched_materials.title"))
                .build();
        // rank management
        public static final BiFunction<Player, Integer, ItemStack> SET_RANK_LEVEL =
                (p, i) -> ItemBuilder.from(Material.NAME_TAG)
                        .name(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponent(p, "gui.ranks-management.setlevel.name"))
                        .lore(SuperMines.getInstance()
                                .getLanguageManager()
                                .getMsgComponentList(
                                        p,
                                        "gui.rank-management.setlevel.lore",
                                        MessageReplacement.replace("level", String.valueOf(i))))
                        .build();

        private Items() {
        }
    }

    public static class Permission {
        public static final String RELOAD = "supermines.reload";

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
        public static final String SET_ONLY_FILL_AIR = "supermines.set_only_fill_air";
        public static final String SET_REQUIRED_LEVEL = "supermines.set_required_level";
        public static final String SET_RESET_TIME = "supermines.set_reset_time";
        public static final String SET_TELEPORT = "supermines.set_teleport";
        public static final String TELEPORT = "supermines.teleport";

        public static final String BYPASS_RANK = "supermines.bypass.rank";
    }
}
