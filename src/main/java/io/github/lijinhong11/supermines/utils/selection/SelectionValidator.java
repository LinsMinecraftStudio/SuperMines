package io.github.lijinhong11.supermines.utils.selection;

import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

public final class SelectionValidator {

    private SelectionValidator() {
    }

    public static boolean validateSingleLocation(Player player, Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return false;
        }

        World world = loc.getWorld();

        int y = loc.getBlockY();
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(player, "command.pos.invalid-height");
            return false;
        }

        WorldBorder border = world.getWorldBorder();
        if (!border.isInside(loc)) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(player, "command.pos.outside-border");
            return false;
        }

        return true;
    }

    public static boolean validateWorldConsistency(Player player, AreaSelection sel, Location newLoc) {
        if (sel == null) return true;

        Location other = sel.pos1() != null ? sel.pos1() : sel.pos2();
        if (other == null) return true;

        if (!other.getWorld().equals(newLoc.getWorld())) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(player, "command.pos.different-world");
            return false;
        }

        return true;
    }

    public static boolean validateAll(
            Player player,
            AreaSelection current,
            Location newLoc
    ) {
        return validateSingleLocation(player, newLoc)
                && validateWorldConsistency(player, current, newLoc);
    }
}
