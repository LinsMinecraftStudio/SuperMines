package io.github.lijinhong11.supermines.api.pos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a cuboid (rectangular) area defined by two block positions.
 */
public record CuboidArea(BlockPos pos1, BlockPos pos2) {
    /**
     * Creates a CuboidArea from two Bukkit Locations.
     *
     * @param loc1 the first location
     * @param loc2 the second location
     * @return a new CuboidArea
     */
    public static CuboidArea createFromLocation(Location loc1, Location loc2) {
        return new CuboidArea(BlockPos.fromLocation(loc1), BlockPos.fromLocation(loc2));
    }

    /**
     * Gets the minimum corner position of this cuboid area.
     *
     * @return the minimum BlockPos
     */
    public BlockPos getMin() {
        return new BlockPos(Math.min(pos1.x(), pos2.x()), Math.min(pos1.y(), pos2.y()), Math.min(pos1.z(), pos2.z()));
    }

    /**
     * Gets the maximum corner position of this cuboid area.
     *
     * @return the maximum BlockPos
     */
    public BlockPos getMax() {
        return new BlockPos(Math.max(pos1.x(), pos2.x()), Math.max(pos1.y(), pos2.y()), Math.max(pos1.z(), pos2.z()));
    }

    /**
     * Gets the center location of this cuboid area in the specified world.
     *
     * @param world the world for the location
     * @return the center location
     */
    public Location getCenterLocation(World world) {
        BlockPos min = getMin();
        BlockPos max = getMax();
        double centerX = (min.x() + max.x()) / 2.0 + 0.5;
        double centerZ = (min.z() + max.z()) / 2.0 + 0.5;
        return new Location(world, centerX, Math.max(pos1.y(), pos2.y()) + 1, centerZ);
    }

    /**
     * Expands this cuboid area by the specified amount in all directions.
     *
     * @param x the amount to expand in X direction
     * @return a new expanded CuboidArea
     */
    public CuboidArea expand(int x) {
        return expand(x, 0);
    }

    /**
     * Expands this cuboid area by the specified amounts in X and Y directions.
     *
     * @param x the amount to expand in X direction
     * @param y the amount to expand in Y direction
     * @return a new expanded CuboidArea
     */
    public CuboidArea expand(int x, int y) {
        return expand(x, y, 0);
    }

    /**
     * Expands this cuboid area by the specified amounts in all directions.
     *
     * @param x the amount to expand in X direction
     * @param y the amount to expand in Y direction
     * @param z the amount to expand in Z direction
     * @return a new expanded CuboidArea
     */
    public CuboidArea expand(int x, int y, int z) {
        return new CuboidArea(pos1.minus(x, y, z), pos2.plus(x, y, z));
    }

    /**
     * Checks if a block position is contained within this cuboid area.
     *
     * @param pos the position to check
     * @return true if the position is within the area, false otherwise
     */
    public boolean contains(BlockPos pos) {
        BlockPos min = getMin();
        BlockPos max = getMax();
        return pos.x() >= min.x()
                && pos.x() <= max.x()
                && pos.y() >= min.y()
                && pos.y() <= max.y()
                && pos.z() >= min.z()
                && pos.z() <= max.z();
    }

    /**
     * Gets the size of this cuboid area in the X direction.
     *
     * @return the X size (in blocks)
     */
    public int sizeX() {
        return Math.abs(pos1.x() - pos2.x()) + 1;
    }

    /**
     * Gets the size of this cuboid area in the Y direction.
     *
     * @return the Y size (in blocks)
     */
    public int sizeY() {
        return Math.abs(pos1.y() - pos2.y()) + 1;
    }

    /**
     * Gets the size of this cuboid area in the Z direction.
     *
     * @return the Z size (in blocks)
     */
    public int sizeZ() {
        return Math.abs(pos1.z() - pos2.z()) + 1;
    }

    /**
     * Gets the total number of blocks in this cuboid area.
     *
     * @return the total number of blocks
     */
    public int volume() {
        return sizeX() * sizeY() * sizeZ();
    }

    /**
     * Performs an action for each block position in this cuboid area.
     *
     * @param action the action to perform for each position
     */
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

    /**
     * Converts this cuboid area to a list of all block positions it contains.
     *
     * @return a list of all BlockPos in this area
     */
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
