package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * The mine object.
 */
public final class Mine implements Identified {
    private final String id;

    private final World world;
    private final Map<Material, Double> blockSpawnEntries;

    private final List<Treasure> treasures;
    private final Set<String> allowedRankIds;

    private Material displayIcon;
    private Component displayName;
    private CuboidArea area;
    private int regenerateSeconds;
    private boolean onlyFillAirWhenRegenerate;
    private int requiredRankLevel;
    private Location tpLoc;

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, Constants.Items.DEFAULT_MINE_ICON, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, new ArrayList<>(), new HashSet<>());
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures, Set<String> allowedRankIds) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, treasures, Rank.DEFAULT.getLevel(), allowedRankIds);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures, int requiredRankLevel, Set<String> allowedRankIds) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, treasures, requiredRankLevel, allowedRankIds, null);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, Material displayIcon, World world, CuboidArea area, Map<Material, Double> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures, int requiredRankLevel, Set<String> allowedRankIds, @Nullable Location tpLoc) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");
        Preconditions.checkArgument(id.matches(Constants.StringsAndComponents.ID_PATTERN), "Mine ID cannot contain special characters");

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
        this.tpLoc = tpLoc == null ? area.getCenterLocation(world) : tpLoc;
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

    public void addAllowedRankId(String rankId) {
        allowedRankIds.add(rankId);
    }

    public void removeAllowedRankId(String rankId) {
        allowedRankIds.remove(rankId);
    }

    public void setAllowedRankIds(Collection<String> rankIds) {
        allowedRankIds.clear();
        allowedRankIds.addAll(rankIds);
    }

    public Set<String> getAllowedRankIds() {
        return new HashSet<>(allowedRankIds);
    }

    public void setTeleportLocation(@NotNull Location tpLoc) {
        Preconditions.checkNotNull(tpLoc, "teleport location cannot be null");

        this.tpLoc = tpLoc;
    }

    public @Nullable Location getTeleportLocation() {
        return tpLoc;
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

    public List<Treasure> getTreasures() {
        return new ArrayList<>(treasures);
    }

    public Map<Material, Double> getBlockSpawnEntries() {
        return new HashMap<>(blockSpawnEntries);
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

    public void setRequiredRankLevel(int requiredRankLevel) {
        this.requiredRankLevel = requiredRankLevel;
    }

    public void setRequiredRankLevel(Rank requiredRank) {
        this.requiredRankLevel = requiredRank.getLevel();
    }

    public int getRequiredRankLevel() {
        return requiredRankLevel;
    }

    public double calculateRestChance() {
        double max = 100;
        for (double chance : blockSpawnEntries.values()) {
            max -= chance;
        }

        return max;
    }

    public boolean canMine(Player p) {
        PlayerData data = SuperMinesAPI.getOrCreatePlayerData(p.getUniqueId());
        Rank rank = data.getRank();

        if (p.hasPermission(Constants.Permission.BYPASS_RANK)) {
            return true;
        }

        if (allowedRankIds.contains(rank.getId())) {
            return true;
        }

        return rank.getLevel() >= requiredRankLevel;
    }
}
