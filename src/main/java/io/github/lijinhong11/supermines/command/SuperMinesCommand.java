package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.gui.GuiManager;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SuperMinesCommand {

    private static final Map<UUID, AreaSelection> selectionMap = new ConcurrentHashMap<>();

    public static void handlePos(Player player, boolean isPos1, Location forcedLoc) {
        Location loc = forcedLoc != null ? forcedLoc : player.getLocation();

        if (SuperMines.getInstance().getMineManager().getMine(loc) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return;
        }

        UUID uuid = player.getUniqueId();
        AreaSelection sel = selectionMap.get(uuid);
        if (sel != null) {
            sel = isPos1 ? new AreaSelection(loc, sel.pos2) : new AreaSelection(sel.pos1, loc);
        } else {
            sel = isPos1 ? new AreaSelection(loc, null) : new AreaSelection(null, loc);
        }

        selectionMap.put(uuid, sel);
    }

    public static void handlePos(Player player, Location pos1, Location pos2) {
        if (SuperMines.getInstance().getMineManager().getMine(pos1) != null || SuperMines.getInstance().getMineManager().getMine(pos2) != null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        selectionMap.put(uuid, new AreaSelection(pos1, pos2));
    }

    public void register() {
        new CommandAPICommand("supermines")
                .withAliases("sm", "mine", "mines")
                .executes((CommandExecutor) (sender, args) -> showHelp(sender))
                .withSubcommand(
                        new CommandAPICommand("treasures")
                                .withPermission(Constants.Permission.TREASURES)
                                .executes((sender, args) -> {
                                    SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.treasures");
                                })
                                .withSubcommands(
                                        new CommandAPICommand("create")
                                                .withPermission(Constants.Permission.TREASURES)
                                                .withArguments(new StringArgument("id"), new StringArgument("displayName"))
                                                .executesPlayer((player, args) -> {

                                                })
                                )
                )
                .withSubcommand(
                        new CommandAPICommand("ranks")
                                .withPermission(Constants.Permission.RANKS)
                                .executes((sender, args) -> {
                                    SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.ranks");
                                })
                                .withSubcommands(
                                        new CommandAPICommand("create")
                                                .withPermission(Constants.Permission.RANKS)
                                                .withArguments(
                                                        new StringArgument("rankId"),
                                                        new StringArgument("displayName"),
                                                        new IntegerArgument("level", 1, Integer.MAX_VALUE)
                                                )
                                                .executesPlayer((player, args) -> {
                                                    String id = (String) args.get("rankId");
                                                    String displayName = (String) args.get("displayName");
                                                    int level = (int) args.get("level");

                                                    if (!id.matches(Constants.StringsAndComponents.ID_PATTERN)) {
                                                        SuperMines.getInstance().getLanguageManager().sendMessages(player, "command.invalid-id");
                                                        return;
                                                    }

                                                    if (SuperMines.getInstance().getRankManager().getRank(id) != null) {
                                                        SuperMines.getInstance().getLanguageManager().sendMessages(player, "command.ranks.create.exists");
                                                        return;
                                                    }

                                                    Rank rank = new Rank(level, id, ComponentUtils.deserialize(displayName));
                                                    SuperMines.getInstance().getRankManager().addRank(rank);
                                                    SuperMines.getInstance().getLanguageManager().sendMessages(player, "command.ranks.create.success", MessageReplacement.replace("%rank%", rank.getId()));
                                                }),
                                        new CommandAPICommand("remove")
                                                .withPermission(Constants.Permission.RANKS)
                                                .withArguments(new StringArgument("rankId")
                                                        .includeSuggestions(ArgumentSuggestions.strings(getRanksList())))
                                                .executesPlayer((player, args) -> {
                                                    String id = (String) args.get("rankId");
                                                    if (SuperMines.getInstance().getRankManager().getRank(id) == null) {
                                                        SuperMines.getInstance().getLanguageManager().sendMessages(player, "command.ranks.not-exists");
                                                        return;
                                                    }

                                                    SuperMines.getInstance().getRankManager().removeRank(id);
                                                    SuperMines.getInstance().getLanguageManager().sendMessages(player, "command.ranks.remove.success", MessageReplacement.replace("%rank%", id));
                                                })
                                )
                )
                .withSubcommand(
                        new CommandAPICommand("pos1")
                                .withPermission(Constants.Permission.POS_SET)
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> handlePos(player, true, null))
                                .withOptionalArguments(new LocationArgument("loc"))
                                .executesPlayer((player, args) -> {
                                    Location loc = (Location) args.get("loc");
                                    handlePos(player, true, loc);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("pos2")
                                .withPermission(Constants.Permission.POS_SET)
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> handlePos(player, false, null))
                                .withOptionalArguments(new LocationArgument("loc"))
                                .executesPlayer((player, args) -> {
                                    Location loc = (Location) args.get("loc");
                                    handlePos(player, false, loc);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("create")
                                .withPermission(Constants.Permission.CREATE)
                                .withArguments(new StringArgument("id"))
                                .withOptionalArguments(new StringArgument("displayName"))
                                .executesPlayer((player, args) -> {
                                    String id = (String) args.get("id");
                                    String displayName = args.get("displayName") == null ? id : (String) args.get("displayName");
                                    createMine(player, id, displayName);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("redefine")
                                .withPermission(Constants.Permission.REDEFINE)
                                .withArguments(
                                        new StringArgument("id")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> redefineMine(player, (String) args.get("id")))
                )
                .withSubcommand(
                        new CommandAPICommand("remove")
                                .withPermission(Constants.Permission.REMOVE)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                                .executes((CommandExecutor) (sender, args) -> removeMine(sender, (String) args.get("mineId")))
                )
                .withSubcommand(
                        new CommandAPICommand("reset")
                                .withPermission(Constants.Permission.RESET)
                                .withArguments(
                                        new StringArgument("id")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                                .executes((CommandExecutor) (sender, args) -> resetMine(sender, (String) args.get("id")))
                )
                .withSubcommand(
                        new CommandAPICommand("list")
                                .withPermission(Constants.Permission.LIST)
                                .executes((CommandExecutor) (sender, args) -> listMines(sender))
                )
                .withSubcommand(
                        new CommandAPICommand("gui")
                                .withPermission(Constants.Permission.GUI)
                                .executesPlayer((player, args) -> {
                                    GuiManager.openGeneral(player);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("addTreasure")
                                .withPermission(Constants.Permission.TREASURES)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                        new StringArgument("treasureId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                                .executes((sender, args) -> {
                                    String mineId = (String) args.get("mineId");
                                    String treasureId = (String) args.get("treasureId");
                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    Treasure treasure = SuperMines.getInstance().getTreasureManager().getTreasure(treasureId);
                                    if (treasure == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.treasure-not-exists");
                                        return;
                                    }

                                    mine.addTreasure(treasure);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("removeTreasure")
                                .withPermission(Constants.Permission.TREASURES)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                        new StringArgument("treasureId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                                .executes((sender, args) -> {
                                    String mineId = (String) args.get("mineId");
                                    String treasureId = (String) args.get("treasureId");
                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    Treasure treasure = SuperMines.getInstance().getTreasureManager().getTreasure(treasureId);
                                    if (treasure == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.treasure-not-exists");
                                        return;
                                    }

                                    mine.removeTreasure(treasure);
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("setRequiredLevel")
                                .withPermission(Constants.Permission.RANKS)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                        new IntegerArgument("level", 1, Integer.MAX_VALUE)
                                )
                                .executes((sender, args) -> {
                                    String mineId = (String) args.get("mineId");
                                    int level = (int) args.get("level");
                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    mine.setRequiredRankLevel(level);
                                    SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.set-required-level",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%level%", String.valueOf(level))
                                    );
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("setBlockGenerate")
                                .withPermission(Constants.Permission.BLOCK_GENERATE)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                        new ItemStackArgument("block"),
                                        new DoubleArgument("chance", 1, 100)
                                )
                                .executes((sender, args) -> {
                                    ItemStack block = (ItemStack) args.get("block");
                                    String mineId = (String) args.get("mineId");
                                    double chance = (double) args.get("chance");
                                    Material mat = block.getType();

                                    if (!mat.isBlock() || mat.isAir()) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.block-generate.invalid-material");
                                        return;
                                    }

                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    double rest = mine.calculateRestChance();
                                    double wasSet = mine.getBlockSpawnEntries().getOrDefault(mat, 0d);
                                    if (wasSet > 0) {
                                        if (chance > wasSet && (chance - wasSet > rest)) {
                                            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.block-generate.chance-too-high");
                                            return;
                                        }
                                    } else {
                                        if (chance > rest) {
                                            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.block-generate.chance-too-high");
                                            return;
                                        }
                                    }

                                    mine.addBlockSpawnEntry(mat, chance);
                                    SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.block-generate.success",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%material%", mat.toString()),
                                            MessageReplacement.replace("%chance%", String.valueOf(chance))
                                    );
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("removeBlockGenerate")
                                .withPermission(Constants.Permission.BLOCK_GENERATE)
                                .withArguments(
                                        new StringArgument("mineId"),
                                        new ItemStackArgument("block")
                                )
                                .executes((sender, args) -> {
                                    ItemStack block = (ItemStack) args.get("block");
                                    String mineId = (String) args.get("mineId");
                                    Material mat = block.getType();
                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    mine.removeBlockSpawnEntry(mat);
                                    SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.block-generate.removed",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName())
                                    );
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("setDisplayName")
                                .withPermission(Constants.Permission.SET_DISPLAY_NAME)
                                .withArguments(
                                        new StringArgument("mineId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                        new StringArgument("displayName")
                                )
                                .executes((sender, args) -> {
                                    String mineId = (String) args.get("mineId");
                                    Mine mine = SuperMines.getInstance().getMineManager().getMine(mineId);
                                    if (mine == null) {
                                        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
                                        return;
                                    }

                                    mine.setDisplayName(ComponentUtils.deserialize((String) args.get("displayName")));
                                    SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.set-display-name",
                                            MessageReplacement.replace("%mine%", mine.getId()),
                                            MessageReplacement.replace("%displayName%", mine.getRawDisplayName())
                                    );
                                })
                )
                .register();
    }

    private void showHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.general");
    }

    private CuboidArea getSelectedArea(Player player, String id, boolean checkExists) {
        if (checkExists && SuperMines.getInstance().getMineManager().getMine(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.exists");
            return null;
        }

        if (!id.matches(Constants.StringsAndComponents.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.invalid-id");
            return null;
        }

        AreaSelection sel = selectionMap.get(player.getUniqueId());
        if (sel == null || sel.pos1 == null || sel.pos2 == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.selection-not-finished");
            return null;
        }

        if (sel.isAnyInMine()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return null;
        }

        return sel.toCuboidArea();
    }

    private void createMine(Player player, String id, String displayName) {
        CuboidArea ca = getSelectedArea(player, id, true);
        if (ca == null) return;
        Component name = displayName == null ? Component.text(id) : ComponentUtils.deserialize(displayName);
        Mine mine = new Mine(id, name, player.getWorld(), ca, new HashMap<>(), 3600, false);
        SuperMines.getInstance().getMineManager().addMine(mine);
        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.success");
    }

    private void redefineMine(Player player, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.mine-not-exists");
            return;
        }
        CuboidArea ca = getSelectedArea(player, id, false);
        if (ca == null) return;
        mine.setArea(ca);
        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.redefine.success");
    }

    private void removeMine(CommandSender sender, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
            return;
        }

        SuperMines.getInstance().getTaskMaker().cancelMineResetTask(mine);
        SuperMines.getInstance().getMineManager().removeMine(id);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.remove.success");
    }

    private void resetMine(CommandSender sender, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
            return;
        }
        SuperMines.getInstance().getTaskMaker().runMineResetTaskNow(mine);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.reset.success");
    }

    private void listMines(CommandSender sender) {
        Component head = SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.head.mine");
        String color = SuperMines.getInstance().getLanguageManager().getMsg(sender, "command.list.color");
        Component sep = SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.separator");
        Collection<Mine> mines = SuperMines.getInstance().getMineManager().getAllMines();
        Component msg = head;
        Mine[] minesArray = mines.toArray(new Mine[0]);
        for (int i = 0; i < mines.size(); i++) {
            Mine m = minesArray[i];
            msg = msg.append(ComponentUtils.deserialize(color + m.getRawDisplayName() + "(" + m.getId() + ")"));
            if (i < mines.size() - 1) msg = msg.append(sep);
        }
        sender.sendMessage(msg);
    }

    private List<String> getMineList() {
        return SuperMines.getInstance().getMineManager().getAllMineIds();
    }

    private List<String> getTreasuresList() {
        return SuperMines.getInstance().getTreasureManager().getAllTreasureIds();
    }

    private List<String> getRanksList() {
        return SuperMines.getInstance().getRankManager().getAllRankIds();
    }

    private record AreaSelection(Location pos1, Location pos2) {
        public CuboidArea toCuboidArea() {
            return CuboidArea.createFromLocation(pos1, pos2);
        }

        public boolean isAnyInMine() {
            return SuperMines.getInstance().getMineManager().getMine(pos1) != null || SuperMines.getInstance().getMineManager().getMine(pos2) != null;
        }
    }
}