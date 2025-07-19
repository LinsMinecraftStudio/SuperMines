package io.github.lijinhong11.supermines.api.mine;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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

    private final Component displayName;
    private final ItemStack displayIcon;

    private final World world;
    private final CuboidArea area;
    private final Map<Material, Integer> blockSpawnEntries;
    private final int regenerateSeconds;
    private final boolean onlyFillAirWhenRegenerate;
    private final List<Treasure> treasures;

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, World world, CuboidArea area, Map<Material, Integer> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, Constants.Items.DEFAULT_MINE_ICON, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate);
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, ItemStack displayIcon, World world, CuboidArea area, Map<Material, Integer> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate) {
        this(id, displayName, displayIcon, world, area, blockSpawnEntries, regenerateSeconds, onlyFillAirWhenRegenerate, new ArrayList<>());
    }

    @ParametersAreNonnullByDefault
    public Mine(String id, Component displayName, ItemStack displayIcon, World world, CuboidArea area, Map<Material, Integer> blockSpawnEntries, int regenerateSeconds, boolean onlyFillAirWhenRegenerate, List<Treasure> treasures) {
        this.onlyFillAirWhenRegenerate = onlyFillAirWhenRegenerate;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");
        Preconditions.checkArgument(id.matches("^[a-zA-Z0-9_-]$"), "Mine ID cannot contain special characters");

        this.id = id;
        this.displayName = displayName;
        this.displayIcon = displayIcon;
        this.world = world;
        this.area = area;
        this.blockSpawnEntries = blockSpawnEntries;
        this.regenerateSeconds = regenerateSeconds;
        this.treasures = treasures;
    }

    public boolean isPlayerInMine(Player player) {
        return area.contains(BlockPos.fromLocation(player.getLocation()));
    }

    public void addTreasure(String id) {
        treasures.add(SuperMinesAPI.getTreasure(id));
    }

    public void addTreasure(Treasure treasure) {
        treasures.add(treasure);
    }

    public void addTreasure(List<Treasure> treasures) {
        this.treasures.addAll(treasures);
    }

    public void removeTreasure(@NotNull String id) {
        treasures.removeIf(treasure -> treasure.getId().equals(id));
    }

    public void removeTreasure(Treasure treasure) {
        treasures.remove(treasure);
    }

    public void removeTreasure(List<Treasure> treasures) {
        this.treasures.removeAll(treasures);
    }

    public void setTreasures(List<Treasure> treasures) {
        this.treasures.clear();
        this.treasures.addAll(treasures);
    }

    public void clearTreasures() {
        treasures.clear();
    }

    public void addBlockSpawnEntry(Material material, int chance) {
        blockSpawnEntries.put(material, chance);
    }

    public void addBlockSpawnEntries(Map<Material, Integer> blockSpawnEntries) {
        this.blockSpawnEntries.putAll(blockSpawnEntries);
    }

    public void removeBlockSpawnEntry(Material material) {
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

    public Component getDisplayName() {
        return displayName;
    }

    public ItemStack getDisplayIcon() {
        return displayIcon;
    }

    public World getWorld() {
        return world;
    }

    public CuboidArea getArea() {
        return area;
    }

    public @Unmodifiable List<Treasure> getTreasures() {
        return Collections.unmodifiableList(treasures);
    }

    public @Unmodifiable Map<Material, Integer> getBlockSpawnEntries() {
        return Collections.unmodifiableMap(blockSpawnEntries);
    }

    public int getRegenerateSeconds() {
        return regenerateSeconds;
    }

    public boolean isOnlyFillAirWhenRegenerate() {
        return onlyFillAirWhenRegenerate;
    }
}
