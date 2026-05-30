package io.github.lijinhong11.supermines.gui;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.gui.MittelGUI;
import io.github.lijinhong11.mittellib.gui.impl.ChestGUI;
import io.github.lijinhong11.mittellib.gui.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.item.ButtonItem;
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class GuiManager {
    private static final String CANCEL_COMMAND = "##CANCEL";

    public static void openGeneral(Player p) {
        ChestGUI gui = MittelGUI.chestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .size(27)
                .structure(
                        "xxxxxxxxx",
                        "xxMxTxRxx",
                        "xxxxxxxxx")
                .bind('x', ButtonItem.unclickable(Constants.Items.BACKGROUND))
                .bind('M', ButtonItem.clickable(Constants.Items.MINES.apply(p), (g, e) -> {
                    openMineList(p);
                    return false;
                }))
                .bind('T', ButtonItem.clickable(Constants.Items.TREASURES.apply(p), (g, e) -> {
                    openTreasureList(p);
                    return false;
                }))
                .bind('R', ButtonItem.clickable(Constants.Items.RANKS.apply(p), (g, e) -> {
                    openRankList(p);
                    return false;
                }))
                .build();

        gui.open(p);
    }

    public static void openMineList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.mines.title", () -> openGeneral(p));

        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            Material mat = mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon();
            ItemStack item = new ItemStack(mat);
            item.editMeta(meta -> {
                meta.displayName(mine.getDisplayName());
                meta.lore(getMineInfo(p, mine));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                openMineManagementGui(p, mine);
                return false;
            }));
        }

        gui.open(p);
    }

    public static void openMineManagementGui(Player p, Mine mine) {
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        ChestGUI gui = buildManagementGUI(p, "gui.mine-management.title", mineName);
        Runnable reopen = () -> openMineManagementGui(p, mine);
        Runnable back = () -> openMineList(p);

        placeCommon(p, gui, mine, mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon(), reopen, back);

        // Display Icon
        gui.putItem(slot(3, 4), ButtonItem.clickable(
                Constants.Items.SET_DISPLAY_ICON.apply(p, mine.getDisplayIcon()),
                (g, e) -> {
                    openMaterialChooser(p, chosen -> {
                        mine.setDisplayIcon(chosen.toItem().getType());
                        reopen.run();
                    });
                    return false;
                }));

        // Regenerate Seconds
        gui.putItem(slot(3, 6), ButtonItem.clickable(
                Constants.Items.SET_REGEN_SECONDS.apply(p, mine.getRegenerateSeconds()),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.SET_RESET_TIME)) return false;
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_reset_time.prompt");
                    handleIntegerInput(p, result -> {
                        mine.setRegenerateSeconds(result);
                        SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        reopen.run();
                    });
                    return false;
                }));

        // Only Fill Air
        gui.putItem(slot(3, 8), ButtonItem.clickable(
                Constants.Items.ONLY_FILL_AIR.apply(p, mine.isOnlyFillAirWhenRegenerate()),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.SET_ONLY_FILL_AIR)) return false;
                    mine.setOnlyFillAirWhenRegenerate(!mine.isOnlyFillAirWhenRegenerate());
                    reopen.run();
                    return false;
                }));

        // Required Rank Level
        gui.putItem(slot(4, 3), ButtonItem.clickable(
                Constants.Items.SET_REQUIRED_RANK_LEVEL.apply(p, mine.getRequiredRankLevel()),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.SET_REQUIRED_LEVEL)) return false;
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_required_lvl.prompt");
                    handleIntegerInput(p, result -> {
                        mine.setRequiredRankLevel(result);
                        SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        reopen.run();
                    });
                    return false;
                }));

        // Block Spawn Entries
        gui.putItem(slot(4, 5), ButtonItem.clickable(
                Constants.Items.BLOCK_SPAWN_ENTRIES.apply(p),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;
                    openBlockSpawnEntries(p, mine);
                    return false;
                }));

        gui.open(p);
    }

    private static void openBlockSpawnEntries(Player p, Mine mine) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.mine-management.block_spawn_entries.name", () -> openMineManagementGui(p, mine));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openBlockChooser(p, b -> true, chosen -> {
                SuperMines.getInstance()
                        .getLanguageManager()
                        .sendMessage(
                                p,
                                "gui.mine-management.block_spawn_entries.add_prompt",
                                MessageReplacement.replace("%material%", chosen.toString()));
                addBlockSpawnEntry(p, mine, chosen);
            });
            return false;
        }));

        for (Map.Entry<PackedBlock, Double> entry : mine.getBlockSpawnEntries().object2DoubleEntrySet()) {
            MessageReplacement r = MessageReplacement.replace("%percent%", String.valueOf(entry.getValue()));
            List<Component> lore = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.each_lore", r);
            PackedBlock block = entry.getKey();
            ItemStack itemStack = block.toItem();
            itemStack.editMeta(meta -> meta.lore(lore));

            gui.addPageItem(ButtonItem.clickable(itemStack, (g, e) -> {
                if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

                if (e.getClick().isLeftClick()) {
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(
                                    p,
                                    "gui.mine-management.block_spawn_entries.set_weight_prompt",
                                    MessageReplacement.replace("%material%", block.getId()));
                    addBlockSpawnEntry(p, mine, block);
                } else if (e.getClick().isRightClick()) {
                    mine.removeBlockSpawnEntry(block);
                    openBlockSpawnEntries(p, mine);
                }
                return false;
            }));
        }

        gui.open(p);
    }

    private static void addBlockSpawnEntry(Player p, Mine mine, PackedBlock material) {
        handleDoubleInput(
                p,
                Constants.WEIGHT_MIN,
                result -> {
                    mine.addBlockSpawnEntry(material, result);
                    openBlockSpawnEntries(p, mine);
                },
                "gui.input.invalid-number");
    }

    public static void openTreasureList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.treasures.title", () -> openGeneral(p));

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            ItemStack item = new ItemStack(Material.CHEST);
            item.editMeta(meta -> {
                meta.displayName(treasure.getDisplayName());
                meta.lore(getTreasureInfo(p, treasure));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                openTreasureManagementGui(p, treasure);
                return false;
            }));
        }

        gui.open(p);
    }

    public static void openTreasureManagementGui(Player p, Treasure treasure) {
        MessageReplacement treasureName = MessageReplacement.replace("%treasure%", treasure.getRawDisplayName());
        ChestGUI gui = buildManagementGUI(p, "gui.treasure-management.title", treasureName);
        Runnable reopen = () -> openTreasureManagementGui(p, treasure);
        Runnable back = () -> openTreasureList(p);

        placeCommon(p, gui, treasure, Material.CHEST, reopen, back);

        // Weight
        gui.putItem(slot(3, 4), ButtonItem.clickable(
                Constants.Items.SET_WEIGHT.apply(p, treasure.getWeight()),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.TREASURES)) return false;
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.treasure-management.set_weight.prompt");
                    handleDoubleInput(
                            p,
                            Constants.WEIGHT_MIN,
                            result -> {
                                treasure.setWeight(result);
                                reopen.run();
                            },
                            "gui.input.invalid-number");
                    return false;
                }));

        // Matched Materials
        gui.putItem(slot(3, 8), ButtonItem.clickable(
                Constants.Items.MATCHED_MATERIALS.apply(p),
                (g, e) -> {
                    openMatchedMaterials(p, treasure);
                    return false;
                }));

        putTreasureItemStack(gui, p, treasure);
    }

    private static void putTreasureItemStack(ChestGUI gui, Player p, Treasure t) {
        ItemStack base = t.getItemStack();
        ItemStack display;
        if (base == null) {
            display = new ItemStack(Material.BARRIER);
            display.editMeta(meta -> {
                meta.displayName(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.itemstack.none"));
                meta.lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(p, "gui.treasure-management.itemstack.none_lore"));
            });
        } else {
            display = base.clone();
        }

        if (display.getItemMeta() != null && base != null) {
            ItemMeta meta = display.getItemMeta();
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
            display.setItemMeta(meta);
        }

        gui.putItem(slot(3, 6), ButtonItem.clickable(display, (g, e) -> {
            if (!checkPermission(p, Constants.Permission.TREASURES)) return false;

            if (e.getClick().isRightClick()) {
                if (t.getItemStack() != null && p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(t.getItemStack());
                }
            } else if (e.getClick().isLeftClick()) {
                ItemStack item = p.getItemOnCursor();
                if (!item.getType().isAir()) {
                    t.setItemStack(item);
                    p.setItemOnCursor(null);
                    putTreasureItemStack(gui, p, t);
                }
            }
            return false;
        }));
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
                    ItemStack item = t.toItem();
                    item.editMeta(meta -> meta.lore(lore));
                    return item;
                },
                treasure::removeMatchedBlock,
                () -> openBlockChooser(p, b -> true, chosen -> {
                    if (!treasure.getMatchedBlocks().contains(chosen)) {
                        treasure.addMatchedBlock(chosen);
                    }
                    openMatchedMaterials(p, treasure);
                }),
                () -> openTreasureManagementGui(p, treasure));
    }

    public static void openRankList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.ranks.title", () -> openGeneral(p));

        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            ItemStack item = new ItemStack(Material.NAME_TAG);
            item.editMeta(meta -> {
                meta.displayName(rank.getDisplayName());
                meta.lore(getRankInfo(p, rank));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                openRankManagementGui(p, rank);
                return false;
            }));
        }

        gui.open(p);
    }

    public static void openRankManagementGui(Player p, Rank rank) {
        MessageReplacement rankName = MessageReplacement.replace("%rank%", rank.getRawDisplayName());
        ChestGUI gui = buildManagementGUI(p, "gui.rank-management.title", rankName);
        Runnable reopen = () -> openRankManagementGui(p, rank);
        Runnable back = () -> openRankList(p);

        placeCommon(p, gui, rank, Material.NAME_TAG, reopen, back);

        gui.putItem(slot(3, 4), ButtonItem.clickable(
                Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel()),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.RANKS)) return false;
                    p.closeInventory();
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.rank-management.setlevel.prompt");
                    handleIntegerInput(p, result -> {
                        rank.setLevel(result);
                        reopen.run();
                    });
                    return false;
                }));

        gui.open(p);
    }

    /* Helper methods */
    private static PaginatedChestGUI buildPagedGUI(Player p, String titleKey, Runnable back) {
        MittelGUI.PagedChestBuilder builder = MittelGUI.pagedChestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey))
                .size(54)
                .structure(
                        "xxxxxxxxx",
                        "xcccccccx",
                        "xcccccccx",
                        "xcccccccx",
                        "xcccccccx",
                        "xxxpxnxbx")
                .content('c')
                .previousPage('p', ButtonItem.unclickable(Constants.Items.PREVIOUS_PAGE.apply(p)))
                .nextPage('n', ButtonItem.unclickable(Constants.Items.NEXT_PAGE.apply(p)))
                .bind('x', ButtonItem.unclickable(Constants.Items.BACKGROUND));

        if (back != null) {
            builder = builder.bind('b', ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
                back.run();
                return false;
            }));
        } else {
            builder.bind('b', ButtonItem.unclickable(Constants.Items.BACKGROUND));
        }

        return builder.build();
    }

    private static ChestGUI buildManagementGUI(Player p, String titleKey, MessageReplacement... replacements) {
        return MittelGUI.chestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey, replacements))
                .size(54)
                .structure(
                        "xxxxxxxxx",
                        "x       x",
                        "x       x",
                        "x       x",
                        "x       x",
                        "xxxxxxxxx")
                .bind('x', ButtonItem.unclickable(Constants.Items.BACKGROUND))
                .build();
    }

    private static int slot(int row, int col) {
        return (row - 1) * 9 + (col - 1);
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

    private static void handleDoubleInput(Player p, double min, Consumer<Double> onSuccess, String errorKey) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                return;
            }

            try {
                double value = Double.parseDouble(result);
                if (value < min) {
                    throw new NumberFormatException();
                }
                onSuccess.accept(value);
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, errorKey);
            }
        });
    }

    private static <T extends Identified> void placeCommon(
            Player p, ChestGUI gui, T object, Material icon, Runnable reopen, Runnable back) {
        // Display icon with object name
        ItemStack iconItem = new ItemStack(icon);
        iconItem.editMeta(meta -> {
            meta.displayName(object.getDisplayName());
            meta.lore(List.of(ComponentUtils.deserialize("&7&lID: " + object.getId())));
        });
        gui.putItem(slot(2, 5), ButtonItem.unclickable(iconItem));

        // Display name setter
        gui.putItem(slot(3, 2), ButtonItem.clickable(
                Constants.Items.SET_DISPLAY_NAME.apply(p, object),
                (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.SET_DISPLAY_NAME)) return false;
                    p.closeInventory();
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.set_display_name.prompt");
                    ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                        if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                            return;
                        }
                        object.setDisplayName(ComponentUtils.deserialize(result));
                        reopen.run();
                    });
                    return false;
                }));

        // Back button
        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));
    }

    private static void openMaterialChooser(Player p, Consumer<PackedBlock> callback) {
        openBlockChooser(p, b -> {
            if (b instanceof MinecraftContentProvider.PackedMinecraftBlock(Material material)) {
                return material.isBlock() && material.isItem();
            } else {
                return true;
            }
        }, "material", callback);
    }

    private static void openBlockChooser(Player p, Predicate<PackedBlock> predicate, Runnable reopen) {
        openBlockChooser(p, predicate, "block", chosen -> reopen.run());
    }

    private static void openBlockChooser(Player p, Predicate<PackedBlock> predicate, Consumer<PackedBlock> callback) {
        openBlockChooser(p, predicate, "block", callback);
    }

    private static void openBlockChooser(
            Player p, Predicate<PackedBlock> predicate, String titleKey, Consumer<PackedBlock> callback) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui." + titleKey + "-chooser.title", null);

        for (PackedBlock block : ContentProviders.getAllUsableBlocks()) {
            ItemStack item = block.toItem();
            if (item == null || !predicate.test(block)) {
                continue;
            }

            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                callback.accept(block);
                return false;
            }));
        }

        gui.open(p);
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
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, pos1, pos2);
    }

    private static List<Component> getTreasureInfo(@NotNull Player p, @NotNull Treasure treasure) {
        MessageReplacement weight = MessageReplacement.replace("%weight%", String.valueOf(treasure.getWeight()));
        MessageReplacement matchedMaterials = MessageReplacement.replace(
                "%matched_materials%",
                String.valueOf(treasure.getMatchedBlocks().size()));
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(p, "gui.treasures.info", weight, matchedMaterials);
    }

    private static List<Component> getRankInfo(@NotNull Player p, @NotNull Rank rank) {
        MessageReplacement level = MessageReplacement.replace("%level%", String.valueOf(rank.getLevel()));
        return SuperMines.getInstance().getLanguageManager().getMsgComponentList(p, "gui.ranks.info", level);
    }
}
