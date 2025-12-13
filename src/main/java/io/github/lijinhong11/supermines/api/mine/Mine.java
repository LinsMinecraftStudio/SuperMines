package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import io.github.lijinhong11.supermines.integrates.block.MinecraftBlockAddon;
import io.github.lijinhong11.supermines.managers.database.StringRankSet;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.ParametersAreNonnullByDefault;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * The mine object.
 */
public final class Mine implements Identified {
    private final String id;

    private final World world;
    private final Map<AddonBlock, Double> blockSpawnEntries;

    private final List<Treasure> treasures;
    private final Set<String> allowedRankIds;

    private final Set<Integer> warningSeconds;

    private final AtomicInteger blocksBroken = new AtomicInteger(0);

    private Material displayIcon;
    private Component displayName;
    private CuboidArea area;
    private int regenerateSeconds;
    private boolean onlyFillAirWhenRegenerate;
    private int requiredRankLevel;
    private Location tpLoc;

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            World world,
            CuboidArea area,
            Map<AddonBlock, Double> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate) {
        this(
                id,
                displayName,
                Constants.Items.DEFAULT_MINE_ICON,
                world,
                area,
                blockSpawnEntries,
                regenerateSeconds,
                onlyFillAirWhenRegenerate);
    }

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            Material displayIcon,
            World world,
            CuboidArea area,
            Map<AddonBlock, Double> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate) {
        this(
                id,
                displayName,
                displayIcon,
                world,
                area,
                blockSpawnEntries,
                regenerateSeconds,
                onlyFillAirWhenRegenerate,
                new ArrayList<>(),
                new HashSet<>());
    }

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            Material displayIcon,
            World world,
            CuboidArea area,
            Map<AddonBlock, Double> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate,
            List<Treasure> treasures,
            Set<String> allowedRankIds) {
        this(
                id,
                displayName,
                displayIcon,
                world,
                area,
                blockSpawnEntries,
                regenerateSeconds,
                onlyFillAirWhenRegenerate,
                treasures,
                Rank.DEFAULT.getLevel(),
                allowedRankIds);
    }

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            Material displayIcon,
            World world,
            CuboidArea area,
            Map<AddonBlock, Double> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate,
            List<Treasure> treasures,
            int requiredRankLevel,
            Set<String> allowedRankIds) {
        this(
                id,
                displayName,
                displayIcon,
                world,
                area,
                blockSpawnEntries,
                regenerateSeconds,
                onlyFillAirWhenRegenerate,
                treasures,
                requiredRankLevel,
                allowedRankIds,
                null,
                new HashSet<>());
    }

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            Material displayIcon,
            World world,
            CuboidArea area,
            Map<AddonBlock, Double> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate,
            List<Treasure> treasures,
            int requiredRankLevel,
            Set<String> allowedRankIds,
            @Nullable Location tpLoc,
            Set<Integer> warningSeconds) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");
        Preconditions.checkArgument(
                id.matches(Constants.StringsAndComponents.ID_PATTERN), "Mine ID cannot contain special characters");

        this.id = id;
        this.displayName = displayName;
        this.displayIcon = displayIcon;
        this.world = world;
        this.area = area;
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
        this.blockSpawnEntries = blockSpawnEntries;
        this.regenerateSeconds = regenerateSeconds;
        this.treasures = treasures;
        this.requiredRankLevel = requiredRankLevel;
        this.allowedRankIds = allowedRankIds;
        this.warningSeconds = warningSeconds;
        this.tpLoc = tpLoc == null ? area.getCenterLocation(world) : tpLoc;
    }

    /**
     * Checks if a player is currently inside this mine.
     *
     * @param player the player to check
     * @return true if the player is inside the mine, false otherwise
     */
    public boolean isPlayerInMine(@NotNull Player player) {
        return area.contains(BlockPos.fromLocation(player.getLocation()));
    }

    /**
     * Adds a treasure to this mine by its ID.
     *
     * @param id the treasure ID to add
     */
    public void addTreasure(@NotNull String id) {
        treasures.add(SuperMinesAPI.getTreasure(id));
    }

    /**
     * Adds a treasure to this mine.
     *
     * @param treasure the treasure to add
     */
    public void addTreasure(@NotNull Treasure treasure) {
        treasures.add(treasure);
    }

    /**
     * Removes a treasure from this mine by its ID.
     *
     * @param id the treasure ID to remove
     */
    public void removeTreasure(@NotNull String id) {
        treasures.removeIf(treasure -> treasure.getId().equals(id));
    }

    /**
     * Removes a treasure from this mine.
     *
     * @param treasure the treasure to remove
     */
    public void removeTreasure(@NotNull Treasure treasure) {
        treasures.remove(treasure);
    }

    /**
     * Sets all treasures for this mine, replacing any existing ones.
     *
     * @param treasures the list of treasures to set
     */
    public void setTreasures(@NotNull List<Treasure> treasures) {
        this.treasures.clear();
        this.treasures.addAll(treasures);
    }

    /**
     * Adds a block spawn entry with the specified material and chance.
     *
     * @param material the material to spawn
     * @param chance the spawn chance (must be between 1 and 100)
     * @throws IllegalArgumentException if chance is not between 1 and 100
     */
    public void addBlockSpawnEntry(@NotNull Material material, double chance) {
        if (chance < 1 || chance > 100) {
            throw new IllegalArgumentException("Chance must be between 1 and 100");
        }

        blockSpawnEntries.put(MinecraftBlockAddon.createForMaterial(material), chance);
    }

    /**
     * Adds multiple block spawn entries to this mine.
     *
     * @param blockSpawnEntries the map of blocks to their spawn chances
     */
    public void addBlockSpawnEntries(Map<AddonBlock, Double> blockSpawnEntries) {
        this.blockSpawnEntries.putAll(blockSpawnEntries);
    }

    /**
     * Removes a block spawn entry by material.
     *
     * @param material the material to remove
     */
    public void removeBlockSpawnEntry(@NotNull Material material) {
        blockSpawnEntries.remove(MinecraftBlockAddon.createForMaterial(material));
    }

    /**
     * Removes a block spawn entry by addon block.
     *
     * @param block the addon block to remove
     */
    public void removeBlockSpawnEntry(@NotNull AddonBlock block) {
        blockSpawnEntries.remove(block);
    }

    /**
     * Removes multiple block spawn entries.
     *
     * @param blocks the list of blocks to remove
     */
    public void removeBlockSpawnEntries(List<AddonBlock> blocks) {
        blocks.forEach(blockSpawnEntries::remove);
    }

    /**
     * Adds an allowed rank ID to this mine.
     *
     * @param rankId the rank ID to add
     */
    public void addAllowedRankId(String rankId) {
        allowedRankIds.add(rankId);
    }

    /**
     * Removes an allowed rank ID from this mine.
     *
     * @param rankId the rank ID to remove
     */
    public void removeAllowedRankId(String rankId) {
        allowedRankIds.remove(rankId);
    }

    /**
     * Sets all allowed rank IDs for this mine, replacing any existing ones.
     *
     * @param rankIds the collection of rank IDs to set
     */
    public void setAllowedRankIds(Collection<String> rankIds) {
        allowedRankIds.clear();
        allowedRankIds.addAll(rankIds);
    }

    /**
     * Gets all allowed rank IDs for this mine.
     *
     * @return a set of allowed rank IDs
     */
    public Set<String> getAllowedRankIds() {
        return allowedRankIds;
    }

    /**
     * Sets the teleport location for this mine.
     *
     * @param tpLoc the teleport location (cannot be null)
     * @throws NullPointerException if tpLoc is null
     */
    public void setTeleportLocation(@NotNull Location tpLoc) {
        Preconditions.checkNotNull(tpLoc, "teleport location cannot be null");

        this.tpLoc = tpLoc;
    }

    /**
     * Gets the teleport location for this mine.
     *
     * @return the teleport location, or null if not set
     */
    public @Nullable Location getTeleportLocation() {
        return tpLoc;
    }

    /**
     * Gets the unique identifier of this mine.
     *
     * @return the mine ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the raw display name of this mine as a string.
     *
     * @return the serialized display name
     */
    public String getRawDisplayName() {
        return ComponentUtils.serialize(getDisplayName());
    }

    /**
     * Sets the display name of this mine.
     *
     * @param displayName the display name component (cannot be null)
     * @throws NullPointerException if displayName is null
     */
    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    /**
     * Gets the display name of this mine.
     *
     * @return the display name component, or a default name if not set
     */
    public Component getDisplayName() {
        return displayName == null ? Constants.StringsAndComponents.RESET.append(Component.text(id)) : displayName;
    }

    /**
     * Sets the display icon material for this mine.
     *
     * @param displayIcon the material to use as display icon (cannot be null)
     * @throws NullPointerException if displayIcon is null
     */
    public void setDisplayIcon(Material displayIcon) {
        Preconditions.checkNotNull(displayIcon, "display icon cannot be null");

        this.displayIcon = displayIcon;
    }

    /**
     * Gets the display icon material for this mine.
     *
     * @return the display icon material
     */
    public Material getDisplayIcon() {
        return displayIcon;
    }

    /**
     * Gets the world this mine is located in.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Sets the area of this mine.
     *
     * @param area the cuboid area (cannot be null)
     * @throws NullPointerException if area is null
     */
    public void setArea(@NotNull CuboidArea area) {
        Preconditions.checkNotNull(area, "area cannot be null");

        this.area = area;
    }

    /**
     * Gets the area of this mine.
     *
     * @return the cuboid area
     */
    public CuboidArea getArea() {
        return area;
    }

    /**
     * Gets all treasures associated with this mine.
     *
     * @return a list of treasures
     */
    public List<Treasure> getTreasures() {
        return treasures;
    }

    /**
     * Gets all block spawn entries for this mine.
     *
     * @return a map of blocks to their spawn chances
     */
    public Map<AddonBlock, Double> getBlockSpawnEntries() {
        return blockSpawnEntries;
    }

    /**
     * Sets the regeneration time in seconds for this mine.
     *
     * @param regenerateSeconds the regeneration time in seconds (must be >= 0)
     * @throws IllegalArgumentException if regenerateSeconds is negative
     */
    public void setRegenerateSeconds(@Range(from = 0, to = Integer.MAX_VALUE) int regenerateSeconds) {
        Preconditions.checkArgument(regenerateSeconds >= 0, "regenerate seconds must equal to or greater than 0");

        this.regenerateSeconds = regenerateSeconds;
    }

    /**
     * Gets the regeneration time in seconds for this mine.
     *
     * @return the regeneration time in seconds
     */
    public int getRegenerateSeconds() {
        return regenerateSeconds;
    }

    /**
     * Sets whether this mine should only fill air blocks when regenerating.
     *
     * @param onlyFillAirWhenRegenerate true to only fill air blocks, false to replace all blocks
     */
    public void setOnlyFillAirWhenRegenerate(boolean onlyFillAirWhenRegenerate) {
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
    }

    /**
     * Checks if this mine only fills air blocks when regenerating.
     *
     * @return true if only air blocks are filled, false otherwise
     */
    public boolean isOnlyFillAirWhenRegenerate() {
        return onlyFillAirWhenRegenerate;
    }

    /**
     * Sets the required rank level for this mine.
     *
     * @param requiredRankLevel the minimum rank level required
     */
    public void setRequiredRankLevel(int requiredRankLevel) {
        this.requiredRankLevel = requiredRankLevel;
    }

    /**
     * Sets the required rank level based on a rank object.
     *
     * @param requiredRank the rank to use for the required level
     */
    public void setRequiredRankLevel(Rank requiredRank) {
        this.requiredRankLevel = requiredRank.getLevel();
    }

    /**
     * Gets the required rank level for this mine.
     *
     * @return the minimum rank level required
     */
    public int getRequiredRankLevel() {
        return requiredRankLevel;
    }

    /**
     * Gets the number of blocks broken in this mine.
     *
     * @return the number of blocks broken
     */
    public int getBlocksBroken() {
        return this.blocksBroken.get();
    }

    /**
     * Sets the number of blocks broken in this mine.
     *
     * @param blocksBroken the number of blocks broken
     */
    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken.set(blocksBroken);
    }

    /**
     * Increments the number of blocks broken in this mine by 1.
     */
    public void plusBlocksBroken() {
        this.blocksBroken.incrementAndGet();
    }

    /**
     * Sets the warning seconds for reset notifications.
     *
     * @param warningSeconds the set of seconds before reset to show warnings
     */
    public void setWarningSeconds(Set<Integer> warningSeconds) {
        this.warningSeconds.clear();
        this.warningSeconds.addAll(warningSeconds);
    }

    /**
     * Gets the warning seconds for reset notifications.
     *
     * @return the set of seconds before reset to show warnings
     */
    public Set<Integer> getWarningSeconds() {
        return this.warningSeconds;
    }

    /**
     * Calculates the remaining chance percentage after all block spawn entries.
     *
     * @return the remaining chance (out of 100)
     */
    public double calculateRestChance() {
        double max = 100;
        for (double chance : blockSpawnEntries.values()) {
            max -= chance;
        }

        return max;
    }

    /**
     * Checks if a player can mine in this mine based on their rank and permissions.
     *
     * @param p the player to check
     * @return true if the player can mine, false otherwise
     */
    public boolean canMine(Player p) {
        PlayerData data = SuperMinesAPI.getOrCreatePlayerData(p.getUniqueId());
        StringRankSet rank = data.getRank();

        if (p.isOp() || p.hasPermission(Constants.Permission.BYPASS_RANK)) {
            return true;
        }

        if (allowedRankIds.isEmpty()) {
            return true;
        }

        if (rank.matchRank(allowedRankIds)) {
            return true;
        }

        return rank.matchRankLevel(requiredRankLevel);
    }
}
