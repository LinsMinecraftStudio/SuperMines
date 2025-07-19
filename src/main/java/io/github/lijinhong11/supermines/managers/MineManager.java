package io.github.lijinhong11.supermines.managers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Mine getObject(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();
        String displayName = section.getString("displayName", id);
        String worldName = section.getString("world");
        ConfigurationSection pos1 = section.getConfigurationSection("pos1");
        ConfigurationSection pos2 = section.getConfigurationSection("pos2");
        int regenerateSeconds = section.getInt("regenerateSeconds", 0);
        boolean onlyFillAirWhenRegenerate = section.getBoolean("onlyFillAirWhenRegenerate", false);

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

        if (regenerateSeconds < 0) {
            regenerateSeconds = 3600; // 1 hour
            section.set("regenerateSeconds", regenerateSeconds);
        }

        BlockPos blockPos1 = new BlockPos(pos1.getInt("x"), pos1.getInt("y"), pos1.getInt("z"));
        BlockPos blockPos2 = new BlockPos(pos2.getInt("x"), pos2.getInt("y"), pos2.getInt("z"));
        return new Mine(id, MiniMessage.miniMessage().deserialize(displayName), world, new CuboidArea(blockPos1, blockPos2), new HashMap<>(), regenerateSeconds, onlyFillAirWhenRegenerate);
    }

    @Override
    public void putObject(@NotNull ConfigurationSection section, Mine object) {
        section.set("displayName", MiniMessage.miniMessage().serialize(object.getDisplayName()));
        section.set("world", object.getWorld().getName());
        section.set("pos1", object.getArea().pos1().toMap());
        section.set("pos2", object.getArea().pos2().toMap());
        section.set("regenerateSeconds", object.getRegenerateSeconds());
        section.set("onlyFillAirWhenRegenerate", object.isOnlyFillAirWhenRegenerate());

        List<String> treasures = new ArrayList<>();
        for (Treasure treasure : object.getTreasures()) {
            treasures.add(treasure.getId());
        }

        Map<String, Integer> blockSpawnEntries = new HashMap<>();
        section.set("blockSpawnEntries", blockSpawnEntries);

        section.set("treasures", treasures);
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
            if (!mine.getWorld().getName().equals(loc.getWorld().getName())) {
                continue;
            }

            if (mine.getArea().contains(BlockPos.fromLocation(loc))) {
                return mine;
            }
        }

        return null;
    }

    @Override
    public void remove(@NotNull String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "mine ID cannot be null or empty");

        mines.remove(id);
        super.remove(id);
    }
}
