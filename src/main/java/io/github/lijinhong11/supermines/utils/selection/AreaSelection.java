package io.github.lijinhong11.supermines.utils.selection;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import org.bukkit.Location;

public record AreaSelection(Location pos1, Location pos2) {
    public CuboidArea toCuboidArea() {
        return CuboidArea.createFromLocation(pos1, pos2);
    }

    public boolean isAnyMineIn() {
        return toCuboidArea().asPosList().stream()
                .anyMatch(p -> SuperMines.getInstance().getMineManager().getMine(p.toLocation(pos1.getWorld())) != null);
    }
}
