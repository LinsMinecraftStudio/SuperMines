package io.github.lijinhong11.supermines.api.pos;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Represents a block position in 3D space with integer coordinates.
 */
public record BlockPos(int x, int y, int z) implements Comparable<BlockPos> {

    /**
     * Creates a BlockPos from a Bukkit Location.
     *
     * @param location the location to convert
     * @return a new BlockPos with the location's block coordinates
     */
    public static BlockPos fromLocation(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Converts this BlockPos to a Bukkit Location in the specified world.
     *
     * @param world the world for the location
     * @return a new Location at this block position
     */
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    /**
     * Adds the specified offsets to this position.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new BlockPos with the added coordinates
     */
    public BlockPos plus(int x, int y, int z) {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds another BlockPos to this position.
     *
     * @param pos the position to add
     * @return a new BlockPos with the added coordinates
     */
    public BlockPos plus(BlockPos pos) {
        return new BlockPos(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    /**
     * Subtracts the specified offsets from this position.
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return a new BlockPos with the subtracted coordinates
     */
    public BlockPos minus(int x, int y, int z) {
        return new BlockPos(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtracts another BlockPos from this position.
     *
     * @param pos the position to subtract
     * @return a new BlockPos with the subtracted coordinates
     */
    public BlockPos minus(BlockPos pos) {
        return new BlockPos(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    /**
     * Calculates the squared distance to another BlockPos.
     *
     * @param other the other position
     * @return the squared distance
     */
    public int distanceSquared(BlockPos other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the Manhattan distance (sum of absolute differences) to another
     * BlockPos.
     *
     * @param other the other position
     * @return the Manhattan distance
     */
    public int distanceManhattan(BlockPos other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    /**
     * Returns a BlockPos with the minimum coordinates of this and another position.
     *
     * @param other the other position
     * @return a new BlockPos with minimum coordinates
     */
    public BlockPos min(BlockPos other) {
        return new BlockPos(Math.min(this.x, other.x), Math.min(this.y, other.y), Math.min(this.z, other.z));
    }

    /**
     * Returns a BlockPos with the maximum coordinates of this and another position.
     *
     * @param other the other position
     * @return a new BlockPos with maximum coordinates
     */
    public BlockPos max(BlockPos other) {
        return new BlockPos(Math.max(this.x, other.x), Math.max(this.y, other.y), Math.max(this.z, other.z));
    }

    /**
     * Converts this BlockPos to an unmodifiable map with keys "x", "y", "z".
     *
     * @return a map containing the coordinates
     */
    public @Unmodifiable Map<String, Integer> toMap() {
        return Map.of("x", x, "y", y, "z", z);
    }

    @Override
    public int compareTo(BlockPos other) {
        int cmpX = Integer.compare(this.x, other.x);
        if (cmpX != 0)
            return cmpX;

        int cmpY = Integer.compare(this.y, other.y);
        if (cmpY != 0)
            return cmpY;

        return Integer.compare(this.z, other.z);
    }

    @Override
    public @NotNull String toString() {
        return x + ", " + y + ", " + z;
    }
}
