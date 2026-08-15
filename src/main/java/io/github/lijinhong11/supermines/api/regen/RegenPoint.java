package io.github.lijinhong11.supermines.api.regen;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.api.iface.Identified;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single regenerable block that respawns on a delay after being mined.
 *
 * <p>Regen points are a global, standalone system fully separated from the mine
 * reset logic: they can exist at any location in any world and regenerate
 * themselves independently.
 */
public final class RegenPoint implements Identified {
    private final String id;
    private final World world;
    private final BlockPos pos;
    private final WeightedRandomMap<PackedBlock> blocks = new WeightedRandomMap<>();
    private final Map<String, Double> rewardChances = new HashMap<>();
    private int respawnSeconds;
    private Component displayName;
    private long respawnAt;

    /**
     * Creates a new regen point.
     *
     * @param id             the unique id
     * @param world          the world of the point
     * @param pos            the block position of the point
     * @param block          the block that will respawn
     * @param respawnSeconds the delay before the block respawns, or 0 to disable automatic respawning
     */
    public RegenPoint(
            @NotNull String id,
            @NotNull World world,
            @NotNull BlockPos pos,
            @NotNull PackedBlock block,
            int respawnSeconds) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkNotNull(pos, "pos");
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkArgument(respawnSeconds >= 0, "respawnSeconds cannot be negative");

        this.id = id;
        this.world = world;
        this.pos = pos;
        this.blocks.put(block, 1D);
        this.respawnSeconds = respawnSeconds;
    }

    /**
     * Gets the unique id of this regen point.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the world of this regen point.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the block position of this regen point.
     *
     * @return the position
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * Gets the location of this regen point.
     *
     * @return the location
     */
    public Location getLocation() {
        return pos.toLocation(world);
    }

    /**
     * Gets the block that respawns at this regen point.
     *
     * @return the block
     */
    public PackedBlock getBlock() {
        return blocks.keySet().iterator().next();
    }

    /**
     * Sets the block that respawns at this regen point.
     *
     * @param block the block (cannot be null)
     */
    public void setBlock(@NotNull PackedBlock block) {
        Preconditions.checkNotNull(block, "block cannot be null");

        blocks.clear();
        blocks.put(block, 1D);
    }

    public PackedBlock selectBlock() {
        return blocks.randomOne();
    }

    public WeightedRandomMap<PackedBlock> getBlocks() {
        return new WeightedRandomMap<>(blocks);
    }

    public void replaceBlocks(@NotNull WeightedRandomMap<PackedBlock> updated) {
        Preconditions.checkNotNull(updated, "blocks cannot be null");
        Preconditions.checkArgument(!updated.isEmpty(), "a regen point must contain at least one block");
        updated.forEach((block, weight) -> Preconditions.checkArgument(weight > 0, "weight must be greater than 0"));
        blocks.clear();
        blocks.putAll(updated);
    }

    public void addBlock(@NotNull PackedBlock block, double weight) {
        Preconditions.checkNotNull(block, "block cannot be null");
        Preconditions.checkArgument(weight > 0, "weight must be greater than 0");

        blocks.put(block, weight);
    }

    public void removeBlock(@NotNull PackedBlock block) {
        Preconditions.checkArgument(blocks.size() > 1, "a regen point must contain at least one block");

        blocks.remove(block);
    }

    public Map<String, Double> getRewardChances() {
        return Collections.unmodifiableMap(new HashMap<>(rewardChances));
    }

    public void setRewardChance(@NotNull String treasureId, double chance) {
        Preconditions.checkNotNull(treasureId, "treasureId cannot be null");
        Preconditions.checkArgument(chance > 0 && chance <= 100, "chance must be greater than 0 and at most 100");

        rewardChances.put(treasureId, chance);
    }

    public void removeReward(@NotNull String treasureId) {
        rewardChances.remove(treasureId);
    }

    /**
     * Gets the delay before the block respawns.
     *
     * @return the respawn seconds
     */
    public int getRespawnSeconds() {
        return respawnSeconds;
    }

    /**
     * Sets the delay before the block respawns.
     *
     * @param respawnSeconds the respawn seconds, or 0 to disable automatic respawning
     */
    public void setRespawnSeconds(int respawnSeconds) {
        Preconditions.checkArgument(respawnSeconds >= 0, "respawnSeconds cannot be negative");
        this.respawnSeconds = respawnSeconds;
    }

    /**
     * Gets the persisted respawn deadline.
     *
     * @return the epoch-millisecond deadline, or 0 when the point is ready
     */
    public long getRespawnAt() {
        return respawnAt;
    }

    /**
     * Sets the persisted respawn deadline.
     *
     * @param respawnAt the epoch-millisecond deadline, or 0 when ready
     */
    public void setRespawnAt(long respawnAt) {
        Preconditions.checkArgument(respawnAt >= 0, "respawnAt cannot be negative");

        this.respawnAt = respawnAt;
    }

    /**
     * Gets the raw display name of this regen point as a string.
     *
     * @return the serialized display name
     */
    public String getRawDisplayName() {
        if (displayName == null) {
            return id;
        }

        return ComponentUtils.serialize(displayName);
    }

    /**
     * Gets the display name of this regen point.
     *
     * @return the display name component
     */
    public Component getDisplayName() {
        return displayName == null ? ComponentUtils.text(id) : displayName;
    }

    /**
     * Sets the display name of this regen point.
     *
     * @param displayName the display name component (cannot be null)
     */
    public void setDisplayName(@Nullable Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }
}
