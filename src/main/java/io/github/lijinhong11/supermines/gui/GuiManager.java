package io.github.lijinhong11.supermines.gui;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.hook.content.MinecraftContentProvider;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.chat.ChatInput;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class GuiManager {
    // GUI position constants (keeping original layout)
    // SHOULD I DO THAT?
    private static final int GENERAL_MINES_ROW = 2;
    private static final int GENERAL_MINES_COL = 3;
    private static final int GENERAL_TREASURES_ROW = 2;
    private static final int GENERAL_TREASURES_COL = 5;
    private static final int GENERAL_RANKS_ROW = 2;
    private static final int GENERAL_RANKS_COL = 7;

    private static final int MANAGEMENT_ICON_ROW = 2;
    private static final int MANAGEMENT_ICON_COL = 5;
    private static final int MANAGEMENT_DISPLAY_NAME_ROW = 3;
    private static final int MANAGEMENT_DISPLAY_NAME_COL = 2;
    private static final int MANAGEMENT_BACK_ROW = 1;
    private static final int MANAGEMENT_BACK_COL = 9;

    private static final int MINE_DISPLAY_ICON_ROW = 3;
    private static final int MINE_DISPLAY_ICON_COL = 4;
    private static final int MINE_REGEN_SECONDS_ROW = 3;
    private static final int MINE_REGEN_SECONDS_COL = 6;
    private static final int MINE_ONLY_FILL_AIR_ROW = 3;
    private static final int MINE_ONLY_FILL_AIR_COL = 8;
    private static final int MINE_REQUIRED_RANK_ROW = 4;
    private static final int MINE_REQUIRED_RANK_COL = 3;
    private static final int MINE_BLOCK_SPAWN_ROW = 4;
    private static final int MINE_BLOCK_SPAWN_COL = 5;

    private static final int TREASURE_CHANCE_ROW = 3;
    private static final int TREASURE_CHANCE_COL = 4;
    private static final int TREASURE_ITEMSTACK_ROW = 3;
    private static final int TREASURE_ITEMSTACK_COL = 6;
    private static final int TREASURE_MATCHED_MATERIALS_ROW = 3;
    private static final int TREASURE_MATCHED_MATERIALS_COL = 8;

    private static final int RANK_LEVEL_ROW = 3;
    private static final int RANK_LEVEL_COL = 4;

    private static final int BLOCK_SPAWN_ADD_ROW = 6;
    private static final int BLOCK_SPAWN_ADD_COL = 1;

    private static final int PAGINATION_ROW = 6;
    private static final int PAGINATION_CLOSE_COL = 1;
    private static final int PAGINATION_PREVIOUS_COL = 3;
    private static final int PAGINATION_NEXT_COL = 7;
    private static final int PAGINATION_BACK_COL = 9;

    private static final int PAGINATION_START_ROW = 6;
    private static final int PAGINATION_START_COL = 1;
    private static final int PAGINATION_END_ROW = 6;
    private static final int PAGINATION_END_COL = 9;

    private static final String CANCEL_COMMAND = "##CANCEL";

    public static void openGeneral(Player p) {
        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .rows(3)
                .create();

        putItem(
                GENERAL_MINES_ROW,
                GENERAL_MINES_COL,
                gui,
                ItemBuilder.from(Constants.Items.MINES.apply(p)),
                e -> openMineList(p));
        putItem(
                GENERAL_TREASURES_ROW,
                GENERAL_TREASURES_COL,
                gui,
                ItemBuilder.from(Constants.Items.TREASURES.apply(p)),
                e -> openTreasureList(p));
        putItem(
                GENERAL_RANKS_ROW,
                GENERAL_RANKS_COL,
                gui,
                ItemBuilder.from(Constants.Items.RANKS.apply(p)),
                e -> openRankList(p));

        gui.open(p);
    }

    public static void openMineList(Player p) {
        PaginatedGui gui = createPaginatedGui(p, "gui.mines.title", 45);
        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            Material mat = mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon();
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(mine.getDisplayName())
                    .lore(getMineInfo(p, mine))
                    .asGuiItem(e -> {
                        openMineManagementGui(p, mine);
                        e.setCancelled(true);
                    });
            gui.addItem(guiItem);
        }

        gui.open(p);
    }

    public static void openMineManagementGui(Player p, Mine mine) {
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        Gui gui = createManagementGui(p, "gui.mine-management.title", mineName);
        Runnable reopen = () -> openMineManagementGui(p, mine);
        Runnable back = () -> openMineList(p);

        placeCommon(p, gui, mine, mine.getDisplayIcon(), reopen, back);

        // Display Icon
        putItem(
                MINE_DISPLAY_ICON_ROW,
                MINE_DISPLAY_ICON_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_DISPLAY_ICON.apply(p, mine.getDisplayIcon())),
                e -> {
                    PackedBlock m = openMaterialChooser(p, reopen);
                    if (m != null) {
                        mine.setDisplayIcon(m.toItem().getType());
                        reopen.run();
                    }
                });

        // Regenerate Seconds
        putItem(
                MINE_REGEN_SECONDS_ROW,
                MINE_REGEN_SECONDS_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_REGEN_SECONDS.apply(p, mine.getRegenerateSeconds())),
                e -> {
                    if (!checkPermission(p, Constants.Permission.SET_RESET_TIME)) return;

                    gui.close(p);
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_reset_time.prompt");
                    handleIntegerInput(p, result -> {
                        mine.setRegenerateSeconds(result);
                        SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        reopen.run();
                    });
                });

        // Only Fill Air
        putItem(
                MINE_ONLY_FILL_AIR_ROW,
                MINE_ONLY_FILL_AIR_COL,
                gui,
                ItemBuilder.from(Constants.Items.ONLY_FILL_AIR.apply(p, mine.isOnlyFillAirWhenRegenerate())),
                e -> {
                    if (!checkPermission(p, Constants.Permission.SET_ONLY_FILL_AIR)) return;
                    mine.setOnlyFillAirWhenRegenerate(!mine.isOnlyFillAirWhenRegenerate());
                    reopen.run();
                });

        // Required Rank Level
        putItem(
                MINE_REQUIRED_RANK_ROW,
                MINE_REQUIRED_RANK_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_REQUIRED_RANK_LEVEL.apply(p, mine.getRequiredRankLevel())),
                e -> {
                    if (!checkPermission(p, Constants.Permission.SET_REQUIRED_LEVEL)) return;

                    gui.close(p);
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_required_lvl.prompt");
                    handleIntegerInput(p, result -> {
                        mine.setRequiredRankLevel(result);
                        SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        reopen.run();
                    });
                });

        // Block Spawn Entries
        putItem(
                MINE_BLOCK_SPAWN_ROW,
                MINE_BLOCK_SPAWN_COL,
                gui,
                ItemBuilder.from(Constants.Items.BLOCK_SPAWN_ENTRIES.apply(p)),
                e -> {
                    if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return;
                    openBlockSpawnEntries(p, mine);
                });

        gui.open(p);
    }

    private static void openBlockSpawnEntries(Player p, Mine mine) {
        PaginatedGui gui = createPaginatedGui(p, "gui.mine-management.block_spawn.title", 45);
        Runnable reopen = () -> openBlockSpawnEntries(p, mine);
        Runnable back = () -> openMineManagementGui(p, mine);

        fillPageButtons(p, gui, back);

        putItem(BLOCK_SPAWN_ADD_ROW, BLOCK_SPAWN_ADD_COL, gui, ItemBuilder.from(Constants.Items.ADD.apply(p)), e -> {
            PackedBlock m = openBlockChooser(p, b -> true, reopen);
            if (m != null) {
                SuperMines.getInstance()
                        .getLanguageManager()
                        .sendMessage(
                                p,
                                "gui.mine-management.block_spawn.add_prompt",
                                MessageReplacement.replace("%material%", m.toString()));
                addBlockSpawnEntry(p, mine, m);
            }
        });

        for (Map.Entry<PackedBlock, Double> entry : mine.getBlockSpawnEntries().entrySet()) {
            MessageReplacement r = MessageReplacement.replace("%precent%", String.valueOf(entry.getValue()));
            List<Component> lore = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn.each_lore", r);
            PackedBlock block = entry.getKey();
            GuiItem item = ItemBuilder.from(block.toItem()).lore(lore).asGuiItem(e -> {
                if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return;

                ClickType type = e.getClick();
                if (type.isLeftClick()) {
                    gui.close(p);
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(
                                    p,
                                    "gui.mine-management.block_spawn.set_precent_prompt",
                                    MessageReplacement.replace("%material%", block.getId()));
                    addBlockSpawnEntry(p, mine, block);
                } else if (type.isRightClick()) {
                    mine.removeBlockSpawnEntry(block);
                    reopen.run();
                }
                e.setCancelled(true);
            });
            gui.addItem(item);
        }

        gui.open(p);
    }

    private static void addBlockSpawnEntry(Player p, Mine mine, Material material) {
        addBlockSpawnEntry(p, mine, new MinecraftContentProvider.PackedMinecraftBlock(material));
    }

    private static void addBlockSpawnEntry(Player p, Mine mine, PackedBlock material) {
        handleDoubleInput(
                p,
                Constants.PERCENTAGE_MIN,
                Constants.PERCENTAGE_MAX,
                result -> {
                    mine.addBlockSpawnEntry(material, result);
                    openBlockSpawnEntries(p, mine);
                },
                "gui.input.invalid-precent");
    }

    public static void openTreasureList(Player p) {
        PaginatedGui gui = createPaginatedGui(p, "gui.treasures.title", 45);
        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            Material mat = Material.CHEST;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(treasure.getDisplayName())
                    .lore(getTreasureInfo(p, treasure))
                    .asGuiItem(e -> {
                        openTreasureManagementGui(p, treasure);
                        e.setCancelled(true);
                    });

            gui.addItem(guiItem);
        }

        gui.open(p);
    }

    public static void openTreasureManagementGui(Player p, Treasure treasure) {
        MessageReplacement treasureName = MessageReplacement.replace("%treasure%", treasure.getRawDisplayName());
        Gui gui = createManagementGui(p, "gui.treasure-management.title", treasureName);
        Runnable reopen = () -> openTreasureManagementGui(p, treasure);
        Runnable back = () -> openTreasureList(p);

        placeCommon(p, gui, treasure, Material.CHEST, reopen, back);

        // Chance
        putItem(
                TREASURE_CHANCE_ROW,
                TREASURE_CHANCE_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_CHANCE.apply(p, treasure.getChance())),
                e -> {
                    if (!checkPermission(p, Constants.Permission.TREASURES)) return;

                    gui.close(p);
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.treasure-management.set_chance.prompt");
                    handleDoubleInput(
                            p,
                            Constants.PERCENTAGE_MIN,
                            Constants.PERCENTAGE_MAX,
                            result -> {
                                treasure.setChance(result);
                                reopen.run();
                            },
                            "gui.input.invalid-precent");
                });

        // Matched Materials
        putItem(
                TREASURE_MATCHED_MATERIALS_ROW,
                TREASURE_MATCHED_MATERIALS_COL,
                gui,
                ItemBuilder.from(Constants.Items.MATCHED_MATERIALS.apply(p)),
                e -> openMatchedMaterials(p, treasure));

        putTreasureItemStack(gui, p, treasure);
    }

    private static void putTreasureItemStack(BaseGui gui, Player p, Treasure t) {
        ItemStack clone = t.getItemStack().clone();
        ItemMeta meta = clone.getItemMeta();
        Component newName = SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(
                        p,
                        "gui.treasure-management.itemstack.name",
                        MessageReplacement.replace("%name%", ComponentUtils.serialize(meta.displayName())));

        meta.displayName(newName);
        meta.lore(SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(p, "gui.treasure-management.itemstack.lore"));
        clone.setItemMeta(meta);

        putItem(TREASURE_ITEMSTACK_ROW, TREASURE_ITEMSTACK_COL, gui, ItemBuilder.from(clone), e -> {
            if (!checkPermission(p, Constants.Permission.TREASURES)) return;

            ClickType type = e.getClick();
            if (type.isRightClick()) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(t.getItemStack());
                }
            } else if (type.isLeftClick()) {
                ItemStack item = p.getItemOnCursor();
                if (!item.getType().isAir()) {
                    t.setItemStack(item);
                    putTreasureItemStack(gui, p, t);
                }
            }
        });
    }

    private static void openMatchedMaterials(Player p, Treasure treasure) {
        ListGUI.openList(
                p,
                SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.matched_materials.title"),
                treasure.getMatchedBlocks(),
                t -> {
                    List<Component> lore = SuperMines.getInstance()
                            .getLanguageManager()
                            .getMsgComponentList(p, "gui.treasure-management.matched_materials.each_lore");
                    return ItemBuilder.from(t.toItem()).lore(lore).build();
                },
                treasure::removeMatchedBlock,
                () -> {
                    PackedBlock m = openBlockChooser(p, b -> true, null);

                    if (!treasure.getMatchedBlocks().contains(m)) {
                        treasure.addMatchedBlock(m);
                    }

                    openMatchedMaterials(p, treasure);
                },
                () -> openTreasureManagementGui(p, treasure));
    }

    public static void openRankList(Player p) {
        PaginatedGui gui = createPaginatedGui(p, "gui.ranks.title", 45);
        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            Material mat = Material.NAME_TAG;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(rank.getDisplayName())
                    .lore(getRankInfo(p, rank))
                    .asGuiItem(e -> {
                        openRankManagementGui(p, rank);
                        e.setCancelled(true);
                    });
            gui.addItem(guiItem);
        }

        gui.open(p);
    }

    public static void openRankManagementGui(Player p, Rank rank) {
        MessageReplacement rankName = MessageReplacement.replace("%rank%", rank.getRawDisplayName());
        Gui gui = createManagementGui(p, "gui.rank-management.title", rankName);
        Runnable reopen = () -> openRankManagementGui(p, rank);
        Runnable back = () -> openRankList(p);

        placeCommon(p, gui, rank, Material.NAME_TAG, reopen, back);

        putItem(
                RANK_LEVEL_ROW,
                RANK_LEVEL_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel())),
                e -> {
                    if (!checkPermission(p, Constants.Permission.RANKS)) return;

                    gui.close(p);
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.rank-management.setlevel.prompt");
                    handleIntegerInput(p, result -> {
                        rank.setLevel(result);
                        reopen.run();
                    });
                });

        gui.open(p);
    }

    /* Helper methods */
    private static PaginatedGui createPaginatedGui(Player p, String titleKey, int pageSize) {
        return Gui.paginated()
                .rows(6)
                .pageSize(pageSize)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey))
                .create();
    }

    private static Gui createManagementGui(Player p, String titleKey, MessageReplacement replacement) {
        return Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey, replacement))
                .rows(6)
                .create();
    }

    private static boolean checkPermission(Player p, String permission) {
        if (!p.hasPermission(permission)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
            return false;
        }
        return true;
    }

    private static void handleIntegerInput(Player p, Consumer<Integer> onSuccess) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                return;
            }

            try {
                int value = Integer.parseUnsignedInt(result);
                onSuccess.accept(value);
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-number");
            }
        });
    }

    private static void handleDoubleInput(
            Player p, double min, double max, Consumer<Double> onSuccess, String errorKey) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                return;
            }

            try {
                double value = Double.parseDouble(result);
                if (value >= max || value < min) {
                    throw new NumberFormatException();
                }
                onSuccess.accept(value);
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, errorKey);
            }
        });
    }

    private static void fillPageButtons(Player p, PaginatedGui gui, Runnable reopen) {
        gui.getFiller()
                .fillBetweenPoints(
                        PAGINATION_START_ROW,
                        PAGINATION_START_COL,
                        PAGINATION_END_ROW,
                        PAGINATION_END_COL,
                        ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));

        putItem(
                PAGINATION_ROW,
                PAGINATION_CLOSE_COL,
                gui,
                ItemBuilder.from(Constants.Items.CLOSE.apply(p)),
                e -> gui.close(p));
        putItem(
                PAGINATION_ROW,
                PAGINATION_PREVIOUS_COL,
                gui,
                ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)),
                e -> gui.previous());
        putItem(
                PAGINATION_ROW,
                PAGINATION_NEXT_COL,
                gui,
                ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)),
                e -> gui.next());
        putItem(
                PAGINATION_ROW,
                PAGINATION_BACK_COL,
                gui,
                ItemBuilder.from(Constants.Items.BACK.apply(p)),
                e -> reopen.run());
    }

    static void putItem(
            int row, int col, BaseGui gui, ItemBuilder item, Consumer<InventoryClickEvent> clickEventConsumer) {
        gui.setItem(row, col, item.asGuiItem(e -> {
            clickEventConsumer.accept(e);
            e.setCancelled(true);
        }));
    }

    private static <T extends Identified> void placeCommon(
            Player p, Gui gui, T object, Material icon, Runnable reopen, Runnable back) {
        GuiFiller filler = gui.getFiller();
        filler.fillBorder(ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));

        putItem(
                MANAGEMENT_ICON_ROW,
                MANAGEMENT_ICON_COL,
                gui,
                ItemBuilder.from(icon)
                        .name(object.getDisplayName())
                        .lore(ComponentUtils.deserialize("&7&lID: " + object.getId())),
                e -> {});

        putItem(
                MANAGEMENT_DISPLAY_NAME_ROW,
                MANAGEMENT_DISPLAY_NAME_COL,
                gui,
                ItemBuilder.from(Constants.Items.SET_DISPLAY_NAME.apply(p, object)),
                e -> {
                    if (!checkPermission(p, Constants.Permission.SET_DISPLAY_NAME)) return;

                    gui.close(p);
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.set_display_name.prompt");
                    ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                        if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                            return;
                        }
                        object.setDisplayName(ComponentUtils.deserialize(result));
                        reopen.run();
                    });
                });

        putItem(
                MANAGEMENT_BACK_ROW,
                MANAGEMENT_BACK_COL,
                gui,
                ItemBuilder.from(Constants.Items.BACK.apply(p)),
                e -> back.run());
    }

    private static PackedBlock openMaterialChooser(Player p, Runnable reopen) {
        return openBlockChooser(p, b -> b instanceof MinecraftContentProvider.PackedMinecraftBlock, "material", reopen);
    }

    private static PackedBlock openBlockChooser(Player p, Predicate<PackedBlock> predicate, Runnable reopen) {
        return openBlockChooser(p, predicate, "block", reopen);
    }

    private static PackedBlock openBlockChooser(
            Player p, Predicate<PackedBlock> predicate, String title, Runnable reopen) {
        PaginatedGui gui = createPaginatedGui(p, "gui." + title + "-chooser.title", 45);
        fillPageButtons(p, gui, reopen);

        AtomicReference<PackedBlock> selected = new AtomicReference<>();

        for (PackedBlock block : ContentProviders.getAllUsableBlocks()) {
            ItemStack item = block.toItem();
            if (item == null || !predicate.test(block)) {
                continue;
            }

            GuiItem guiItem = ItemBuilder.from(item).asGuiItem(e -> {
                selected.set(block);
                reopen.run();
                e.setCancelled(true);
            });

            gui.addItem(guiItem);
        }

        gui.open(p);
        return selected.get();
    }

    private static List<Component> getMineInfo(@NotNull Player p, @NotNull Mine mine) {
        Preconditions.checkNotNull(mine, "mine cannot be null");
        MessageReplacement world =
                MessageReplacement.replace("%world%", mine.getWorld().getName());
        MessageReplacement regenerateSeconds =
                MessageReplacement.replace("%regenerate_seconds%", String.valueOf(mine.getRegenerateSeconds()));
        MessageReplacement pos1 =
                MessageReplacement.replace("%pos1%", mine.getArea().pos1().toString());
        MessageReplacement pos2 =
                MessageReplacement.replace("%pos2%", mine.getArea().pos2().toString());
        return SuperMines.getInstance().getLanguageManager().getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, pos1, pos2);
    }

    private static List<Component> getTreasureInfo(@NotNull Player p, @NotNull Treasure treasure) {
        MessageReplacement chance = MessageReplacement.replace("%chance%", String.valueOf(treasure.getChance()));
        MessageReplacement matchedMaterials = MessageReplacement.replace(
                "%matched_materials%",
                String.valueOf(treasure.getMatchedBlocks().size()));
        return SuperMines.getInstance().getLanguageManager().getMsgComponentList(p, "gui.treasures.info", chance, matchedMaterials);
    }

    private static List<Component> getRankInfo(@NotNull Player p, @NotNull Rank rank) {
        MessageReplacement level = MessageReplacement.replace("%level%", String.valueOf(rank.getLevel()));
        return SuperMines.getInstance().getLanguageManager().getMsgComponentList(p, "gui.ranks.info", level);
    }
}
