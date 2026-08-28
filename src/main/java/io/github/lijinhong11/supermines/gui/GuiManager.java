package io.github.lijinhong11.supermines.gui;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.gui.dialog.impl.input.MultiLineTextInputDialog;
import io.github.lijinhong11.mittellib.gui.dialog.impl.input.TextInputDialog;
import io.github.lijinhong11.mittellib.gui.inventory.MittelGUI;
import io.github.lijinhong11.mittellib.gui.inventory.choosers.MaterialChooser;
import io.github.lijinhong11.mittellib.gui.inventory.impl.ChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.mittellib.gui.inventory.item.MittelGUIItem;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.math.SphereArea;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.chat.ChatInput;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GuiManager {
    static final String CANCEL_COMMAND = "##CANCEL";

    public static void openMain(Player p) {
        ChestGUI gui = MittelGUI.chestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .size(27)
                .structure("XXXXXXXXC", "XPXMXTXRX", "XXXXXXXXX")
                .bind('X', ButtonItem.BACKGROUND)
                .bind('P', ButtonItem.clickable(Constants.Items.REGEN_POINTS.apply(p), (g, e) -> {
                    openRegenPointList(p);
                    return false;
                }))
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
                .bind('C', ButtonItem.clickable(Constants.Items.CLOSE.apply(p), (g, e) -> {
                    p.closeInventory();
                    return false;
                }))
                .build();

        gui.open(p);
    }

    public static void openMineList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.mines.title", () -> openMain(p));

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

        placeCommon(
                p,
                gui,
                mine,
                mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon(),
                reopen,
                back);

        // Display Icon
        gui.putItem(
                slot(3, 4),
                ButtonItem.clickable(Constants.Items.SET_DISPLAY_ICON.apply(p, mine.getDisplayIcon()), (g, e) -> {
                    MaterialChooser.openVanillaChooser(p, chosen -> {
                        mine.setDisplayIcon(chosen.toItem().getType());
                        SuperMines.getInstance().getMineManager().saveMine(mine);
                        reopen.run();
                    });
                    return false;
                }));

        // Regenerate Seconds
        gui.putItem(
                slot(3, 6),
                ButtonItem.clickable(
                        Constants.Items.SET_REGEN_SECONDS.apply(p, mine.getRegenerateSeconds()), (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.SET_RESET_TIME)) return false;
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(p, "gui.mine-management.set_regen_seconds.prompt");
                            handleIntegerInput(p, result -> {
                                mine.setRegenerateSeconds(result);
                                SuperMines.getInstance().getMineManager().saveMine(mine);
                                SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                                reopen.run();
                            });
                            return false;
                        }));

        // Only Fill Air
        gui.putItem(
                slot(3, 8),
                ButtonItem.clickable(
                        Constants.Items.ONLY_FILL_AIR.apply(p, mine.isOnlyFillAirWhenRegenerate()), (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.SET_ONLY_FILL_AIR)) return false;
                            mine.setOnlyFillAirWhenRegenerate(!mine.isOnlyFillAirWhenRegenerate());
                            SuperMines.getInstance().getMineManager().saveMine(mine);
                            reopen.run();
                            return false;
                        }));

        // Required Rank Level
        gui.putItem(
                slot(4, 3),
                ButtonItem.clickable(
                        Constants.Items.SET_REQUIRED_RANK_LEVEL.apply(p, mine.getRequiredRankLevel()), (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.SET_REQUIRED_LEVEL)) return false;
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(p, "gui.mine-management.set_required_lvl.prompt");
                            handleIntegerInput(p, result -> {
                                mine.setRequiredRankLevel(result);
                                SuperMines.getInstance().getMineManager().saveMine(mine);
                                SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                                reopen.run();
                            });
                            return false;
                        }));

        // Block Spawn Entries
        gui.putItem(slot(4, 5), ButtonItem.clickable(Constants.Items.BLOCK_SPAWN_ENTRIES.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;
            openBlockSpawnEntries(p, mine);
            return false;
        }));

        // Auto Pickup
        gui.putItem(
                slot(4, 7), ButtonItem.clickable(Constants.Items.AUTO_PICKUP.apply(p, mine.isAutoPickup()), (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.SET_AUTO_PICKUP)) return false;
                    mine.setAutoPickup(!mine.isAutoPickup());
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                    reopen.run();
                    return false;
                }));

        // Treasures
        gui.putItem(
                slot(5, 2),
                ButtonItem.clickable(
                        Constants.Items.MINE_TREASURES.apply(
                                p, mine.getTreasures().size()),
                        (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.TREASURES)) return false;
                            openMineTreasures(p, mine);
                            return false;
                        }));

        gui.open(p);
    }

    private static void openMineTreasures(Player p, Mine mine) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.treasures.title", () -> openMineManagementGui(p, mine));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openMineTreasureChooser(p, mine);
            return false;
        }));

        for (Treasure treasure : mine.getTreasures()) {
            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> {
                meta.displayName(treasure.getDisplayName());
                meta.lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(p, "gui.mine-management.treasures.each_lore"));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (!e.getClick().isRightClick()) return false;
                mine.removeTreasure(treasure);
                SuperMines.getInstance().getMineManager().saveMine(mine);
                openMineTreasures(p, mine);
                return false;
            }));
        }

        gui.open(p);
    }

    private static void openMineTreasureChooser(Player p, Mine mine) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.treasures.chooser_title", () -> openMineTreasures(p, mine));
        Set<String> selectedIds =
                mine.getTreasures().stream().map(Treasure::getId).collect(Collectors.toSet());

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            if (selectedIds.contains(treasure.getId())) continue;

            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> meta.displayName(treasure.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                mine.addTreasure(treasure);
                SuperMines.getInstance().getMineManager().saveMine(mine);
                openMineTreasures(p, mine);
                return false;
            }));
        }

        gui.open(p);
    }

    static void openBlockSpawnEntries(Player p, Mine mine) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.block_spawn_entries.name", () -> openMineManagementGui(p, mine));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            MaterialChooser.openUsableBlockChooser(p, chosen -> {
                SuperMines.getInstance()
                        .getLanguageManager()
                        .sendMessage(
                                p,
                                "gui.mine-management.block_spawn_entries.add_prompt",
                                MessageReplacement.replace("%material%", chosen.toString()));
                p.closeInventory();
                addBlockSpawnEntry(p, mine, chosen);
            });
            return false;
        }));

        for (Map.Entry<BlockSpawnEntry, Double> entry :
                mine.getBlockSpawnEntries().object2DoubleEntrySet()) {
            MessageReplacement r = MessageReplacement.replace("%weight%", String.valueOf(entry.getValue()));
            List<Component> lore = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.each_lore", r);
            PackedBlock block = entry.getKey();
            ItemStack itemStack = block.toItem();
            itemStack.editMeta(meta -> meta.lore(lore));

            gui.addPageItem(ButtonItem.clickable(itemStack, (g, e) -> {
                if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

                if (e.getClick().isShiftClick()) {
                    GenerationConditionGui.openEntryConditions(p, mine, entry.getKey());
                } else if (e.getClick().isLeftClick()) {
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
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                    openBlockSpawnEntries(p, mine);
                }
                return false;
            }));
        }

        gui.open(p);
    }

    private static void addBlockSpawnEntry(Player p, Mine mine, PackedBlock material) {
        handleWeightInput(
                p,
                result -> {
                    mine.addBlockSpawnEntry(material, result);
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                    openBlockSpawnEntries(p, mine);
                },
                "gui.input.invalid-number");
    }

    public static void openTreasureList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(
                p,
                "gui.treasures.title",
                () -> openMain(p),
                ButtonItem.clickable(Constants.Items.COPY_TREASURE.apply(p), (g, e) -> {
                    openTreasureCopySource(p);
                    return false;
                }));

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

    private static void openTreasureCopySource(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.treasures.copy_source_title", () -> openTreasureList(p));
        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            ItemStack item = new ItemStack(Material.CHEST);
            item.editMeta(meta -> meta.displayName(treasure.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                TextInputDialog dialog = TextInputDialog.create(
                        SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasures.copy_title"),
                        SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasures.copy_label"),
                        newId -> {
                            if (!newId.matches(Constants.ID_PATTERN)
                                    || SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(newId)
                                            != null) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.invalid-id");
                                openTreasureCopySource(p);
                                return;
                            }
                            Treasure copy = treasure.copy(newId);
                            SuperMines.getInstance().getTreasureManager().addTreasure(copy);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            p,
                                            "command.treasures.copy.success",
                                            MessageReplacement.replace("%treasure%", copy.getRawDisplayName()));
                            openTreasureList(p);
                        });
                dialog.show(p);
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
        gui.putItem(
                slot(3, 4), ButtonItem.clickable(Constants.Items.SET_WEIGHT.apply(p, treasure.getWeight()), (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.TREASURES)) return false;
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.treasure-management.set_weight.prompt");
                    handleWeightInput(
                            p,
                            result -> {
                                treasure.setWeight(result);
                                SuperMines.getInstance().getTreasureManager().saveTreasure(treasure);
                                reopen.run();
                            },
                            "gui.input.invalid-number");
                    return false;
                }));

        // Matched Materials
        gui.putItem(slot(3, 8), ButtonItem.clickable(Constants.Items.MATCHED_MATERIALS.apply(p), (g, e) -> {
            openMatchedMaterials(p, treasure);
            return false;
        }));

        putTreasureItemStack(gui, p, treasure);

        gui.putItem(slot(4, 3), ButtonItem.clickable(Constants.Items.TREASURE_COMMANDS.apply(p, treasure), (g, e) -> {
            MultiLineTextInputDialog dialog = MultiLineTextInputDialog.create(
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .getMsgComponent(p, "gui.treasure-management.set_command.title"),
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .getMsgComponent(p, "gui.treasure-management.set_command.label"),
                    ls -> {
                        treasure.setConsoleCommands(ls);
                        SuperMines.getInstance().getTreasureManager().saveTreasure(treasure);
                        openTreasureManagementGui(p, treasure);
                    },
                    2500,
                    25,
                    0,
                    treasure.getConsoleCommands() == null ? new ArrayList<>() : treasure.getConsoleCommands());

            dialog.show(p);
            return false;
        }));

        gui.open(p);
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
            Component itemName = meta.displayName() == null
                    ? Component.translatable(base.getType().translationKey())
                    : meta.displayName();

            Component newName = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponent(p, "gui.treasure-management.itemstack.name");

            newName = newName.replaceText(b -> b.matchLiteral("%name%").replacement(itemName));

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
                    SuperMines.getInstance().getTreasureManager().saveTreasure(t);
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
                block -> {
                    treasure.removeMatchedBlock(block);
                    SuperMines.getInstance().getTreasureManager().saveTreasure(treasure);
                },
                () -> MaterialChooser.openUsableBlockChooser(p, chosen -> {
                    if (!treasure.getMatchedBlocks().contains(chosen)) {
                        treasure.addMatchedBlock(chosen);
                        SuperMines.getInstance().getTreasureManager().saveTreasure(treasure);
                    }
                    openMatchedMaterials(p, treasure);
                }),
                () -> openTreasureManagementGui(p, treasure));
    }

    public static void openRankList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(
                p,
                "gui.ranks.title",
                () -> openMain(p),
                ButtonItem.clickable(Constants.Items.COPY_RANK.apply(p), (g, e) -> {
                    openRankCopySource(p);
                    return false;
                }));

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

    private static void openRankCopySource(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.ranks.copy_source_title", () -> openRankList(p));
        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            ItemStack item = new ItemStack(Material.NAME_TAG);
            item.editMeta(meta -> meta.displayName(rank.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                TextInputDialog dialog = TextInputDialog.create(
                        SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.ranks.copy_title"),
                        SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.ranks.copy_label"),
                        newId -> {
                            if (!newId.matches(Constants.ID_PATTERN)
                                    || SuperMines.getInstance().getRankManager().getRank(newId) != null) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.invalid-id");
                                openRankCopySource(p);
                                return;
                            }
                            Rank copy = rank.copy(newId);
                            SuperMines.getInstance().getRankManager().addRank(copy);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            p,
                                            "command.ranks.create.copy",
                                            MessageReplacement.replace("%rank%", copy.getRawDisplayName()));
                            openRankList(p);
                        });
                dialog.show(p);
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

        gui.putItem(
                slot(3, 4), ButtonItem.clickable(Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel()), (g, e) -> {
                    if (!checkPermission(p, Constants.Permission.RANKS)) return false;
                    p.closeInventory();
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.rank-management.setlevel.prompt");
                    handleIntegerInput(p, result -> {
                        rank.setLevel(result);
                        SuperMines.getInstance().getRankManager().saveRank(rank);
                        reopen.run();
                    });
                    return false;
                }));

        gui.open(p);
    }

    public static void openRegenPointList(Player p) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.regenpoints.title", () -> openMain(p));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) {
                return false;
            }
            openAddRegenPoint(p);
            return false;
        }));

        for (RegenPoint point : SuperMines.getInstance().getRegenPointManager().getAllRegenPoints()) {
            ItemStack item = point.getBlock().toItem();
            item.editMeta(meta -> {
                meta.displayName(point.getDisplayName());
                meta.lore(getRegenPointInfo(p, point));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                openRegenPointManagementGui(p, point);
                return false;
            }));
        }

        gui.open(p);
    }

    public static void openRegenPointManagementGui(Player p, RegenPoint point) {
        MessageReplacement pointName = MessageReplacement.replace("%point%", point.getRawDisplayName());
        ChestGUI gui = buildManagementGUI(p, "gui.regen-point-management.title", pointName);
        Runnable reopen = () -> openRegenPointManagementGui(p, point);
        Runnable back = () -> openRegenPointList(p);

        placeCommon(p, gui, point, point.getBlock().toItem().getType(), reopen, back);

        // Respawn Seconds
        gui.putItem(
                slot(3, 4),
                ButtonItem.clickable(
                        Constants.Items.SET_RESPAWN_SECONDS.apply(p, point.getRespawnSeconds()), (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(p, "gui.regen-point-management.set_respawn_seconds.prompt");
                            handleIntegerInput(
                                    p,
                                    result -> {
                                        SuperMines.getInstance()
                                                .getRegenPointManager()
                                                .setRespawnSeconds(point, result);
                                        reopen.run();
                                    },
                                    reopen);
                            return false;
                        }));

        // Block Pool
        gui.putItem(slot(3, 6), ButtonItem.clickable(Constants.Items.SET_REGEN_BLOCK.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
            openRegenPointBlocks(p, point);
            return false;
        }));

        gui.putItem(
                slot(3, 8),
                ButtonItem.clickable(
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .getMessagedItem(
                                        Material.CLOCK,
                                        "gui.regen-point-management.periodic_rewards.interval",
                                        p,
                                        MessageReplacement.replace(
                                                "%blocks%", String.valueOf(point.getPeriodicRewardIntervalBlocks())),
                                        MessageReplacement.replace(
                                                "%mined%", String.valueOf(getPeriodicMiningCount(p, point))),
                                        MessageReplacement.replace(
                                                "%remaining%", String.valueOf(getPeriodicMiningRemaining(p, point)))),
                        (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(p, "gui.regen-point-management.periodic_rewards.interval_prompt");
                            handleIntegerInput(
                                    p,
                                    result -> {
                                        point.setPeriodicRewardIntervalBlocks(result);
                                        SuperMines.getInstance()
                                                .getRegenPointManager()
                                                .saveRegenPoint(point);
                                        reopen.run();
                                    },
                                    reopen);
                            return false;
                        }));

        // Independent Rewards
        gui.putItem(slot(4, 3), ButtonItem.clickable(Constants.Items.REGEN_REWARDS.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
            openRegenPointRewards(p, point);
            return false;
        }));

        gui.putItem(
                slot(4, 5),
                ButtonItem.clickable(
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .getMessagedItem(
                                        Material.GOLDEN_APPLE,
                                        "gui.regen-point-management.periodic_rewards",
                                        p,
                                        MessageReplacement.replace(
                                                "%amount%",
                                                String.valueOf(point.getPeriodicRewardChances()
                                                        .size()))),
                        (g, e) -> {
                            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
                            openPeriodicRewards(p, point);
                            return false;
                        }));

        // Teleport
        gui.putItem(slot(4, 7), ButtonItem.clickable(Constants.Items.TP_TO_POINT.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.TELEPORT)) return false;
            p.teleportAsync(point.getLocation().clone().add(0.5, 0, 0.5));
            return false;
        }));

        gui.putItem(slot(5, 2), ButtonItem.clickable(Constants.Items.RESPAWN_NOW.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
            SuperMines.getInstance().getRegenPointManager().respawnNow(point);
            reopen.run();
            return false;
        }));

        gui.putItem(slot(5, 4), ButtonItem.clickable(Constants.Items.REMOVE_REGEN_POINT.apply(p), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.REGEN_POINTS)) return false;
            if (!e.getClick().isShiftClick() || !e.getClick().isRightClick()) return false;
            SuperMines.getInstance().getRegenPointManager().removeRegenPoint(point.getId());
            back.run();
            return false;
        }));

        gui.open(p);
    }

    private static void openRegenPointBlocks(Player p, RegenPoint point) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.regen-point-management.blocks.title", () -> openRegenPointManagementGui(p, point));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            MaterialChooser.openUsableBlockChooser(p, chosen -> promptRegenBlockWeight(p, point, chosen));
            return false;
        }));

        for (Map.Entry<PackedBlock, Double> entry : point.getBlocks().object2DoubleEntrySet()) {
            PackedBlock block = entry.getKey();
            ItemStack item = block.toItem();
            item.editMeta(meta -> meta.lore(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(
                            p,
                            "gui.regen-point-management.blocks.each_lore",
                            MessageReplacement.replace("%weight%", String.valueOf(entry.getValue())),
                            MessageReplacement.replace(
                                    "%probability%", point.getBlocks().getDisplayProbability(block)))));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    if (point.getBlocks().size() > 1) {
                        point.removeBlock(block);
                        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    }
                    openRegenPointBlocks(p, point);
                } else {
                    promptRegenBlockWeight(p, point, block);
                }
                return false;
            }));
        }

        gui.open(p);
    }

    private static void promptRegenBlockWeight(Player p, RegenPoint point, PackedBlock block) {
        p.closeInventory();
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "gui.regen-point-management.blocks.weight_prompt",
                        MessageReplacement.replace("%block%", block.getId()));
        handleWeightInput(
                p,
                weight -> {
                    point.addBlock(block, weight);
                    SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    openRegenPointBlocks(p, point);
                },
                "gui.input.invalid-number",
                () -> openRegenPointBlocks(p, point));
    }

    private static void openRegenPointRewards(Player p, RegenPoint point) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.regen-point-management.rewards.title", () -> openRegenPointManagementGui(p, point));

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openRegenRewardChooser(p, point);
            return false;
        }));

        for (Map.Entry<String, Double> entry : point.getRewardChances().entrySet()) {
            Treasure treasure = SuperMines.getInstance().getTreasureManager().getTreasure(entry.getKey());
            if (treasure == null) continue;
            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> {
                meta.displayName(treasure.getDisplayName());
                meta.lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(
                                p,
                                "gui.regen-point-management.rewards.each_lore",
                                MessageReplacement.replace("%chance%", String.valueOf(entry.getValue()))));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    point.removeReward(treasure.getId());
                    SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    openRegenPointRewards(p, point);
                } else {
                    promptRegenRewardChance(p, point, treasure);
                }
                return false;
            }));
        }

        gui.open(p);
    }

    private static void openRegenRewardChooser(Player p, RegenPoint point) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.regen-point-management.rewards.chooser_title", () -> openRegenPointRewards(p, point));
        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> meta.displayName(treasure.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                promptRegenRewardChance(p, point, treasure);
                return false;
            }));
        }
        gui.open(p);
    }

    private static void promptRegenRewardChance(Player p, RegenPoint point, Treasure treasure) {
        p.closeInventory();
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "gui.regen-point-management.rewards.chance_prompt",
                        MessageReplacement.replace("%treasure%", treasure.getRawDisplayName()));
        handleWeightInput(
                p,
                chance -> {
                    if (chance > 100) {
                        SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-percent");
                        openRegenPointRewards(p, point);
                        return;
                    }
                    point.setRewardChance(treasure.getId(), chance);
                    SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    openRegenPointRewards(p, point);
                },
                "gui.input.invalid-percent",
                () -> openRegenPointRewards(p, point));
    }

    private static void openPeriodicRewards(Player p, RegenPoint point) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.regen-point-management.periodic_rewards.title", () -> openRegenPointManagementGui(p, point));
        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openPeriodicRewardChooser(p, point);
            return false;
        }));

        for (Map.Entry<String, Double> entry : point.getPeriodicRewardChances().entrySet()) {
            Treasure treasure = SuperMines.getInstance().getTreasureManager().getTreasure(entry.getKey());
            if (treasure == null) continue;
            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> {
                meta.displayName(treasure.getDisplayName());
                meta.lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(
                                p,
                                "gui.regen-point-management.periodic_rewards.each_lore",
                                MessageReplacement.replace("%chance%", String.valueOf(entry.getValue()))));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (e.getClick().isRightClick()) {
                    point.removePeriodicReward(treasure.getId());
                    SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    openPeriodicRewards(p, point);
                } else {
                    promptPeriodicRewardChance(p, point, treasure);
                }
                return false;
            }));
        }
        gui.open(p);
    }

    private static int getPeriodicMiningCount(Player p, RegenPoint point) {
        return SuperMines.getInstance()
                .getPlayerDataManager()
                .getOrCreatePlayerData(p.getUniqueId())
                .getRegenPointMining(point.getId());
    }

    private static int getPeriodicMiningRemaining(Player p, RegenPoint point) {
        int interval = point.getPeriodicRewardIntervalBlocks();
        return interval <= 0 ? 0 : Math.max(0, interval - getPeriodicMiningCount(p, point));
    }

    private static void openPeriodicRewardChooser(Player p, RegenPoint point) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.regen-point-management.periodic_rewards.chooser_title", () -> openPeriodicRewards(p, point));
        Set<String> selected = point.getPeriodicRewardChances().keySet();
        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            if (selected.contains(treasure.getId())) continue;
            ItemStack item = treasure.getItemStack() == null
                    ? new ItemStack(Material.CHEST)
                    : treasure.getItemStack().clone();
            item.editMeta(meta -> meta.displayName(treasure.getDisplayName()));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                promptPeriodicRewardChance(p, point, treasure);
                return false;
            }));
        }
        gui.open(p);
    }

    private static void promptPeriodicRewardChance(Player p, RegenPoint point, Treasure treasure) {
        p.closeInventory();
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "gui.regen-point-management.periodic_rewards.chance_prompt",
                        MessageReplacement.replace("%treasure%", treasure.getRawDisplayName()));
        handleWeightInput(
                p,
                chance -> {
                    if (chance > 100) {
                        SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-percent");
                        openPeriodicRewards(p, point);
                        return;
                    }
                    point.setPeriodicRewardChance(treasure.getId(), chance);
                    SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    openPeriodicRewards(p, point);
                },
                "gui.input.invalid-percent",
                () -> openPeriodicRewards(p, point));
    }

    private static void openAddRegenPoint(Player p) {
        var target = p.getTargetBlockExact(5);
        if (target == null || target.getType().isAir()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.regenpoints.no-target-block");
            openRegenPointList(p);
            return;
        }

        Location loc = target.getLocation();
        if (SuperMines.getInstance().getRegenPointManager().getRegenPoint(loc) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.regenpoints.already-exists");
            openRegenPointList(p);
            return;
        }

        MaterialChooser.openUsableBlockChooser(p, chosen -> {
            TextInputDialog dialog = TextInputDialog.create(
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.regenpoints.create_title"),
                    SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.regenpoints.create_label"),
                    id -> {
                        if (!id.matches(Constants.ID_PATTERN)
                                || SuperMines.getInstance().getRegenPointManager().getRegenPoint(id) != null) {
                            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.invalid-id");
                            openRegenPointList(p);
                            return;
                        }

                        RegenPoint point = new RegenPoint(
                                id,
                                loc.getWorld(),
                                BlockPos.fromLocation(loc),
                                chosen,
                                SuperMines.getInstance().getRegenPointManager().getDefaultRespawnSeconds());
                        SuperMines.getInstance().getRegenPointManager().addRegenPoint(point);
                        openRegenPointManagementGui(p, point);
                    });
            dialog.show(p);
        });
    }

    private static List<Component> getRegenPointInfo(@NotNull Player p, @NotNull RegenPoint point) {
        MessageReplacement world =
                MessageReplacement.replace("%world%", point.getWorld().getName());
        MessageReplacement pos =
                MessageReplacement.replace("%pos%", point.getPos().toString());
        MessageReplacement block =
                MessageReplacement.replace("%block%", point.getBlock().getId());
        MessageReplacement seconds = MessageReplacement.replace("%seconds%", String.valueOf(point.getRespawnSeconds()));
        boolean pending = SuperMines.getInstance().getRegenPointManager().isPending(point);
        long remaining =
                pending ? Math.max(0L, (point.getRespawnAt() - System.currentTimeMillis() + 999L) / 1000L) : 0L;
        MessageReplacement blocks = MessageReplacement.replace(
                "%blocks%", String.valueOf(point.getBlocks().size()));
        MessageReplacement rewards = MessageReplacement.replace(
                "%rewards%", String.valueOf(point.getRewardChances().size()));
        MessageReplacement status = MessageReplacement.replace(
                "%status%",
                SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsg(
                                p,
                                pending
                                        ? "gui.regenpoints.status.respawning"
                                        : point.getRespawnSeconds() == 0
                                                ? "gui.regenpoints.status.disabled"
                                                : "gui.regenpoints.status.ready"));
        MessageReplacement remainingSeconds = MessageReplacement.replace("%remaining%", String.valueOf(remaining));
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        p,
                        "gui.regenpoints.info",
                        world,
                        pos,
                        block,
                        seconds,
                        blocks,
                        rewards,
                        status,
                        remainingSeconds);
    }

    /* Helper methods */
    static PaginatedChestGUI buildPagedGUI(Player p, String titleKey, Runnable back) {
        return buildPagedGUI(p, titleKey, back, null);
    }

    static PaginatedChestGUI buildPagedGUI(Player p, String titleKey, Runnable back, @Nullable MittelGUIItem copyItem) {
        MittelGUI.PagedChestBuilder builder = MittelGUI.pagedChestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey))
                .size(54)
                .structure("XXXXXXXXX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XCCCCCCCX", "XKXPXNXBX")
                .content('C')
                .previousPage('P', ButtonItem.unclickable(Constants.Items.PREVIOUS_PAGE.apply(p)))
                .nextPage('N', ButtonItem.unclickable(Constants.Items.NEXT_PAGE.apply(p)))
                .bind('X', ButtonItem.BACKGROUND);

        if (copyItem != null) {
            builder.bind('K', copyItem);
        } else {
            builder.bind('K', ButtonItem.BACKGROUND);
        }

        if (back != null) {
            builder = builder.bind('B', ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
                back.run();
                return false;
            }));
        } else {
            builder.bind('B', ButtonItem.BACKGROUND);
        }

        return builder.build();
    }

    static ChestGUI buildManagementGUI(Player p, String titleKey, MessageReplacement... replacements) {
        return MittelGUI.chestBuilder()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, titleKey, replacements))
                .size(54)
                .structure("XXXXXXXXX", "X       X", "X       X", "X       X", "X       X", "XXXXXXXXX")
                .bind('X', ButtonItem.BACKGROUND)
                .build();
    }

    static int slot(int row, int col) {
        return (row - 1) * 9 + (col - 1);
    }

    static boolean checkPermission(Player p, String permission) {
        if (!p.hasPermission(permission)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
            return false;
        }
        return true;
    }

    static void handleIntegerInput(Player p, Consumer<Integer> onSuccess) {
        handleIntegerInput(p, onSuccess, null);
    }

    private static void handleIntegerInput(Player p, Consumer<Integer> onSuccess, Runnable recovery) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                if (recovery != null) SuperMines.getInstance().getTaskMaker().runSync(recovery);
                return;
            }

            try {
                int value = Integer.parseInt(result);
                SuperMines.getInstance().getTaskMaker().runSync(() -> onSuccess.accept(value));
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-number");
                if (recovery != null) SuperMines.getInstance().getTaskMaker().runSync(recovery);
            }
        });
    }

    private static void handleWeightInput(Player p, Consumer<Double> onSuccess, String errorKey) {
        handleWeightInput(p, onSuccess, errorKey, null);
    }

    private static void handleWeightInput(Player p, Consumer<Double> onSuccess, String errorKey, Runnable recovery) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                if (recovery != null) SuperMines.getInstance().getTaskMaker().runSync(recovery);
                return;
            }

            try {
                double value = Double.parseDouble(result);
                if (!Double.isFinite(value) || value < Constants.WEIGHT_MIN) {
                    throw new NumberFormatException();
                }

                SuperMines.getInstance().getTaskMaker().runSync(() -> onSuccess.accept(value));
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, errorKey);
                if (recovery != null) SuperMines.getInstance().getTaskMaker().runSync(recovery);
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
        gui.putItem(slot(3, 2), ButtonItem.clickable(Constants.Items.SET_DISPLAY_NAME.apply(p, object), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.SET_DISPLAY_NAME)) return false;
            p.closeInventory();
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.set_display_name.prompt");
            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                    SuperMines.getInstance().getTaskMaker().runSync(reopen);
                    return;
                }
                object.setDisplayName(ComponentUtils.deserialize(result));

                switch (object) {
                    case Mine mine -> SuperMines.getInstance().getMineManager().saveMine(mine);
                    case Treasure treasure ->
                        SuperMines.getInstance().getTreasureManager().saveTreasure(treasure);
                    case Rank rank -> SuperMines.getInstance().getRankManager().saveRank(rank);
                    case RegenPoint point ->
                        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(point);
                    default -> {}
                }
                SuperMines.getInstance().getTaskMaker().runSync(reopen);
            });
            return false;
        }));

        // Back button
        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));
    }

    private static List<Component> getMineInfo(@NotNull Player p, @NotNull Mine mine) {
        Preconditions.checkNotNull(mine, "mine cannot be null");
        MessageReplacement world =
                MessageReplacement.replace("%world%", mine.getWorld().getName());
        MessageReplacement regenerateSeconds =
                MessageReplacement.replace("%regenerate_seconds%", String.valueOf(mine.getRegenerateSeconds()));
        if (mine.getArea() instanceof SphereArea(BlockPos center1, int radius1)) {
            MessageReplacement center = MessageReplacement.replace("%center%", center1.toString());
            MessageReplacement radius = MessageReplacement.replace("%radius%", String.valueOf(radius1));
            return SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mines.info-sphere", world, regenerateSeconds, center, radius);
        } else if (mine.getArea() instanceof CuboidArea(BlockPos pos3, BlockPos pos4)) {
            MessageReplacement pos1 = MessageReplacement.replace("%pos1%", pos3.toString());
            MessageReplacement pos2 = MessageReplacement.replace("%pos2%", pos4.toString());
            return SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, pos1, pos2);
        }
        MessageReplacement info =
                MessageReplacement.replace("%area%", mine.getArea().toString());
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, info);
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
