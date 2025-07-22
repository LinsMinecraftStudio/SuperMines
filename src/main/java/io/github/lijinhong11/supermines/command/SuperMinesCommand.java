package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.ALocationArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.gui.GuiManager;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Command("supermines")
@Alias({"sm", "mine", "mines"})
public class SuperMinesCommand {
    private final Map<UUID, AreaSelection> selectionMap = new ConcurrentHashMap<>();

    @Default
    public void showHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.general");
    }

    @Subcommand("treasure")
    public void showTreasureHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.treasure");
    }

    @Subcommand("rank")
    public void showRankHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.rank");
    }

    @Subcommand("pos1")
    @Permission(Constants.Permission.POS_SET)
    public void setPos1(Player player) {
        if (SuperMines.getInstance().getMineManager().getMine(player.getLocation()) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return;
        }

        if (selectionMap.get(player.getUniqueId()) != null) {
            AreaSelection selectionBefore = selectionMap.get(player.getUniqueId());
            AreaSelection newAfter = new AreaSelection(player.getLocation(), selectionBefore.pos2);

            selectionMap.put(player.getUniqueId(), newAfter);
        } else {
            AreaSelection newAfter = new AreaSelection(player.getLocation(), null);
            selectionMap.put(player.getUniqueId(), newAfter);
        }
    }

    @Subcommand("pos1")
    @Permission(Constants.Permission.POS_SET)
    public void setPos1(Player player, @ALocationArgument Location loc) {
        if (SuperMines.getInstance().getMineManager().getMine(player.getLocation()) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return;
        }

        if (selectionMap.get(player.getUniqueId()) != null) {
            AreaSelection selectionBefore = selectionMap.get(player.getUniqueId());
            AreaSelection newAfter = new AreaSelection(loc, selectionBefore.pos2);

            selectionMap.put(player.getUniqueId(), newAfter);
        } else {
            AreaSelection newAfter = new AreaSelection(loc, null);
            selectionMap.put(player.getUniqueId(), newAfter);
        }
    }

    @Subcommand("pos2")
    @Permission(Constants.Permission.POS_SET)
    public void setPos2(Player player) {
        if (SuperMines.getInstance().getMineManager().getMine(player.getLocation()) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return;
        }

        if (selectionMap.get(player.getUniqueId()) != null) {
            AreaSelection selectionBefore = selectionMap.get(player.getUniqueId());
            AreaSelection newAfter = new AreaSelection(selectionBefore.pos1, player.getLocation());

            selectionMap.put(player.getUniqueId(), newAfter);
        } else {
            AreaSelection newAfter = new AreaSelection(null, player.getLocation());
            selectionMap.put(player.getUniqueId(), newAfter);
        }
    }

    @Subcommand("pos2")
    @Permission(Constants.Permission.POS_SET)
    public void setPos2(Player player, @ALocationArgument Location loc) {
        if (SuperMines.getInstance().getMineManager().getMine(player.getLocation()) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return;
        }

        if (selectionMap.get(player.getUniqueId()) != null) {
            AreaSelection selectionBefore = selectionMap.get(player.getUniqueId());
            AreaSelection newAfter = new AreaSelection(selectionBefore.pos1, loc);

            selectionMap.put(player.getUniqueId(), newAfter);
        } else {
            AreaSelection newAfter = new AreaSelection(null, loc);
            selectionMap.put(player.getUniqueId(), newAfter);
        }
    }

    @Subcommand("create")
    @Permission(Constants.Permission.CREATE)
    public void createMine(Player player, @AStringArgument String id) {
        CuboidArea ca = getAreaSelection(player, id, true);
        if (ca == null) {
            return;
        }

        Mine mine = new Mine(id, Component.text(id), player.getWorld(), ca, new HashMap<>(), 3600, false);
        SuperMines.getInstance().getMineManager().addMine(mine);

        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.success");
    }

    @Subcommand("create")
    @Permission(Constants.Permission.CREATE)
    public void createMine(Player player, @AStringArgument String id, @AStringArgument String displayName) {
        CuboidArea ca = getAreaSelection(player, id, true);
        if (ca == null) {
            return;
        }

        Mine mine = new Mine(id, ComponentUtils.deserialize(displayName), player.getWorld(), ca, new HashMap<>(), 3600, false);
        SuperMines.getInstance().getMineManager().addMine(mine);

        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.success");
    }

    @Subcommand("redefine")
    @Permission(Constants.Permission.REDEFINE)
    public void redefineMine(Player player, @AStringArgument String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.mine-not-exists");
            return;
        }

        CuboidArea ca = getAreaSelection(player, id, false);
        if (ca == null) {
            return;
        }

        mine.setArea(ca);
        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.redefine.success");
    }

    @Subcommand("remove")
    @Permission(Constants.Permission.REMOVE)
    public void removeMine(Player player, @AStringArgument String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.mine-not-exists");
            return;
        }

        SuperMines.getInstance().getTaskMaker().cancelMineResetWarningTasks(mine);
        SuperMines.getInstance().getTaskMaker().cancelMineResetTask(mine);
        SuperMines.getInstance().getMineManager().remove(id);

        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.remove.success");
    }

    @Subcommand("gui")
    @Permission(Constants.Permission.GUI)
    public void openGui(Player player) {
        GuiManager.openGeneral(player);
    }

    private CuboidArea getAreaSelection(Player player, String id, boolean checkExisting) {
        if (checkExisting) {
            if (SuperMines.getInstance().getMineManager().getMine(id) != null) {
                SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.exists");
                return null;
            }

            if (!id.matches(Constants.StringsAndComponents.ID_PATTERN)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.invalid-id");
                return null;
            }
        }

        if (selectionMap.get(player.getUniqueId()) == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.no-selection");
            return null;
        }

        AreaSelection selection = selectionMap.get(player.getUniqueId());
        if (selection.pos1 == null || selection.pos2 == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.selection-not-finished");
            return null;
        }

        return selection.toCuboidArea();
    }

    private record AreaSelection(Location pos1, Location pos2) {
        public CuboidArea toCuboidArea() {
            return CuboidArea.createFromLocation(pos1, pos2);
        }
    }
}
