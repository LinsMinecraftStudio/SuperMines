package io.github.lijinhong11.supermines.gui;

import static io.github.lijinhong11.supermines.gui.GuiManager.CANCEL_COMMAND;
import static io.github.lijinhong11.supermines.gui.GuiManager.buildManagementGUI;
import static io.github.lijinhong11.supermines.gui.GuiManager.buildPagedGUI;
import static io.github.lijinhong11.supermines.gui.GuiManager.checkPermission;
import static io.github.lijinhong11.supermines.gui.GuiManager.handleIntegerInput;
import static io.github.lijinhong11.supermines.gui.GuiManager.openBlockSpawnEntries;
import static io.github.lijinhong11.supermines.gui.GuiManager.slot;

import io.github.lijinhong11.mittellib.gui.inventory.choosers.BiomeChooser;
import io.github.lijinhong11.mittellib.gui.inventory.impl.ChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.impl.PaginatedChestGUI;
import io.github.lijinhong11.mittellib.gui.inventory.item.ButtonItem;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.chat.ChatInput;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.AndGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.BiomeGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.BorderGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.MineYGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.NotGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.OrGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.PlaceholderGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.SurfaceGenerateCondition;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

final class GenerationConditionGui {
    private GenerationConditionGui() {}

    static void openEntryConditions(Player p, Mine mine, BlockSpawnEntry entry) {
        PaginatedChestGUI gui = buildPagedGUI(
                p, "gui.mine-management.block_spawn_entries.conditions.title", () -> openBlockSpawnEntries(p, mine));
        Runnable reopen = () -> openEntryConditions(p, mine, entry);

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            openConditionTypeChooser(
                    p,
                    condition -> {
                        entry.addGenerateCondition(condition);
                        SuperMines.getInstance().getMineManager().saveMine(mine);
                        IGenerateCondition[] holder = new IGenerateCondition[] {condition};
                        Consumer<IGenerateCondition> replace = updated -> {
                            entry.removeGenerateCondition(holder[0]);
                            entry.addGenerateCondition(updated);
                            holder[0] = updated;
                            SuperMines.getInstance().getMineManager().saveMine(mine);
                        };
                        Runnable remove = () -> {
                            entry.removeGenerateCondition(holder[0]);
                            SuperMines.getInstance().getMineManager().saveMine(mine);
                            reopen.run();
                        };
                        openConditionNodeEditor(p, mine, entry, condition, replace, remove, reopen);
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
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                    reopen.run();
                    return false;
                }

                IGenerateCondition[] holder = new IGenerateCondition[] {condition};
                Consumer<IGenerateCondition> replace = updated -> {
                    entry.removeGenerateCondition(holder[0]);
                    entry.addGenerateCondition(updated);
                    holder[0] = updated;
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                };
                Runnable remove = () -> {
                    entry.removeGenerateCondition(holder[0]);
                    SuperMines.getInstance().getMineManager().saveMine(mine);
                    reopen.run();
                };
                openConditionNodeEditor(p, mine, entry, condition, replace, remove, reopen);
                return false;
            }));
        }

        gui.open(p);
    }

    private static void openConditionTypeChooser(Player p, Consumer<IGenerateCondition> onPick, Runnable back) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.block_spawn_entries.conditions.chooser_title", back);

        List<String> keys = new ArrayList<>(List.of("surface", "mineY", "border", "biome", "and", "or", "not"));
        if (!availablePlaceholderTypes().isEmpty()) {
            keys.add(4, "placeholder");
        }
        for (String key : keys) {
            IGenerateCondition condition = createDefaultCondition(key);
            gui.addPageItem(ButtonItem.clickable(getConditionTypeItem(p, condition), (g, e) -> {
                onPick.accept(condition);
                return false;
            }));
        }

        gui.open(p);
    }

    private static ItemStack getConditionTypeItem(Player p, IGenerateCondition condition) {
        ItemStack item = new ItemStack(condition.icon());
        item.editMeta(meta -> {
            meta.displayName(condition.getDisplayName(p));
            meta.lore(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.conditions.types.add_lore"));
        });
        return item;
    }

    private static IGenerateCondition createDefaultCondition(String typeKey) {
        return switch (typeKey) {
            case "surface" -> new SurfaceGenerateCondition(1);
            case "mineY" -> new MineYGenerateCondition(0, 0);
            case "border" -> new BorderGenerateCondition(BorderGenerateCondition.Mode.RIM);
            case "biome" -> new BiomeGenerateCondition(List.of("PLAINS"));
            case "and" -> new AndGenerateCondition(new ArrayList<>());
            case "or" -> new OrGenerateCondition(new ArrayList<>());
            case "not" -> new NotGenerateCondition(new AndGenerateCondition(new ArrayList<>()));
            case "placeholder" ->
                new PlaceholderGenerateCondition(
                        "%player_name%", "", availablePlaceholderTypes().get(0));
            default -> throw new IllegalArgumentException("Unknown condition type: " + typeKey);
        };
    }

    private static void openConditionNodeEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            IGenerateCondition node,
            Consumer<IGenerateCondition> onSave,
            Runnable onDelete,
            Runnable back) {
        switch (node) {
            case AndGenerateCondition and ->
                openCompositeEditor(
                        p,
                        mine,
                        entry,
                        onSave,
                        "gui.mine-management.block_spawn_entries.conditions.and.title",
                        AndGenerateCondition::new,
                        new ArrayList<>(and.conditions()),
                        back);
            case OrGenerateCondition or ->
                openCompositeEditor(
                        p,
                        mine,
                        entry,
                        onSave,
                        "gui.mine-management.block_spawn_entries.conditions.or.title",
                        OrGenerateCondition::new,
                        new ArrayList<>(or.conditions()),
                        back);
            case NotGenerateCondition not -> openNotEditor(p, mine, entry, not, onSave, onDelete, back);
            case BiomeGenerateCondition biome -> openBiomeEditor(p, biome, onSave, onDelete, back);
            case PlaceholderGenerateCondition placeholder -> openPlaceholderEditor(p, placeholder, onSave, back);
            case null, default -> openLeafEditor(p, node, onSave, back);
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
        Runnable subDelete = () -> {
            list.remove(index);
            onSave.accept(build.apply(new ArrayList<>(list)));
            back.run();
        };
        openConditionNodeEditor(p, mine, entry, sub, subReplace, subDelete, back);
    }

    private static void openNotEditor(
            Player p,
            Mine mine,
            BlockSpawnEntry entry,
            NotGenerateCondition not,
            Consumer<IGenerateCondition> onSave,
            Runnable onDelete,
            Runnable back) {
        ChestGUI gui = buildManagementGUI(p, "gui.mine-management.block_spawn_entries.conditions.not.title");

        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));

        NotGenerateCondition[] holder = new NotGenerateCondition[] {not};
        gui.putItem(slot(3, 5), ButtonItem.clickable(getConditionItem(p, holder[0].inner()), (g, e) -> {
            if (!checkPermission(p, Constants.Permission.BLOCK_GENERATE)) return false;

            if (e.getClick().isRightClick()) {
                openConditionTypeChooser(
                        p,
                        newInner -> {
                            holder[0] = new NotGenerateCondition(newInner);
                            onSave.accept(holder[0]);
                            openNotEditor(p, mine, entry, holder[0], onSave, onDelete, back);
                        },
                        () -> openNotEditor(p, mine, entry, holder[0], onSave, onDelete, back));
                return false;
            }

            openConditionNodeEditor(
                    p,
                    mine,
                    entry,
                    holder[0].inner(),
                    newInner -> {
                        holder[0] = new NotGenerateCondition(newInner);
                        onSave.accept(holder[0]);
                        openNotEditor(p, mine, entry, holder[0], onSave, onDelete, back);
                    },
                    onDelete,
                    () -> openNotEditor(p, mine, entry, holder[0], onSave, onDelete, back));
            return false;
        }));

        gui.open(p);
    }

    private static void openBiomeEditor(
            Player p,
            BiomeGenerateCondition condition,
            Consumer<IGenerateCondition> onSave,
            Runnable onDelete,
            Runnable back) {
        PaginatedChestGUI gui =
                buildPagedGUI(p, "gui.mine-management.block_spawn_entries.conditions.biome.title", back);

        gui.addPageItem(ButtonItem.clickable(Constants.Items.ADD.apply(p), (g, e) -> {
            BiomeChooser.openBiomeChooser(p, chosen -> {
                List<String> biomes = new ArrayList<>(condition.getStringBiomes());
                String biomeKey = chosen.key().asString();
                if (!biomes.contains(biomeKey)) {
                    biomes.add(biomeKey);
                    BiomeGenerateCondition updated = new BiomeGenerateCondition(biomes);
                    onSave.accept(updated);
                    openBiomeEditor(p, updated, onSave, onDelete, back);
                    return;
                }
                openBiomeEditor(p, condition, onSave, onDelete, back);
            });
            return false;
        }));

        condition.getStringBiomes().stream().sorted().forEach(biomeKey -> {
            ItemStack item = new ItemStack(Material.OAK_SAPLING);
            item.editMeta(meta -> {
                meta.displayName(Component.text(biomeKey));
                meta.lore(SuperMines.getInstance()
                        .getLanguageManager()
                        .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.conditions.biome.each_lore"));
            });
            gui.addPageItem(ButtonItem.clickable(item, (g, e) -> {
                if (!e.getClick().isRightClick()) return false;
                if (condition.getStringBiomes().size() <= 1) {
                    onDelete.run();
                    return false;
                }

                List<String> biomes = new ArrayList<>(condition.getStringBiomes());
                biomes.remove(biomeKey);
                BiomeGenerateCondition updated = new BiomeGenerateCondition(biomes);
                onSave.accept(updated);
                openBiomeEditor(p, updated, onSave, onDelete, back);
                return false;
            }));
        });

        gui.open(p);
    }

    private static void openLeafEditor(
            Player p, IGenerateCondition node, Consumer<IGenerateCondition> onSave, Runnable back) {
        ChestGUI gui = buildManagementGUI(
                p,
                "gui.mine-management.block_spawn_entries.conditions.leaf.title",
                MessageReplacement.replace("%type%", node.key()));

        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));

        switch (node) {
            case SurfaceGenerateCondition surface ->
                gui.putItem(
                        slot(3, 5), ButtonItem.clickable(getMessagedLeafItem(p, "surface", surface.depth()), (g, e) -> {
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            p,
                                            "gui.mine-management.block_spawn_entries.conditions.leaf.surface"
                                                    + ".prompt");
                            handleIntegerInput(p, result -> {
                                if (result < 1) {
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(p, "gui.input.invalid-number");
                                    openLeafEditor(p, node, onSave, back);
                                    return;
                                }
                                SurfaceGenerateCondition updated = new SurfaceGenerateCondition(result);
                                onSave.accept(updated);
                                openLeafEditor(p, updated, onSave, back);
                            });
                            return false;
                        }));
            case MineYGenerateCondition mineY -> {
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
                                MineYGenerateCondition updated =
                                        new MineYGenerateCondition(result, mineY.getMaxYInMine());
                                onSave.accept(updated);
                                openLeafEditor(p, updated, onSave, back);
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
                                MineYGenerateCondition updated =
                                        new MineYGenerateCondition(mineY.getMinYInMine(), result);
                                onSave.accept(updated);
                                openLeafEditor(p, updated, onSave, back);
                            });
                            return false;
                        }));
            }
            case BorderGenerateCondition border ->
                gui.putItem(
                        slot(3, 5),
                        ButtonItem.clickable(
                                getMessagedLeafItem(
                                        p,
                                        "border",
                                        border.mode() == BorderGenerateCondition.Mode.RIM ? "RIM" : "CORE"),
                                (g, e) -> {
                                    BorderGenerateCondition.Mode next =
                                            border.mode() == BorderGenerateCondition.Mode.RIM
                                                    ? BorderGenerateCondition.Mode.CORE
                                                    : BorderGenerateCondition.Mode.RIM;
                                    BorderGenerateCondition updated = new BorderGenerateCondition(next);
                                    onSave.accept(updated);
                                    openLeafEditor(p, updated, onSave, back);
                                    return false;
                                }));
            default -> {}
        }

        gui.open(p);
    }

    private static void openPlaceholderEditor(
            Player p, PlaceholderGenerateCondition condition, Consumer<IGenerateCondition> onSave, Runnable back) {
        ChestGUI gui = buildManagementGUI(
                p,
                "gui.mine-management.block_spawn_entries.conditions.leaf.title",
                MessageReplacement.replace("%type%", condition.key()));

        gui.putItem(slot(1, 9), ButtonItem.clickable(Constants.Items.BACK.apply(p), (g, e) -> {
            back.run();
            return false;
        }));

        gui.putItem(
                slot(3, 3),
                ButtonItem.clickable(getMessagedLeafItem(p, "placeholder", condition.getPlaceholder()), (g, e) -> {
                    p.closeInventory();
                    SuperMines.getInstance()
                            .getLanguageManager()
                            .sendMessage(
                                    p, "gui.mine-management.block_spawn_entries.conditions.leaf.placeholder.prompt");
                    ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                        if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                            SuperMines.getInstance()
                                    .getTaskMaker()
                                    .runSync(() -> openPlaceholderEditor(p, condition, onSave, back));
                            return;
                        }
                        PlaceholderGenerateCondition updated = new PlaceholderGenerateCondition(
                                result, condition.getCompareContent(), condition.getParseType());
                        onSave.accept(updated);
                        SuperMines.getInstance()
                                .getTaskMaker()
                                .runSync(() -> openPlaceholderEditor(p, updated, onSave, back));
                    });
                    return false;
                }));

        gui.putItem(
                slot(3, 5),
                ButtonItem.clickable(
                        getMessagedLeafItem(p, "placeholder_compare", condition.getCompareContent()), (g, e) -> {
                            p.closeInventory();
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            p,
                                            "gui.mine-management.block_spawn_entries.conditions.leaf.placeholder_compare.prompt");
                            ChatInput.waitForPlayer(SuperMines.getInstance(), p, result -> {
                                if (result.equalsIgnoreCase(CANCEL_COMMAND)) {
                                    SuperMines.getInstance()
                                            .getTaskMaker()
                                            .runSync(() -> openPlaceholderEditor(p, condition, onSave, back));
                                    return;
                                }
                                PlaceholderGenerateCondition updated = new PlaceholderGenerateCondition(
                                        condition.getPlaceholder(), result, condition.getParseType());
                                onSave.accept(updated);
                                SuperMines.getInstance()
                                        .getTaskMaker()
                                        .runSync(() -> openPlaceholderEditor(p, updated, onSave, back));
                            });
                            return false;
                        }));

        gui.putItem(
                slot(3, 7),
                ButtonItem.clickable(
                        getMessagedLeafItem(p, "placeholder_parse_type", condition.getParseType()), (g, e) -> {
                            List<PlaceholderGenerateCondition.ParseType> values = availablePlaceholderTypes();
                            if (values.isEmpty()) return false;
                            int current = Math.max(0, values.indexOf(condition.getParseType()));
                            PlaceholderGenerateCondition.ParseType next = values.get((current + 1) % values.size());
                            PlaceholderGenerateCondition updated = new PlaceholderGenerateCondition(
                                    condition.getPlaceholder(), condition.getCompareContent(), next);
                            onSave.accept(updated);
                            openPlaceholderEditor(p, updated, onSave, back);
                            return false;
                        }));

        gui.open(p);
    }

    private static List<PlaceholderGenerateCondition.ParseType> availablePlaceholderTypes() {
        List<PlaceholderGenerateCondition.ParseType> available = new ArrayList<>();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            available.add(PlaceholderGenerateCondition.ParseType.PLACEHOLDERAPI);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            available.add(PlaceholderGenerateCondition.ParseType.MINIPLACEHOLDERS);
        }
        return available;
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
        ItemStack item = new ItemStack(condition.icon());
        item.editMeta(meta -> {
            meta.displayName(condition.getDisplayName(p));
            List<Component> lore = new ArrayList<>(condition.getLore(p));
            lore.addAll(SuperMines.getInstance()
                    .getLanguageManager()
                    .getMsgComponentList(p, "gui.mine-management.block_spawn_entries.conditions.each_lore"));
            meta.lore(lore);
        });
        return item;
    }
}
