package io.github.lijinhong11.supermines.utils.selection;

import io.github.lijinhong11.mittellib.math.AreaOfBlocks;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.math.SphereArea;
import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record AreaSelection(Location pos1, Location pos2, boolean sphere) {
    public CuboidArea toCuboidArea() {
        return CuboidArea.createFromLocation(pos1, pos2);
    }

    public SphereArea toSphereArea() {
        BlockPos center = BlockPos.fromLocation(pos1);
        int radius = (int) Math.round(pos1.distance(pos2));
        return new SphereArea(center, radius);
    }

    public AreaOfBlocks toArea() {
        return sphere ? toSphereArea() : toCuboidArea();
    }

    public boolean isAnyMineIn() {
        return toArea().asPosList().stream()
                .anyMatch(
                        p -> SuperMines.getInstance().getMineManager().getMine(p.toLocation(pos1.getWorld())) != null);
    }
}
