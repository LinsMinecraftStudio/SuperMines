package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.gui.GuiManager;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SuperMinesCommand {

    private final Map<UUID, AreaSelection> selectionMap = new ConcurrentHashMap<>();

    public void register() {
        new CommandAPICommand("supermines")
                .withAliases("sm", "mine", "mines")
                .executes((CommandExecutor) (sender, args) -> showHelp(sender))
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
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> createMine(player, (String) args.get("id"), null))
                                .withArguments(new StringArgument("id"), new StringArgument("displayName"))
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> createMine(player, (String) args.get("id"), (String) args.get("displayName")))
                )
                .withSubcommand(
                        new CommandAPICommand("redefine")
                                .withPermission(Constants.Permission.REDEFINE)
                                .withArguments(new StringArgument("id")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList()))
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> redefineMine(player, (String) args.get("id"))))
                )
                .withSubcommand(
                        new CommandAPICommand("remove")
                                .withPermission(Constants.Permission.REMOVE)
                                .withArguments(new StringArgument("id")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList()))
                                .executes((CommandExecutor) (sender, args) -> removeMine(sender, (String) args.get("id"))))
                )
                .withSubcommand(
                        new CommandAPICommand("reset")
                                .withPermission(Constants.Permission.RESET)
                                .withArguments(new StringArgument("id")
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
                                .executesPlayer((PlayerCommandExecutor) (player, args) -> GuiManager.openGeneral(player))
                ).register();
    }

    private void showHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.general");
    }

    private void handlePos(Player player, boolean isPos1, Location forcedLoc) {
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

    private CuboidArea getSelectedArea(Player player, String id, boolean checkExists) {
        if (checkExists && SuperMines.getInstance().getMineManager().getMine(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.exists");
            return null;
        }
        if (!id.matches(Constants.StringsAndComponents.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.invalid-id");
            return null;
        }
        AreaSelection sel = selectionMap.get(player.getUniqueId());
        if (sel == null || sel.pos1 == null || sel.pos2 == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.selection-not-finished");
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
        SuperMines.getInstance().getTaskMaker().cancelMineResetWarningTasks(mine);
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
        Component head = SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.head");
        String color = SuperMines.getInstance().getLanguageManager().getMsg(sender, "command.list.color");
        Component sep = SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.separator");
        List<Mine> mines = SuperMines.getInstance().getMineManager().getAll();
        Component msg = head;
        for (int i = 0; i < mines.size(); i++) {
            Mine m = mines.get(i);
            msg = msg.append(ComponentUtils.deserialize(color + m.getRawDisplayName() + "(" + m.getId() + ")"));
            if (i < mines.size() - 1) msg = msg.append(sep);
        }
        sender.sendMessage(msg);
    }

    private List<String> getMineList() {
        return SuperMines.getInstance().getMineManager().getAll().stream().map(Mine::getId).toList();
    }

    private List<String> getTreasuresList() {
        return SuperMines.getInstance().getTreasureManager().getAll().stream().map(Treasure::getId).toList();
    }

    private record AreaSelection(Location pos1, Location pos2) {
        public CuboidArea toCuboidArea() {
            return CuboidArea.createFromLocation(pos1, pos2);
        }
    }
}