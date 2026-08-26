package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Constants {
    public static final String DATABASE_FILE = "data/data.db";
    public static final String ID_PATTERN = "^[a-zA-Z0-9_-]+$";
    public static final double WEIGHT_MIN = 0.001d;

    private Constants() {}

    public static class Keys {
        public static final NamespacedKey WAND_KEY = new NamespacedKey(SuperMines.getInstance(), "supermines_wand");

        private Keys() {}
    }

    public static class Items {
        public static final Material DEFAULT_MINE_ICON = Material.STONE;

        public static final Function<Player, ItemStack> WAND = player -> {
            ItemStack item = new ItemStack(Material.BLAZE_ROD);
            item.editMeta(meta -> {
                meta.displayName(SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "wand.name"));
                meta.lore(SuperMines.getInstance().getLanguageManager().getMsgComponentList(player, "wand.lore"));
                meta.getPersistentDataContainer().set(Keys.WAND_KEY, PersistentDataType.BOOLEAN, true);
            });
            return item;
        };

        public static final Function<Player, ItemStack> PREVIOUS_PAGE = player -> {
            ItemStack item = new ItemStack(Material.PAPER);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.previous")));
            return item;
        };

        public static final Function<Player, ItemStack> NEXT_PAGE = player -> {
            ItemStack item = new ItemStack(Material.PAPER);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.next")));
            return item;
        };

        public static final Function<Player, ItemStack> BACK = player -> {
            ItemStack item = new ItemStack(Material.ARROW);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.back")));
            return item;
        };

        public static final Function<Player, ItemStack> CLOSE = player -> {
            ItemStack item = new ItemStack(Material.BARRIER);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.close")));
            return item;
        };

        public static final Function<Player, ItemStack> MINES = player -> {
            ItemStack item = new ItemStack(Material.STONE);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.mines.title")));
            return item;
        };

        public static final Function<Player, ItemStack> TREASURES = player -> {
            ItemStack item = new ItemStack(Material.CHEST);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.treasures.title")));
            return item;
        };

        public static final Function<Player, ItemStack> RANKS = player -> {
            ItemStack item = new ItemStack(Material.NAME_TAG);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.ranks.title")));
            return item;
        };

        public static final Function<Player, ItemStack> REGEN_POINTS = player -> {
            ItemStack item = new ItemStack(Material.DIAMOND_ORE);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(player, "gui.regenpoints.title")));
            return item;
        };

        public static final BiFunction<Player, Identified, ItemStack> SET_DISPLAY_NAME =
                (p, i) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.NAME_TAG,
                                "gui.set_display_name",
                                p,
                                MessageReplacement.replace("%name%", i.getRawDisplayName()));

        public static final Function<Player, ItemStack> ADD = p -> {
            ItemStack item = new ItemStack(Material.PAPER);
            item.editMeta(meta -> meta.displayName(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.add")));
            return item;
        };

        public static final BiFunction<Player, Material, ItemStack> SET_DISPLAY_ICON =
                (p, m) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.PAINTING,
                                "gui.mine-management.set_display_icon",
                                p,
                                MessageReplacement.replace("%material%", m.toString()));

        public static final BiFunction<Player, Integer, ItemStack> SET_REGEN_SECONDS =
                (p, i) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.CLOCK,
                                "gui.mine-management.set_regen_seconds",
                                p,
                                MessageReplacement.replace("%seconds%", String.valueOf(i)));

        public static final BiFunction<Player, Boolean, ItemStack> ONLY_FILL_AIR = (p, b) -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(
                        Material.BARRIER,
                        "gui.mine-management.only_fill_air",
                        p,
                        MessageReplacement.replace("%status%", StringUtils.toBooleanStatus(p, b)));

        public static final BiFunction<Player, Boolean, ItemStack> AUTO_PICKUP = (p, b) -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(
                        Material.HOPPER,
                        "gui.mine-management.auto_pickup",
                        p,
                        MessageReplacement.replace("%status%", StringUtils.toBooleanStatus(p, b)));

        public static final BiFunction<Player, Integer, ItemStack> SET_REQUIRED_RANK_LEVEL =
                (p, i) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.BEACON,
                                "gui.mine-management.set_required_lvl",
                                p,
                                MessageReplacement.replace("%level%", String.valueOf(i)));

        public static final Function<Player, ItemStack> BLOCK_SPAWN_ENTRIES = p -> {
            ItemStack item = new ItemStack(Material.COAL_ORE);
            item.editMeta(meta -> meta.displayName(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponent(p, "gui.mine-management.block_spawn_entries.name")));
            return item;
        };

        public static final BiFunction<Player, Integer, ItemStack> MINE_TREASURES =
                (p, amount) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.CHEST,
                                "gui.mine-management.treasures",
                                p,
                                MessageReplacement.replace("%amount%", String.valueOf(amount)));

        public static final BiFunction<Player, Double, ItemStack> SET_WEIGHT = (p, i) -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(
                        Material.BRUSH,
                        "gui.treasure-management.set_weight",
                        p,
                        MessageReplacement.replace("%weight%", String.valueOf(i)));

        public static final Function<Player, ItemStack> MATCHED_MATERIALS = p -> {
            ItemStack item = new ItemStack(Material.STONE);
            item.editMeta(meta -> meta.displayName(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponent(p, "gui.treasure-management.matched_materials.title")));
            return item;
        };

        public static final BiFunction<Player, Treasure, ItemStack> TREASURE_COMMANDS =
                (p, t) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.PAPER,
                                "gui.treasure-management.set_command.item",
                                p,
                                MessageReplacement.replace(
                                        "%commands%",
                                        String.valueOf(
                                                t.getConsoleCommands() == null
                                                        ? 0
                                                        : t.getConsoleCommands().size())));

        public static final Function<Player, ItemStack> COPY_TREASURE = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.CHEST_MINECART, "gui.treasures.copy", p);

        public static final Function<Player, ItemStack> COPY_RANK = p ->
                SuperMines.getInstance().getLanguageManager().getMessagedItem(Material.NAME_TAG, "gui.ranks.copy", p);

        public static final BiFunction<Player, Integer, ItemStack> SET_RANK_LEVEL = (p, i) -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(
                        Material.NAME_TAG,
                        "gui.ranks-management.setlevel",
                        p,
                        MessageReplacement.replace("level", String.valueOf(i)));

        public static final BiFunction<Player, Integer, ItemStack> SET_RESPAWN_SECONDS =
                (p, i) -> SuperMines.getInstance()
                        .getLanguageManager()
                        .getMessagedItem(
                                Material.CLOCK,
                                "gui.regen-point-management.set_respawn_seconds",
                                p,
                                MessageReplacement.replace("%seconds%", String.valueOf(i)));

        public static final Function<Player, ItemStack> SET_REGEN_BLOCK = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.COAL_ORE, "gui.regen-point-management.set_block", p);

        public static final Function<Player, ItemStack> REGEN_REWARDS = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.CHEST, "gui.regen-point-management.rewards", p);

        public static final Function<Player, ItemStack> RESPAWN_NOW = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.BONE_MEAL, "gui.regen-point-management.respawn_now", p);

        public static final Function<Player, ItemStack> REMOVE_REGEN_POINT = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.BARRIER, "gui.regen-point-management.remove", p);

        public static final Function<Player, ItemStack> TP_TO_POINT = p -> SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(Material.ENDER_PEARL, "gui.regen-point-management.tp", p);

        private Items() {}
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
        public static final String SET_AUTO_PICKUP = "supermines.set_auto_pickup";
        public static final String SET_REQUIRED_LEVEL = "supermines.set_required_level";
        public static final String SET_RESET_TIME = "supermines.set_reset_time";
        public static final String SET_TELEPORT = "supermines.set_teleport";
        public static final String TELEPORT = "supermines.teleport";

        public static final String REGEN_POINTS = "supermines.regen_points";

        public static final String BYPASS_RANK = "supermines.bypass.rank";

        private Permission() {}
    }
}
