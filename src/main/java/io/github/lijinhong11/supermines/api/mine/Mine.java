package io.github.lijinhong11.supermines.api.mine;

import io.github.lijinhong11.supermines.api.SuperMinesAPI;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The mine object.
 */
public class Mine {
    private final String id;
    private final Component displayName;

    private final World world;
    private final CuboidArea area;
    private final Map<Material, Integer> blockSpawnEntries;
    private final List<Treasure> treasures;

    public Mine(String id, Component displayName, World world, CuboidArea area, Map<Material, Integer> blockSpawnEntries) {
        this(id, displayName, world, area, blockSpawnEntries, new ArrayList<>());
    }

    public Mine(String id, Component displayName, World world, CuboidArea area, Map<Material, Integer> blockSpawnEntries, List<Treasure> treasures) {
        this.id = id;
        this.displayName = displayName;
        this.world = world;
        this.area = area;
        this.blockSpawnEntries = blockSpawnEntries;
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

    public void removeTreasure(String id) {
        treasures.removeIf(treasure -> treasure.id().equals(id));
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

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public World getWorld() {
        return world;
    }

    public CuboidArea getArea() {
        return area;
    }

    public List<Treasure> getTreasures() {
        return treasures;
    }

    public Map<Material, Integer> getBlockSpawnEntries() {
        return blockSpawnEntries;
    }
}
