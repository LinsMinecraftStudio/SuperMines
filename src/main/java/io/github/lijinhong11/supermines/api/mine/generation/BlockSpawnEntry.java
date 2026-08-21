package io.github.lijinhong11.supermines.api.mine.generation;

import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A weighted block that can be generated in a mine. Each entry holds a {@link PackedBlock} and an
 * optional set of {@link IGenerateCondition}s that decide where the block is allowed to appear.
 */
public class BlockSpawnEntry implements PackedBlock {
    private final PackedBlock block;
    private final Set<IGenerateCondition> conditions;

    public BlockSpawnEntry(@NotNull PackedBlock block) {
        this(block, new HashSet<>());
    }

    public BlockSpawnEntry(@NotNull PackedBlock block, @NotNull Collection<IGenerateCondition> conditions) {
        this.block = block;
        this.conditions = new HashSet<>(conditions);
    }

    @Override
    public void place(@NotNull Location location) {
        block.place(location);
    }

    @Override
    public @NotNull String getId() {
        return block.getId();
    }

    @Override
    public ItemStack toItem() {
        return block.toItem();
    }

    /**
     * Gets the underlying block of this spawn entry.
     *
     * @return the block
     */
    public PackedBlock getBlock() {
        return block;
    }

    /**
     * Gets all generation conditions of this spawn entry.
     *
     * @return a set of generation conditions
     */
    public Set<IGenerateCondition> getConditions() {
        return conditions;
    }

    /**
     * Adds a generation condition to this spawn entry.
     *
     * @param condition the condition to add
     */
    public void addGenerateCondition(@NotNull IGenerateCondition condition) {
        conditions.add(condition);
    }

    /**
     * Removes a generation condition from this spawn entry.
     *
     * @param condition the condition to remove
     */
    public void removeGenerateCondition(@NotNull IGenerateCondition condition) {
        conditions.remove(condition);
    }

    /**
     * Sets all generation conditions of this spawn entry, replacing any existing ones.
     *
     * @param conditions the collection of conditions to set
     */
    public void setGenerateConditions(@NotNull Collection<IGenerateCondition> conditions) {
        this.conditions.clear();
        this.conditions.addAll(conditions);
    }

    /**
     * Checks whether this entry is allowed to generate at the given position in a mine.
     *
     * @param mine the mine being generated
     * @param target the target block position
     * @return true if the block can be generated, false otherwise
     */
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        for (IGenerateCondition condition : conditions) {
            if (!condition.canGenerate(mine, target)) {
                return false;
            }
        }

        return true;
    }
}
