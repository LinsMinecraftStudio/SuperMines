package io.github.lijinhong11.supermines.gui;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.gui.inventory.MittelGUI;
import io.github.lijinhong11.mittellib.gui.inventory.choosers.MaterialChooser;
import io.github.lijinhong11.mittellib.gui.inventory.impl.ChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.hook.content.MinecraftContentProvider;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.math.SphereArea;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.chat.ChatInput;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.AndGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.BiomeGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.BorderGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.ChanceGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.MineYGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.NotGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.OrGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.SurfaceGenerateCondition;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
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
                .structure("xxxxxxxxx", "xxMxTxRxx", "xxxxxxxxx")
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
                    openMaterialChooser(p, chosen -> {
                        mine.setDisplayIcon(chosen.toItem().getType());
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
                                    .sendMessage(p, "gui.mine-management.set_reset_time.prompt");
                            handleIntegerInput(p, result -> {
                                mine.setRegenerateSeconds(result);
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
                    reopen.run();
                    return false;
                }));

        gui.open(p);
    }

    private static void openBlockSpawnEntries(Player p, Mine mine) {
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
                    openEntryConditions(p, mine, entry.getKey());
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
        gui.putItem(
                slot(3, 4), ButtonItem.clickable(Constants.Items.SET_WEIGHT.apply(p, treasure.getWeight()), (g, e) -> {
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
        gui.putItem(slot(3, 8), ButtonItem.clickable(Constants.Items.MATCHED_MATERIALS.apply(p), (g, e) -> {
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
                () -> MaterialChooser.openUsableBlockChooser(p, chosen -> {
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

        gui.putItem(
                slot(3, 4), ButtonItem.clickable(Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel()), (g, e) -> {
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
                .structure("xxxxxxxxx", "xcccccccx", "xcccccccx", "xcccccccx", "xcccccccx", "xxxpxnxbx")
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
                .structure("xxxxxxxxx", "x       x", "x       x", "x       x", "x       x", "xxxxxxxxx")
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
                SuperMines.getInstance().getTaskMaker().runSync(() -> onSuccess.accept(value));
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
                SuperMines.getInstance().getTaskMaker().runSync(() -> onSuccess.accept(value));
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
        gui.putItem(slot(3, 2), ButtonItem.clickable(Constants.Items.SET_DISPLAY_NAME.apply(p, object), (g, e) -> {
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
        openBlockChooser(
                p,
                b -> {
                    if (b instanceof MinecraftContentProvider.PackedMinecraftBlock(Material material)) {
                        return material.isBlock() && material.isItem();
                    } else {
                        return false;
                    }
                },
                callback);
    }

    private static void openBlockChooser(Player p, Predicate<PackedBlock> predicate, Consumer<PackedBlock> callback) {
        PaginatedChestGUI gui = buildPagedGUI(p, "gui.material-chooser.title", null);

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

    private static void openEntryConditions(Player p, Mine mine, BlockSpawnEntry entry) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.mine-management.block_spawn_entries.conditions.title", () -> openBlockSpawnEntries(p, mine));
        Runnable reopen = () -> openEntryConditions(p, mine, entry);

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openConditionTypeChooser(
                    p,
                    mine,
                    entry,
                    condition -> {
                        entry.addGenerateCondition(condition);
                        IGenerateCondition[] holder = new IGenerateCondition[] {condition};
                        Consumer<IGenerateCondition> replace = updated -> {
                            entry.removeGenerateCondition(holder[0]);
                            entry.addGenerateCondition(updated);
                            holder[0] = updated;
                        };
                        openConditionNodeEditor(p, mine, entry, condition, replace, reopen);
                    },
                    reopen);
            return false;
        }));

        for (IGenerateCondition condition : entry.getConditions()) {
            ItemStack item = getConditionItem(p, condition);
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

                if (e.getClick().isRightClick()) {
                    entry.removeGenerateCondition(condition);
                    reopen.run();
                    return false;
                }

                IGenerateCondition[] holder = new IGenerateCondition[] {condition};
                Consumer<IGenerateCondition> replace = updated -> {
                    entry.removeGenerateCondition(holder[0]);
                    entry.addGenerateCondition(updated);
                    holder[0] = updated;
                };
                openConditionNodeEditor(p, mine, entry, condition, replace, reopen);
                return false;
            }));
        }

        gui.open(p);
    }

    private static void openConditionTypeChooser(
            Player p, Mine mine, BlockSpawnEntry entry, Consumer<IGenerateCondition> onPick, Runnable back) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.block_spawn_entries.conditions.chooser_title", back);

        for (String key : new String[] {"chance", "surface", "mineY", "border", "biome", "and", "or", "not"}) {
            gui.addPageItem(ButtonItem.clickable(getConditionTypeItem(p, key), (g, e) -> {
                onPick.accept(createDefaultCondition(key));
                return false;
            }));
        }

        gui.open(p);
    }

    private static IGenerateCondition createDefaultCondition(String typeKey) {
        return switch (typeKey) {
            case "chance" -> new ChanceGenerateCondition(0.5);
            case "surface" -> new SurfaceGenerateCondition(1);
            case "mineY" -> new MineYGenerateCondition(0, 0);
            case "border" -> new BorderGenerateCondition(BorderGenerateCondition.Mode.RIM);
            case "biome" -> new BiomeGenerateCondition(List.of("PLAINS"));
            case "and" -> new AndGenerateCondition(new ArrayList<>());
            case "or" -> new OrGenerateCondition(new ArrayList<>());
            case "not" -> new NotGenerateCondition(new ChanceGenerateCondition(0.5));
            default -> throw new IllegalArgumentException("Unknown condition type: " + typeKey);
        };
    }

    private static void openConditionNodeEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            IGenerateCondition node,
            Consumer<IGenerateCondition> onSave,
            Runnable back) {
        if (node instanceof AndGenerateCondition and) {
            openCompositeEditor(
                    p,
                    mine,
                    entry,
                    onSave,
                    "gui.mine-management.block_spawn_entries.conditions.and.title",
                    AndGenerateCondition::new,
                    new ArrayList<>(and.getConditions()),
                    back);
        } else if (node instanceof OrGenerateCondition or) {
            openCompositeEditor(
                    p,
                    mine,
                    entry,
                    onSave,
                    "gui.mine-management.block_spawn_entries.conditions.or.title",
                    OrGenerateCondition::new,
                    new ArrayList<>(or.getConditions()),
                    back);
        } else if (node instanceof NotGenerateCondition not) {
            openNotEditor(p, mine, entry, not, onSave, back);
        } else {
            openLeafEditor(p, mine, entry, node, onSave, back);
        }
    }

    private static void openCompositeEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            Consumer<IGenerateCondition> onSave,
            String titleKey,
            Function<List<IGenerateCondition>, IGenerateCondition> build,
            List<IGenerateCondition> current,
            Runnable back) {
        PaginatedChestGUI gui = buildPagedGUI(p, titleKey, back);

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openConditionTypeChooser(
                    p,
                    mine,
                    entry,
                    sub -> {
                        List<IGenerateCondition> updated = new ArrayList<>(current);
                        updated.add(sub);
                        onSave.accept(build.apply(new ArrayList<>(updated)));
                        editSubCondition(
                                p,
                                mine,
                                entry,
                                onSave,
                                build,
                                updated,
                                updated.size() - 1,
                                () -> openCompositeEditor(p, mine, entry, onSave, titleKey, build, updated, back));
                    },
                    () -> openCompositeEditor(p, mine, entry, onSave, titleKey, build, current, back));
            return false;
        }));

        for (int i = 0; i < current.size(); i++) {
            int index = i;
            ItemStack item = getConditionItem(p, current.get(i));
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

                if (e.getClick().isRightClick()) {
                    List<IGenerateCondition> updated = new ArrayList<>(current);
                    updated.remove(index);
                    onSave.accept(build.apply(updated));
                    openCompositeEditor(p, mine, entry, onSave, titleKey, build, updated, back);
                    return false;
                }

                editSubCondition(
                        p,
                        mine,
                        entry,
                        onSave,
                        build,
                        current,
                        index,
                        () -> openCompositeEditor(p, mine, entry, onSave, titleKey, build, current, back));
                return false;
            }));
        }

        gui.open(p);
    }

    private static void editSubCondition(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            Consumer<IGenerateCondition> onSave,
            Function<List<IGenerateCondition>, IGenerateCondition> build,
            List<IGenerateCondition> list,
            int index,
            Runnable back) {
        IGenerateCondition sub = list.get(index);
        Consumer<IGenerateCondition> subReplace = updated -> {
            list.set(index, updated);
            onSave.accept(build.apply(new ArrayList<>(list)));
        };
        openConditionNodeEditor(p, mine, entry, sub, subReplace, back);
    }

    private static void openNotEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            NotGenerateCondition not,
            Consumer<IGenerateCondition> onSave,
            Runnable back) {
        ChestGUI gui = buildManagementGUI(p, "gui.mine-management.block_spawn_entries.conditions.not.title");

        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));

        NotGenerateCondition[] holder = new NotGenerateCondition[] {not};
        gui.putItem(slot(3, 5), ButtonItem.clickable(getConditionItem(p, holder[0].getInner()), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

            if (e.getClick().isRightClick()) {
                openConditionTypeChooser(
                        p,
                        mine,
                        entry,
                        newInner -> {
                            holder[0] = new NotGenerateCondition(newInner);
                            onSave.accept(holder[0]);
                            openNotEditor(p, mine, entry, holder[0], onSave, back);
                        },
                        () -> openNotEditor(p, mine, entry, holder[0], onSave, back));
                return false;
            }

            openConditionNodeEditor(
                    p,
                    mine,
                    entry,
                    holder[0].getInner(),
                    newInner -> {
                        holder[0] = new NotGenerateCondition(newInner);
                        onSave.accept(holder[0]);
                        openNotEditor(p, mine, entry, holder[0], onSave, back);
                    },
                    () -> openNotEditor(p, mine, entry, holder[0], onSave, back));
            return false;
        }));

        gui.open(p);
    }

    private static void openLeafEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            IGenerateCondition node,
            Consumer<IGenerateCondition> onSave,
            Runnable back) {
        ChestGUI gui = buildManagementGUI(
                p,
                "gui.mine-management.block_spawn_entries.conditions.leaf.title",
                MessageReplacement.replace("%type%", node.key()));

        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));

        if (node instanceof ChanceGenerateCondition chance) {
            gui.putItem(
                    slot(3, 5), ButtonItem.clickable(getMessagedLeafItem(p, "chance", chance.getChance()), (g, e) -> {
                        p.closeInventory();
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .sendMessage(
                                        p,
                                        "gui.mine-management.block_spawn_entries.conditions.leaf.chance" + ".prompt");
                        handleDoubleInput(
                                p,
                                0,
                                result -> {
                                    if (result > 1) {
                                        SuperMines.getInstance()
                                                .getLanguageManager()
                                                .sendMessage(p, "gui.input.invalid-number");
                                        openLeafEditor(p, mine, entry, node, onSave, back);
                                        return;
                                    }
                                    ChanceGenerateCondition updated = new ChanceGenerateCondition(result);
                                    onSave.accept(updated);
                                    openLeafEditor(p, mine, entry, updated, onSave, back);
                                },
                                "gui.input.invalid-number");
                        return false;
                    }));
        } else if (node instanceof SurfaceGenerateCondition surface) {
            gui.putItem(
                    slot(3, 5), ButtonItem.clickable(getMessagedLeafItem(p, "surface", surface.getDepth()), (g, e) -> {
                        p.closeInventory();
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .sendMessage(
                                        p,
                                        "gui.mine-management.block_spawn_entries.conditions.leaf.surface" + ".prompt");
                        handleIntegerInput(p, result -> {
                            if (result < 1) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(p, "gui.input.invalid-number");
                                openLeafEditor(p, mine, entry, node, onSave, back);
                                return;
                            }
                            SurfaceGenerateCondition updated = new SurfaceGenerateCondition(result);
                            onSave.accept(updated);
                            openLeafEditor(p, mine, entry, updated, onSave, back);
                        });
                        return false;
                    }));
        } else if (node instanceof MineYGenerateCondition mineY) {
            gui.putItem(
                    slot(3, 4),
                    ButtonItem.clickable(getMessagedLeafItem(p, "mineY_min", mineY.getMinYInMine()), (g, e) -> {
                        p.closeInventory();
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .sendMessage(
                                        p,
                                        "gui.mine-management.block_spawn_entries.conditions.leaf.mineY_min"
                                                + ".prompt");
                        handleIntegerInput(p, result -> {
                            MineYGenerateCondition updated = new MineYGenerateCondition(result, mineY.getMaxYInMine());
                            onSave.accept(updated);
                            openLeafEditor(p, mine, entry, updated, onSave, back);
                        });
                        return false;
                    }));
            gui.putItem(
                    slot(3, 6),
                    ButtonItem.clickable(getMessagedLeafItem(p, "mineY_max", mineY.getMaxYInMine()), (g, e) -> {
                        p.closeInventory();
                        SuperMines.getInstance()
                                .getLanguageManager()
                                .sendMessage(
                                        p,
                                        "gui.mine-management.block_spawn_entries.conditions.leaf.mineY_max"
                                                + ".prompt");
                        handleIntegerInput(p, result -> {
                            MineYGenerateCondition updated = new MineYGenerateCondition(mineY.getMinYInMine(), result);
                            onSave.accept(updated);
                            openLeafEditor(p, mine, entry, updated, onSave, back);
                        });
                        return false;
                    }));
        } else if (node instanceof BorderGenerateCondition border) {
            gui.putItem(
                    slot(3, 5),
                    ButtonItem.clickable(
                            getMessagedLeafItem(
                                    p, "border", border.getMode() == BorderGenerateCondition.Mode.RIM ? "RIM" : "CORE"),
                            (g, e) -> {
                                BorderGenerateCondition.Mode next = border.getMode() == BorderGenerateCondition.Mode.RIM
                                        ? BorderGenerateCondition.Mode.CORE
                                        : BorderGenerateCondition.Mode.RIM;
                                BorderGenerateCondition updated = new BorderGenerateCondition(next);
                                onSave.accept(updated);
                                openLeafEditor(p, mine, entry, updated, onSave, back);
                                return false;
                            }));
        } else if (node instanceof BiomeGenerateCondition biome) {
            gui.putItem(
                    slot(3, 5),
                    ButtonItem.clickable(
                            getMessagedLeafItem(p, "biome", String.join(", ", biome.getBiomes())), (g, e) -> {
                                p.closeInventory();
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(
                                                p,
                                                "gui.mine-management.block_spawn_entries.conditions.leaf.biome"
                                                        + ".prompt");
                                ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                                    if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                                        return;
                                    }

                                    List<String> parsed = Arrays.stream(result.split(","))
                                            .map(String::trim)
                                            .map(String::toUpperCase)
                                            .filter(name -> !name.isEmpty())
                                            .toList();
                                    if (parsed.isEmpty()) {
                                        SuperMines.getInstance()
                                                .getLanguageManager()
                                                .sendMessage(p, "gui.input.invalid-number");
                                        return;
                                    }
                                    BiomeGenerateCondition updated = new BiomeGenerateCondition(parsed);
                                    onSave.accept(updated);
                                    SuperMines.getInstance()
                                            .getTaskMaker()
                                            .runSync(() -> openLeafEditor(p, mine, entry, updated, onSave, back));
                                });
                                return false;
                            }));
        }

        gui.open(p);
    }

    private static ItemStack getMessagedLeafItem(Player p, String paramKey, Object value) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMessagedItem(
                        Material.REPEATER,
                        "gui.mine-management.block_spawn_entries.conditions.leaf." + paramKey,
                        p,
                        MessageReplacement.replace("%value%", String.valueOf(value)));
    }

    private static ItemStack getConditionItem(Player p, IGenerateCondition condition) {
        String typeKey = condition.key();
        ItemStack item = new ItemStack(getConditionMaterial(typeKey));
        item.editMeta(meta -> {
            meta.displayName(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponent(
                            p, "gui.mine-management.block_spawn_entries.conditions.types." + typeKey + ".name"));
            List<Component> lore = new ArrayList<>(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(
                            p,
                            "gui.mine-management.block_spawn_entries.conditions.description." + typeKey,
                            getConditionReplacements(condition)));
            lore.addAll(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.conditions.each_lore"));
            meta.lore(lore);
        });
        return item;
    }

    private static ItemStack getConditionTypeItem(Player p, String typeKey) {
        ItemStack item = new ItemStack(getConditionMaterial(typeKey));
        item.editMeta(meta -> {
            meta.displayName(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponent(
                            p, "gui.mine-management.block_spawn_entries.conditions.types." + typeKey + ".name"));
            meta.lore(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.conditions.types.add_lore"));
        });
        return item;
    }

    private static MessageReplacement[] getConditionReplacements(IGenerateCondition condition) {
        List<MessageReplacement> result = new ArrayList<>();
        if (condition instanceof ChanceGenerateCondition c) {
            result.add(MessageReplacement.replace("%chance%", String.valueOf(c.getChance())));
        } else if (condition instanceof SurfaceGenerateCondition s) {
            result.add(MessageReplacement.replace("%depth%", String.valueOf(s.getDepth())));
        } else if (condition instanceof MineYGenerateCondition m) {
            result.add(MessageReplacement.replace("%min%", String.valueOf(m.getMinYInMine())));
            result.add(MessageReplacement.replace("%max%", String.valueOf(m.getMaxYInMine())));
        } else if (condition instanceof BorderGenerateCondition b) {
            result.add(MessageReplacement.replace("%mode%", b.getMode().name()));
        } else if (condition instanceof BiomeGenerateCondition biome) {
            result.add(MessageReplacement.replace("%biomes%", String.join(", ", biome.getBiomes())));
        } else if (condition instanceof AndGenerateCondition and) {
            result.add(MessageReplacement.replace(
                    "%amount%", String.valueOf(and.getConditions().size())));
        } else if (condition instanceof OrGenerateCondition or) {
            result.add(MessageReplacement.replace(
                    "%amount%", String.valueOf(or.getConditions().size())));
        } else if (condition instanceof NotGenerateCondition not) {
            result.add(MessageReplacement.replace("%inner%", not.getInner().key()));
        }
        return result.toArray(new MessageReplacement[0]);
    }

    private static Material getConditionMaterial(String typeKey) {
        return switch (typeKey) {
            case "chance" -> Material.PAPER;
            case "surface" -> Material.GRASS_BLOCK;
            case "mineY" -> Material.LADDER;
            case "border" -> Material.OAK_FENCE;
            case "biome" -> Material.OAK_SAPLING;
            case "and" -> Material.GREEN_WOOL;
            case "or" -> Material.ORANGE_WOOL;
            case "not" -> Material.RED_WOOL;
            default -> Material.NAME_TAG;
        };
    }
}
