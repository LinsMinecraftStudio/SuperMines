package io.github.lijinhong11.supermines.api.pos;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record CuboidArea(BlockPos pos1, BlockPos pos2) {

    public static CuboidArea createByLocations(Location loc1, Location loc2) {
        return new CuboidArea(BlockPos.fromLocation(loc1), BlockPos.fromLocation(loc2));
    }

    public BlockPos getMin() {
        return new BlockPos(
                Math.min(pos1.x(), pos2.x()),
                Math.min(pos1.y(), pos2.y()),
                Math.min(pos1.z(), pos2.z())
        );
    }

    public BlockPos getMax() {
        return new BlockPos(
                Math.max(pos1.x(), pos2.x()),
                Math.max(pos1.y(), pos2.y()),
                Math.max(pos1.z(), pos2.z())
        );
    }

    public CuboidArea expand(int x) {
        return expand(x, 0);
    }

    public CuboidArea expand(int x, int y) {
        return expand(x, y, 0);
    }

    public CuboidArea expand(int x, int y, int z) {
        return new CuboidArea(
                pos1.minus(x, y, z),
                pos2.plus(x, y, z)
        );
    }

    public boolean contains(BlockPos pos) {
        BlockPos min = getMin();
        BlockPos max = getMax();
        return pos.x() >= min.x() && pos.x() <= max.x() &&
                pos.y() >= min.y() && pos.y() <= max.y() &&
                pos.z() >= min.z() && pos.z() <= max.z();
    }

    public int sizeX() {
        return Math.abs(pos1.x() - pos2.x()) + 1;
    }

    public int sizeY() {
        return Math.abs(pos1.y() - pos2.y()) + 1;
    }

    public int sizeZ() {
        return Math.abs(pos1.z() - pos2.z()) + 1;
    }

    public int volume() {
        return sizeX() * sizeY() * sizeZ();
    }

    public void forEach(Consumer<BlockPos> action) {
        BlockPos min = getMin();
        BlockPos max = getMax();
        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    action.accept(new BlockPos(x, y, z));
                }
            }
        }
    }

    public List<BlockPos> asPosList() {
        List<BlockPos> list = new ArrayList<>(volume());
        forEach(list::add);
        return list;
    }

    @Override
    public @NotNull String toString() {
        return "CuboidArea{" + getMin() + " -> " + getMax() + '}';
    }
}