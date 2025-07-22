package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The mine object.
 */
public class Mine {
    private final String id;

    private final World world;
    private final Map<Material, Double> blockSpawnEntries;

    private final List<Treasure> treasures;

    private Material displayIcon;
    private Component displayName;
    private CuboidArea area;
    private int regenerateSeconds;
    private boolean onlyFillAirWhenRegenerate;
    private Rank requiredRank;

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, Constants.Items.DEFAULT_MINE_ICON, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, new ArrayList<>());
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, treasures, Rank.DEFAULT);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures, Rank requiredRank) {
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");
        Preconditions.checkArgument(id.matches(Constants.StringsAndComponents.ID_PATTERN), "Mine ID cannot contain special characters");

        this.id = id;
        this.displayName = displayName;
        this.displayIcon = displayIcon;
        this.world = world;
        this.area = area;
        this.blockSpawnEntries = blockSpawnEntries;
        this.regenerateSeconds = regenerateSeconds;
        this.treasures = treasures;
        this.requiredRank = requiredRank;
    }

    public boolean isPlayerInMine(@NotNull Player player) {
        return area.contains(BlockPos.fromLocation(player.getLocation()));
    }

    public void addTreasure(@NotNull String id) {
        treasures.add(SuperMinesAPI.getTreasure(id));
    }

    public void addTreasure(@NotNull Treasure treasure) {
        treasures.add(treasure);
    }

    public void addTreasure(@NotNull List<Treasure> treasures) {
        this.treasures.addAll(treasures);
    }

    public void removeTreasure(@NotNull String id) {
        treasures.removeIf(treasure -> treasure.getId().equals(id));
    }

    public void removeTreasure(@NotNull Treasure treasure) {
        treasures.remove(treasure);
    }

    public void removeTreasure(@NotNull List<Treasure> treasures) {
        this.treasures.removeAll(treasures);
    }

    public void setTreasures(@NotNull List<Treasure> treasures) {
        this.treasures.clear();
        this.treasures.addAll(treasures);
    }

    public void clearTreasures() {
        treasures.clear();
    }

    public void addBlockSpawnEntry(@NotNull Material material, @Range(from = 1, to = 100) double chance) {
        if (chance < 1 || chance > 100) {
            throw new IllegalArgumentException("Chance must be between 1 and 100");
        }
        blockSpawnEntries.put(material, chance);
    }

    public void addBlockSpawnEntries(Map<Material, Double> blockSpawnEntries) {
        this.blockSpawnEntries.putAll(blockSpawnEntries);
    }

    public void removeBlockSpawnEntry(@NotNull Material material) {
        blockSpawnEntries.remove(material);
    }

    public void removeBlockSpawnEntries(Material... materials) {
        for (Material material : materials) {
            blockSpawnEntries.remove(material);
        }
    }

    public void removeBlockSpawnEntries(List<Material> materials) {
        materials.forEach(blockSpawnEntries::remove);
    }

    public String getId() {
        return id;
    }

    public String getRawDisplayName() {
        return ComponentUtils.serialize(displayName);
    }

    public void setDisplayName(@NotNull Component displayName) {
        Preconditions.checkNotNull(displayName, "display name cannot be null");

        this.displayName = displayName;
    }

    public Component getDisplayName() {
        return displayName == null ? Component.text(id) : displayName;
    }

    public void setDisplayIcon(Material displayIcon) {
        Preconditions.checkNotNull(displayIcon, "display icon cannot be null");

        this.displayIcon = displayIcon;
    }

    public Material getDisplayIcon() {
        return displayIcon;
    }

    public World getWorld() {
        return world;
    }

    public void setArea(@NotNull CuboidArea area) {
        Preconditions.checkNotNull(area, "area cannot be null");

        this.area = area;
    }

    public CuboidArea getArea() {
        return area;
    }

    public @Unmodifiable List<Treasure> getTreasures() {
        return Collections.unmodifiableList(treasures);
    }

    public @Unmodifiable Map<Material, Double> getBlockSpawnEntries() {
        return Collections.unmodifiableMap(blockSpawnEntries);
    }

    public void setRegenerateSeconds(@Range(from = 1, to = Integer.MAX_VALUE) int regenerateSeconds) {
        this.regenerateSeconds = regenerateSeconds;
    }

    public int getRegenerateSeconds() {
        return regenerateSeconds;
    }

    public void setOnlyFillAirWhenRegenerate(boolean onlyFillAirWhenRegenerate) {
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
    }

    public boolean isOnlyFillAirWhenRegenerate() {
        return onlyFillAirWhenRegenerate;
    }

    public void setRequiredRank(Rank requiredRank) {
        this.requiredRank = requiredRank;
    }

    public Rank getRequiredRank() {
        return requiredRank;
    }
}
