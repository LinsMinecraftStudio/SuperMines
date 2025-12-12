package io.github.lijinhong11.supermines.managers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import io.github.lijinhong11.supermines.integrates.block.BlockAddon;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import java.util.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MineManager extends AbstractFileObjectManager<Mine> {
    private final Map<String, Mine> mines = new HashMap<>();

    public MineManager() {
        super("data/mines.yml");

        load();
    }

    private void load() {
        for (Mine object : super.getAll()) {
            mines.put(object.getId(), object);
        }
    }

    @Override
    protected Mine getObject(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();
        String displayName = section.getString("displayName", id);
        String worldName = section.getString("world");
        ConfigurationSection pos1 = section.getConfigurationSection("pos1");
        ConfigurationSection pos2 = section.getConfigurationSection("pos2");
        int regenerateSeconds = section.getInt("regenerateSeconds", 0);
        boolean onlyFillAirWhenRegenerate = section.getBoolean("onlyFillAirWhenRegenerate", false);
        Material displayIcon = Material.getMaterial(section.getString("displayIcon", "STONE"));
        int requiredRankLevel = section.getInt("requiredRankLevel", 1);
        Location loc = section.getLocation("teleportLocation");

        if (id == null) {
            return null;
        }

        if (Strings.isNullOrEmpty(worldName)) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        if (pos1 == null) {
            return null;
        }

        if (pos2 == null) {
            return null;
        }

        if (displayIcon == null) {
            return null;
        }

        if (regenerateSeconds < 0) {
            regenerateSeconds = 3600;
            section.set("regenerateSeconds", regenerateSeconds);
        }

        BlockPos blockPos1 = new BlockPos(pos1.getInt("x"), pos1.getInt("y"), pos1.getInt("z"));
        BlockPos blockPos2 = new BlockPos(pos2.getInt("x"), pos2.getInt("y"), pos2.getInt("z"));

        Map<AddonBlock, Double> blockSpawnEntries = new HashMap<>();

        ConfigurationSection blockSpawn = section.getConfigurationSection("blockSpawnEntries");
        if (blockSpawn != null) {
            for (String m : blockSpawn.getKeys(false)) {
                AddonBlock block = BlockAddon.getAddonBlock(m);
                if (block == null) {
                    continue;
                }

                blockSpawnEntries.put(block, blockSpawn.getDouble(m, 1));
            }
        }

        List<String> treasures = section.getStringList("treasures");
        List<Treasure> treasureList = treasures.stream()
                .filter(treasure ->
                        SuperMines.getInstance().getTreasureManager().getTreasure(treasure) != null)
                .map(t -> SuperMines.getInstance().getTreasureManager().getTreasure(t))
                .toList();

        Set<String> allowedRankIds = new HashSet<>();
        if (section.contains("allowedRankIds")) {
            allowedRankIds = new HashSet<>(section.getStringList("allowedRankIds"));
        }

        Set<Integer> resetWarningSeconds = new HashSet<>();
        if (section.contains("resetWarningSeconds")) {
            resetWarningSeconds = new HashSet<>(section.getIntegerList("resetWarningSeconds"));
        }

        return new Mine(
                id,
                MiniMessage.miniMessage().deserialize(displayName),
                displayIcon,
                world,
                new CuboidArea(blockPos1, blockPos2),
                blockSpawnEntries,
                regenerateSeconds,
                onlyFillAirWhenRegenerate,
                treasureList,
                requiredRankLevel,
                allowedRankIds,
                loc,
                resetWarningSeconds);
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, Mine object) {
        section.set("displayName", MiniMessage.miniMessage().serialize(object.getDisplayName()));
        section.set("world", object.getWorld().getName());
        section.set("pos1", object.getArea().pos1().toMap());
        section.set("pos2", object.getArea().pos2().toMap());
        section.set("regenerateSeconds", object.getRegenerateSeconds());
        section.set("onlyFillAirWhenRegenerate", object.isOnlyFillAirWhenRegenerate());
        section.set("displayIcon", object.getDisplayIcon().toString());
        section.set("requiredRankLevel", object.getRequiredRankLevel());
        section.set("allowedRankIds", object.getAllowedRankIds().stream().toList());
        section.set("resetWarningSeconds", object.getWarningSeconds().stream().toList());

        List<String> treasures = new ArrayList<>();
        for (Treasure treasure : object.getTreasures()) {
            treasures.add(treasure.getId());
        }
        section.set("treasures", treasures);

        Map<String, Double> blockSpawnEntries = new HashMap<>();
        for (Map.Entry<AddonBlock, Double> entry : object.getBlockSpawnEntries().entrySet()) {
            blockSpawnEntries.put(entry.getKey().toString(), entry.getValue());
        }

        ConfigurationSection blockSpawn = section.createSection("blockSpawnEntries");
        for (Map.Entry<String, Double> entry : blockSpawnEntries.entrySet()) {
            blockSpawn.set(entry.getKey(), entry.getValue());
        }

        if (object.getTeleportLocation() != null) {
            section.set("teleportLocation", object.getTeleportLocation());
        }
    }

    @Override
    public void saveAndClose() {
        for (Mine mine : mines.values()) {
            super.putObject(mine.getId(), mine);
        }
    }

    public void addMine(@NotNull Mine mine) {
        Preconditions.checkNotNull(mine, "mine cannot be null");

        if (mines.containsKey(mine.getId())) {
            throw new IllegalArgumentException("mine with ID " + mine.getId() + " already exists");
        }

        mines.put(mine.getId(), mine);
        super.putObject(mine.getId(), mine);
    }

    public @Nullable Mine getMine(@NotNull String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "mine id cannot be null or empty");

        return mines.get(id);
    }

    public @Nullable Mine getMine(@NotNull Location loc) {
        for (Mine mine : mines.values()) {
            boolean sameWorld = mine.getWorld() == loc.getWorld();
            boolean contains = mine.getArea().contains(BlockPos.fromLocation(loc));
            if (sameWorld && contains) {
                return mine;
            }
        }

        return null;
    }

    public void removeMine(@NotNull String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "mine ID cannot be null or empty");

        mines.remove(id);
        super.remove(id);
    }

    public Collection<Mine> getAllMines() {
        return mines.values();
    }

    public Set<String> getAllMineIds() {
        return mines.keySet();
    }
}
