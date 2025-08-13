package io.github.lijinhong11.supermines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.chat.ChatInput;
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

public class GuiManager {
    public static void openGeneral(Player p) {
        Gui gui = Gui.gui()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.general.title"))
                .rows(3)
                .create();

        gui.setItem(2, 3, ItemBuilder.from(Constants.Items.MINES.apply(p)).asGuiItem(e -> {
            openMineList(p);
            e.setCancelled(true);
        }));
        gui.setItem(2, 5, ItemBuilder.from(Constants.Items.TREASURES.apply(p)).asGuiItem(e -> {
            openTreasureList(p);
            e.setCancelled(true);
        }));
        gui.setItem(2, 7, ItemBuilder.from(Constants.Items.RANKS.apply(p)).asGuiItem(e -> {
            openRankList(p);
            e.setCancelled(true);
        }));

        gui.open(p);
    }

    public static void openMineList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.mines.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            Material mat = mine.getDisplayIcon() == null ? Constants.Items.DEFAULT_MINE_ICON : mine.getDisplayIcon();
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(mine.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getMineInfo(p, mine))
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

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.mine-management.title", mineName))
                .rows(6)
                .create();

        placeCommon(p, gui, mine, mine.getDisplayIcon(), () -> openMineManagementGui(p, mine), () -> openMineList(p));

        putItem(3, 4, gui, ItemBuilder.from(Constants.Items.SET_DISPLAY_ICON.apply(p, mine.getDisplayIcon())), e -> {
            Material m = openMaterialChooser(p, mt -> true, () -> openMineManagementGui(p, mine));
            mine.setDisplayIcon(m);

            openMineManagementGui(p, mine);
        });
        putItem(
                3,
                6,
                gui,
                ItemBuilder.from(Constants.Items.SET_REGEN_SECONDS.apply(p, mine.getRegenerateSeconds())),
                e -> {
                    if (!p.hasPermission(Constants.Permission.SET_RESET_TIME)) {
                        SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                        return;
                    }

                    gui.close(p);

                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_reset_time.prompt");
                    ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                        if (result.equalsIgnoreCase("##CANCEL")) {
                            return;
                        }

                        try {
                            int time = Integer.parseUnsignedInt(result);
                            mine.setRegenerateSeconds(time);

                            SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        } catch (NumberFormatException ex) {
                            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-number");
                        }

                        openMineManagementGui(p, mine);
                    });
                });
        putItem(
                3,
                8,
                gui,
                ItemBuilder.from(Constants.Items.ONLY_FILL_AIR.apply(p, mine.isOnlyFillAirWhenRegenerate())),
                e -> {
                    if (!p.hasPermission(Constants.Permission.SET_ONLY_FILL_AIR)) {
                        SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                        return;
                    }

                    mine.setOnlyFillAirWhenRegenerate(!mine.isOnlyFillAirWhenRegenerate());

                    openMineManagementGui(p, mine);
                });
        putItem(
                4,
                3,
                gui,
                ItemBuilder.from(Constants.Items.SET_REQUIRED_RANK_LEVEL.apply(p, mine.getRequiredRankLevel())),
                e -> {
                    if (!p.hasPermission(Constants.Permission.SET_REQUIRED_LEVEL)) {
                        SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                        return;
                    }

                    gui.close(p);

                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(p, "gui.mine-management.set_required_lvl.prompt");
                    ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                        if (result.equalsIgnoreCase("##CANCEL")) {
                            return;
                        }

                        try {
                            int lvl = Integer.parseUnsignedInt(result);
                            mine.setRequiredRankLevel(lvl);

                            SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                        } catch (NumberFormatException ex) {
                            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-number");
                        }

                        openMineManagementGui(p, mine);
                    });
                });
        putItem(4, 5, gui, ItemBuilder.from(Constants.Items.BLOCK_SPAWN_ENTRIES.apply(p)), e -> {
            if (!p.hasPermission(Constants.Permission.BLOCK_GENERATE)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                return;
            }

            openBlockSpawnEntries(p, mine);
        });

        gui.open(p);
    }

    private static void openBlockSpawnEntries(Player p, Mine mine) {
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .pageSize(45)
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.mine-management.block_spawn.title"))
                .create();

        fillPageButtons(p, gui, () -> openMineManagementGui(p, mine));

        putItem(6, 1, gui, ItemBuilder.from(Constants.Items.ADD.apply(p)), e -> {
            Material m = openMaterialChooser(p, Material::isBlock, () -> {});

            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            p,
                            "gui.mine-management.block_spawn.add_prompt",
                            MessageReplacement.replace("%material%", m.toString()));
            addBlockSpawnEntry(p, mine, m);
        });

        for (Map.Entry<Material, Double> entry : mine.getBlockSpawnEntries().entrySet()) {
            MessageReplacement r = MessageReplacement.replace("%precent%", String.valueOf(entry.getValue()));
            List<Component> lore = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn.each_lore", r);
            GuiItem item = ItemBuilder.from(entry.getKey()).lore(lore).asGuiItem(e -> {
                if (!p.hasPermission(Constants.Permission.BLOCK_GENERATE)) {
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                    return;
                }

                ClickType type = e.getClick();
                Material material = entry.getKey();
                if (type.isLeftClick()) {
                    gui.close(p);

                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(
                                    p,
                                    "gui.mine-management.block_spawn.set_precent_prompt",
                                    MessageReplacement.replace("%material%", material.toString()));
                    addBlockSpawnEntry(p, mine, material);
                } else if (type.isRightClick()) {
                    mine.removeBlockSpawnEntry(material);

                    openBlockSpawnEntries(p, mine);
                }

                e.setCancelled(true);
            });
            gui.addItem(item);
        }

        gui.open(p);
    }

    private static void addBlockSpawnEntry(Player p, Mine mine, Material material) {
        ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
            if (result.equalsIgnoreCase("##CANCEL")) {
                return;
            }

            try {
                double d = Double.parseDouble(result);

                if (d >= 100 || d < 0) {
                    throw new NumberFormatException();
                }

                mine.addBlockSpawnEntry(material, d);
            } catch (NumberFormatException ex) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-precent");
            }

            openBlockSpawnEntries(p, mine);
        });
    }

    public static void openTreasureList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.treasures.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));

        for (Treasure treasure : SuperMines.getInstance().getTreasureManager().getAllTreasures()) {
            Material mat = Material.CHEST;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(treasure.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getTreasureInfo(p, treasure))
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

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.title", treasureName))
                .rows(6)
                .create();

        placeCommon(p, gui, treasure, Material.CHEST, () -> openTreasureManagementGui(p, treasure), () -> openTreasureList(p));

        putItem(3, 4, gui, ItemBuilder.from(Constants.Items.SET_CHANCE.apply(p, treasure.getChance())), e -> {
            if (!p.hasPermission(Constants.Permission.TREASURES)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                return;
            }

            gui.close(p);

            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.treasure-management.set_chance.prompt");
            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                if (result.equalsIgnoreCase("##CANCEL")) {
                    return;
                }

                try {
                    double d = Double.parseDouble(result);

                    if (d >= 100 || d < 0) {
                        throw new NumberFormatException();
                    }

                    treasure.setChance(d);
                } catch (NumberFormatException ex) {
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-precent");
                }

                openTreasureManagementGui(p, treasure);
            });
        });

        putItem(
                3,
                8,
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

        putItem(3, 6, gui, ItemBuilder.from(clone), e -> {
            if (!p.hasPermission(Constants.Permission.TREASURES)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                return;
            }

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
        PaginatedGui gui = Gui.paginated()
                .rows(6)
                .pageSize(45)
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.treasure-management.matched_materials.title"))
                .create();

        fillPageButtons(p, gui, () -> openTreasureManagementGui(p, treasure));

        putItem(6, 1, gui, ItemBuilder.from(Constants.Items.ADD.apply(p)), e -> {
            Material m = openMaterialChooser(p, Material::isBlock, () -> {});

            if (!treasure.getMatchedMaterials().contains(m)) {
                treasure.addMatchedMaterial(m);
            }

            openMatchedMaterials(p, treasure);
        });

        for (Material material : treasure.getMatchedMaterials()) {
            List<Component> lore = SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.treasure-management.matched_materials.each_lore");
            GuiItem item = ItemBuilder.from(material).lore(lore).asGuiItem(e -> {
                if (!p.hasPermission(Constants.Permission.BLOCK_GENERATE)) {
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                    return;
                }

                treasure.removeMatchedMaterial(material);
                openMatchedMaterials(p, treasure);

                e.setCancelled(true);
            });
            gui.addItem(item);
        }

        gui.open(p);
    }

    public static void openRankList(Player p) {
        PaginatedGui gui = Gui.paginated()
                .pageSize(45)
                .rows(6)
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.ranks.title"))
                .create();

        fillPageButtons(p, gui, () -> openGeneral(p));
        for (Rank rank : SuperMines.getInstance().getRankManager().getAllRanks()) {
            Material mat = Material.NAME_TAG;
            GuiItem guiItem = ItemBuilder.from(mat)
                    .name(rank.getDisplayName())
                    .lore(SuperMines.getInstance().getLanguageManager().getRankInfo(p, rank))
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

        Gui gui = Gui.gui()
                .title(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponent(p, "gui.rank-management.title", rankName))
                .rows(6)
                .create();

        placeCommon(p, gui, rank, Material.NAME_TAG, () -> openRankManagementGui(p, rank), () -> openRankList(p));

        putItem(3, 4, gui, ItemBuilder.from(Constants.Items.SET_RANK_LEVEL.apply(p, rank.getLevel())), e -> {
            if (!p.hasPermission(Constants.Permission.RANKS)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                return;
            }

            gui.close(p);

            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.rank-management.setlevel.prompt");
            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                if (result.equalsIgnoreCase("##CANCEL")) {
                    return;
                }

                try {
                    int lvl = Integer.parseUnsignedInt(result);
                    rank.setLevel(lvl);
                } catch (NumberFormatException ex) {
                    SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.input.invalid-number");
                }

                openRankManagementGui(p, rank);
            });
        });
    }

    /* common methods */
    private static void fillPageButtons(Player p, PaginatedGui gui, Runnable reopen) {
        gui.getFiller()
                .fillBetweenPoints(
                        6, 1, 6, 9, ItemBuilder.from(Constants.Items.BACKGROUND).asGuiItem(e -> e.setCancelled(true)));

        putItem(6, 1, gui, ItemBuilder.from(Constants.Items.CLOSE.apply(p)), e -> gui.close(p));
        putItem(6, 3, gui, ItemBuilder.from(Constants.Items.PREVIOUS_PAGE.apply(p)), e -> gui.previous());
        putItem(6, 7, gui, ItemBuilder.from(Constants.Items.NEXT_PAGE.apply(p)), e -> gui.next());
        putItem(6, 9, gui, ItemBuilder.from(Constants.Items.BACK.apply(p)), e -> reopen.run());
    }

    private static void putItem(
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
                2,
                5,
                gui,
                ItemBuilder.from(icon)
                        .name(object.getDisplayName())
                        .lore(ComponentUtils.deserialize("&7&lID: " + object.getId())),
                e -> {});

        putItem(3, 2, gui, ItemBuilder.from(Constants.Items.SET_DISPLAY_NAME.apply(p, object)), e -> {
            if (!p.hasPermission(Constants.Permission.SET_DISPLAY_NAME)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(p, "common.no-permission");
                return;
            }

            gui.close(p);

            SuperMines.getInstance().getLanguageManager().sendMessage(p, "gui.set_display_name.prompt");
            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                if (result.equalsIgnoreCase("##CANCEL")) {
                    return;
                }

                object.setDisplayName(ComponentUtils.deserialize(result));

                reopen.run();
            });
        });

        putItem(1, 9, gui, ItemBuilder.from(Constants.Items.BACK.apply(p)), e -> back.run());
    }

    private static Material openMaterialChooser(Player p, Predicate<Material> predicate, Runnable reopen) {
        PaginatedGui gui = Gui.paginated()
                .title(SuperMines.getInstance().getLanguageManager().getMsgComponent(p, "gui.material-chooser.title"))
                .rows(6)
                .create();

        fillPageButtons(p, gui, reopen);

        AtomicReference<Material> selected = new AtomicReference<>();

        for (Material material : Material.values()) {
            if (material.isAir()) {
                continue;
            }

            if (!predicate.test(material)) {
                continue;
            }

            GuiItem guiItem = ItemBuilder.from(material).asGuiItem(e -> {
                selected.set(material);
                reopen.run();
                e.setCancelled(true);
            });

            gui.addItem(guiItem);
        }

        gui.open(p);

        return selected.get();
    }
}
