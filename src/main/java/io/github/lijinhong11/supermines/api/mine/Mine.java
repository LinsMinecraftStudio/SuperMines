package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.mittellib.hook.content.MinecraftContentProvider;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.AreaOfBlocks;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.math.SphereArea;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.events.MineEditEvent;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.managers.database.StringRankSet;
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
    private final WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries;

    private final List<Treasure> treasures;
    private final Set<String> allowedRankIds;

    private final Set<Integer> warningSeconds;

    private final AtomicInteger blocksBroken = new AtomicInteger(0);

    private Material displayIcon;
    private Component displayName;
    private AreaOfBlocks area;
    private int regenerateSeconds;
    private boolean onlyFillAirWhenRegenerate;
    private boolean autoPickup;
    private int requiredRankLevel;
    private Location tpLoc;

    @ParametersAreNonnullByDefault
    public Mine(
            String id,
            Component displayName,
            World world,
            AreaOfBlocks area,
            WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries,
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
            AreaOfBlocks area,
            WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries,
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
            AreaOfBlocks area,
            WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries,
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
            AreaOfBlocks area,
            WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries,
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
            AreaOfBlocks area,
            WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries,
            int regenerateSeconds,
            boolean onlyFillAirWhenRegenerate,
            List<Treasure> treasures,
            int requiredRankLevel,
            Set<String> allowedRankIds,
            @Nullable Location tpLoc,
            Set<Integer> warningSeconds) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");
        Preconditions.checkArgument(id.matches(Constants.ID_PATTERN), "Mine ID cannot contain special characters");

        this.id = id;
        this.displayName = Preconditions.checkNotNull(displayName, "Mine display name must not be null");
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
        this.tpLoc = tpLoc;
    }

    /**
     * Checks if a player is currently inside this mine.
     *
     * @param player the player to check
     * @return true if the player is inside the mine, false otherwise
     */
    public boolean isPlayerInMine(Player player) {
        return area.contains(BlockPos.fromLocation(player.getLocation()));
    }

    /**
     * Adds a treasure to this mine by its ID.
     *
     * @param id the treasure ID to add
     */
    public void addTreasure(String id) {
        treasures.add(SuperMinesAPI.getTreasure(id));
    }

    /**
     * Adds a treasure to this mine.
     *
     * @param treasure the treasure to add
     */
    public void addTreasure(Treasure treasure) {
        if (treasures.stream().noneMatch(existing -> existing.getId().equals(treasure.getId()))) {
            treasures.add(treasure);
        }
    }

    /**
     * Removes a treasure from this mine by its ID.
     *
     * @param id the treasure ID to remove
     */
    public void removeTreasure(String id) {
        treasures.removeIf(treasure -> treasure.getId().equals(id));
    }

    /**
     * Removes a treasure from this mine.
     *
     * @param treasure the treasure to remove
     */
    public void removeTreasure(Treasure treasure) {
        treasures.remove(treasure);
    }

    /**
     * Adds a block spawn entry with the specified material and weight.
     *
     * @param material the material of the block to spawn
     * @param weight   the spawn weight (> 0)
     * @throws IllegalArgumentException if weight is not greater than 0
     */
    public void addBlockSpawnEntry(Material material, double weight) {
        addBlockSpawnEntry(new MinecraftContentProvider.PackedMinecraftBlock(material), weight);
    }

    /**
     * Adds a block spawn entry with the specified material and weight.
     *
     * @param block  the block to spawn
     * @param weight the spawn weight (> 0)
     * @throws IllegalArgumentException if weight is not greater than 0
     */
    public void addBlockSpawnEntry(PackedBlock block, double weight) {
        Preconditions.checkArgument(Double.isFinite(weight) && weight > 0, "weight must be finite and greater than 0");

        BlockSpawnEntry existing = blockSpawnEntries.keySet().stream()
                .filter(entry -> entry.getId().equals(block.getId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            blockSpawnEntries.put(existing, weight);
            return;
        }

        BlockSpawnEntry entry = block instanceof BlockSpawnEntry b ? b : new BlockSpawnEntry(block);
        blockSpawnEntries.put(entry, weight);
    }

    /**
     * Adds multiple block spawn entries to this mine.
     *
     * @param blockSpawnEntries the map of blocks to their spawn chances
     */
    public void addBlockSpawnEntries(WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries) {
        this.blockSpawnEntries.putAll(blockSpawnEntries);
    }

    /**
     * Removes a block spawn entry by material.
     *
     * @param material the material to remove
     */
    public void removeBlockSpawnEntry(Material material) {
        removeBlockSpawnEntry(new MinecraftContentProvider.PackedMinecraftBlock(material));
    }

    /**
     * Removes a block spawn entry by addon block.
     *
     * @param block the addon block to remove
     */
    public void removeBlockSpawnEntry(PackedBlock block) {
        blockSpawnEntries.keySet().stream()
                .filter(entry -> entry.getId().equals(block.getId()))
                .findFirst()
                .ifPresent(blockSpawnEntries::remove);
    }

    /**
     * Removes multiple block spawn entries.
     *
     * @param blocks the list of blocks to remove
     */
    public void removeBlockSpawnEntries(List<PackedBlock> blocks) {
        blocks.forEach(this::removeBlockSpawnEntry);
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
     * Gets all allowed rank IDs for this mine.
     *
     * @return a set of allowed rank IDs
     */
    public Set<String> getAllowedRankIds() {
        return allowedRankIds;
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
     * Gets the teleport location for this mine.
     *
     * @return the teleport location, or null if not set
     */
    public @Nullable Location getTeleportLocation() {
        return tpLoc;
    }

    /**
     * Sets the teleport location for this mine.
     *
     * @param tpLoc the teleport location (cannot be null)
     * @throws NullPointerException if tpLoc is null
     */
    public void setTeleportLocation(Location tpLoc) {
        Preconditions.checkNotNull(tpLoc, "teleport location cannot be null");

        if (!allowEdit(MineEditEvent.Property.TELEPORT_LOCATION, this.tpLoc, tpLoc)) return;
        this.tpLoc = tpLoc;
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
    public @NotNull String getRawDisplayName() {
        return ComponentUtils.serialize(displayName);
    }

    /**
     * Gets the display name of this mine.
     *
     * @return the display name component, or a default name if not set
     */
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of this mine.
     *
     * @param displayName the display name component (cannot be null)
     */
    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        if (!allowEdit(MineEditEvent.Property.DISPLAY_NAME, this.displayName, displayName)) return;
        this.displayName = displayName;
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
     * Sets the display icon material for this mine.
     *
     * @param displayIcon the material to use as display icon (cannot be null)
     * @throws NullPointerException if displayIcon is null
     */
    public void setDisplayIcon(Material displayIcon) {
        Preconditions.checkNotNull(displayIcon, "display icon cannot be null");

        if (!allowEdit(MineEditEvent.Property.DISPLAY_ICON, this.displayIcon, displayIcon)) return;
        this.displayIcon = displayIcon;
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
     * Gets the area of this mine.
     *
     * @return the area
     */
    public AreaOfBlocks getArea() {
        return area;
    }

    /**
     * Sets the area of this mine.
     *
     * @param area the area (cannot be null)
     * @throws NullPointerException if area is null
     */
    public void setArea(AreaOfBlocks area) {
        Preconditions.checkNotNull(area, "area cannot be null");

        if (!allowEdit(MineEditEvent.Property.AREA, this.area, area)) return;
        this.area = area;
    }

    /**
     * Gets all treasures associated with this mine.
     *
     * @return a list of treasures
     */
    public List<Treasure> getTreasures() {
        return List.copyOf(treasures);
    }

    /**
     * Sets all treasures for this mine, replacing any existing ones.
     *
     * @param treasures the list of treasures to set
     */
    public void setTreasures(List<Treasure> treasures) {
        this.treasures.clear();
        this.treasures.addAll(treasures);
    }

    /**
     * Gets all block spawn entries for this mine.
     *
     * @return a map of blocks to their spawn weights
     */
    public WeightedRandomMap<BlockSpawnEntry> getBlockSpawnEntries() {
        return blockSpawnEntries;
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
     * Sets the regeneration time in seconds for this mine.
     *
     * @param regenerateSeconds the regeneration time in seconds (must be >= 0)
     * @throws IllegalArgumentException if regenerateSeconds is negative
     */
    public void setRegenerateSeconds(@Range(from = 0, to = Integer.MAX_VALUE) int regenerateSeconds) {
        Preconditions.checkArgument(regenerateSeconds >= 0, "regenerate seconds must equal to or greater than 0");

        if (!allowEdit(MineEditEvent.Property.RESET_SECONDS, this.regenerateSeconds, regenerateSeconds)) return;
        this.regenerateSeconds = regenerateSeconds;
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
     * Sets whether this mine should only fill air blocks when regenerating.
     *
     * @param onlyFillAirWhenRegenerate true to only fill air blocks, false to replace all blocks
     */
    public void setOnlyFillAirWhenRegenerate(boolean onlyFillAirWhenRegenerate) {
        if (!allowEdit(MineEditEvent.Property.ONLY_FILL_AIR, this.onlyFillAirWhenRegenerate, onlyFillAirWhenRegenerate))
            return;
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
    }

    public boolean isAutoPickup() {
        return autoPickup;
    }

    public void setAutoPickup(boolean autoPickup) {
        if (!allowEdit(MineEditEvent.Property.AUTO_PICKUP, this.autoPickup, autoPickup)) return;
        this.autoPickup = autoPickup;
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
     * Sets the required rank level for this mine.
     *
     * @param requiredRankLevel the minimum rank level required
     */
    public void setRequiredRankLevel(int requiredRankLevel) {
        Preconditions.checkArgument(requiredRankLevel > 0, "required rank level must be greater than 0");
        if (!allowEdit(MineEditEvent.Property.REQUIRED_RANK_LEVEL, this.requiredRankLevel, requiredRankLevel)) return;
        this.requiredRankLevel = requiredRankLevel;
    }

    /**
     * Sets the required rank level based on a rank object.
     *
     * @param requiredRank the rank to use for the required level
     */
    public void setRequiredRankLevel(@NotNull Rank requiredRank) {
        setRequiredRankLevel(requiredRank.getLevel());
    }

    private boolean allowEdit(MineEditEvent.Property property, Object oldValue, Object newValue) {
        SuperMines plugin = SuperMines.getInstance();
        if (plugin == null
                || plugin.getMineManager() == null
                || plugin.getMineManager().getMine(id) != this) {
            return true;
        }

        MineEditEvent event = new MineEditEvent(this, property, oldValue, newValue);
        event.callEvent();
        return !event.isCancelled();
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
        this.blocksBroken.updateAndGet(value -> value == Integer.MAX_VALUE ? value : value + 1);
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
     * Sets the warning seconds for reset notifications.
     *
     * @param warningSeconds the set of seconds before reset to show warnings
     */
    public void setWarningSeconds(Set<Integer> warningSeconds) {
        this.warningSeconds.clear();
        this.warningSeconds.addAll(warningSeconds);
    }

    public Location getCenterLocation() {
        return getAreaCenter(world);
    }

    public Location getSafeTopLocation() {
        int topY;
        int centerX;
        int centerZ;

        if (area instanceof CuboidArea ca) {
            topY = ca.getMax().y();
            centerX = (ca.getMin().x() + ca.getMax().x()) / 2;
            centerZ = (ca.getMin().z() + ca.getMax().z()) / 2;
        } else if (area instanceof SphereArea(BlockPos center, int radius)) {
            topY = center.y() + radius;
            centerX = center.x();
            centerZ = center.z();
        } else {
            return world.getSpawnLocation();
        }

        for (int dx = 0; dx <= 5; dx++) {
            for (int dz = 0; dz <= 5; dz++) {
                for (int signX : new int[] {-1, 1}) {
                    for (int signZ : new int[] {-1, 1}) {
                        int x = centerX + signX * dx;
                        int z = centerZ + signZ * dz;
                        Location feet = new Location(world, x + 0.5, topY + 1, z + 0.5);
                        if (feet.getBlock().getType().isAir()
                                && feet.clone()
                                        .add(0, 1, 0)
                                        .getBlock()
                                        .getType()
                                        .isAir()) {
                            return feet;
                        }
                    }
                }
            }
        }

        return new Location(world, centerX + 0.5, topY + 2, centerZ + 0.5);
    }

    private Location getAreaCenter(World world) {
        if (area instanceof CuboidArea ca) {
            return ca.getCenterLocation(world);
        } else if (area instanceof SphereArea sa) {
            return sa.center().toLocation(world);
        }
        return world.getSpawnLocation();
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
