package io.github.lijinhong11.supermines.api.pos;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

public record BlockPos(int x, int y, int z) implements Comparable<BlockPos> {

    public static BlockPos fromLocation(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }

    public BlockPos plus(int x, int y, int z) {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos plus(BlockPos pos) {
        return new BlockPos(this.x + pos.x, this.y + pos.y, this.z + pos.z);
    }

    public BlockPos minus(int x, int y, int z) {
        return new BlockPos(this.x - x, this.y - y, this.z - z);
    }

    public BlockPos minus(BlockPos pos) {
        return new BlockPos(this.x - pos.x, this.y - pos.y, this.z - pos.z);
    }

    public int distanceSquared(BlockPos other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        int dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public int distanceManhattan(BlockPos other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y) + Math.abs(this.z - other.z);
    }

    public BlockPos min(BlockPos other) {
        return new BlockPos(
                Math.min(this.x, other.x),
                Math.min(this.y, other.y),
                Math.min(this.z, other.z)
        );
    }

    public BlockPos max(BlockPos other) {
        return new BlockPos(
                Math.max(this.x, other.x),
                Math.max(this.y, other.y),
                Math.max(this.z, other.z)
        );
    }

    public @Unmodifiable Map<String, Integer> toMap() {
        return Map.of("x", x, "y", y, "z", z);
    }

    @Override
    public int compareTo(BlockPos other) {
        int cmpX = Integer.compare(this.x, other.x);
        if (cmpX != 0) return cmpX;

        int cmpY = Integer.compare(this.y, other.y);
        if (cmpY != 0) return cmpY;

        return Integer.compare(this.z, other.z);
    }

    @Override
    public @NotNull String toString() {
        return x + ", " + y + ", " + z;
    }
}