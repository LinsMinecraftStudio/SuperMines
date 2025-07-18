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
        super("mines.yml");

        load();
    }

    private void load() {
        for (Mine object : super.getAll()) {
            mines.put(object.getId(), object);
        }
    }

    @Override
    public Mine deserialize(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();
        String displayName = section.getString("displayName", id);
        String worldName = section.getString("world");
        ConfigurationSection pos1 = section.getConfigurationSection("pos1");
        ConfigurationSection pos2 = section.getConfigurationSection("pos2");

        if (id == null) {
            return null;
        }

        if (Strings.isNullOrEmpty(worldName) || Bukkit.getWorld(worldName) == null) {
            return null;
        }


        if (pos1 == null) {
            return null;
        }


        if (pos2 == null) {
            return null;
        }

        BlockPos blockPos1 = new BlockPos(pos1.getInt("x"), pos1.getInt("y"), pos1.getInt("z"));
        BlockPos blockPos2 = new BlockPos(pos2.getInt("x"), pos2.getInt("y"), pos2.getInt("z"));
        return new Mine(id, MiniMessage.miniMessage().deserialize(displayName), Bukkit.getWorld(worldName), new CuboidArea(blockPos1, blockPos2), new HashMap<>());
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section, Mine object) {
        section.set("displayName", MiniMessage.miniMessage().serialize(object.getDisplayName()));
        section.set("world", object.getWorld().getName());
        section.set("pos1", object.getArea().pos1().toMap());
        section.set("pos2", object.getArea().pos2().toMap());

        List<String> treasures = new ArrayList<>();
        for (Treasure treasure : object.getTreasures()) {
            treasures.add(treasure.getId());
        }

        section.set("treasures", treasures);
    }

    public void addMine(Mine mine) {
        Preconditions.checkNotNull(mine, "Mine cannot be null");

        if (mines.containsKey(mine.getId())) {
            throw new IllegalArgumentException("Mine with ID " + mine.getId() + " already exists");
        }

        mines.put(mine.getId(), mine);
        super.serialize(mine.getId(), mine);
    }

    public @Nullable Mine getMine(String id) {
        return mines.get(id);
    }

    public void removeMine(String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "Mine ID cannot be null or empty");

        if (mines.containsKey(id)) {
            mines.remove(id);
            super.remove(id);
        }
    }
}
