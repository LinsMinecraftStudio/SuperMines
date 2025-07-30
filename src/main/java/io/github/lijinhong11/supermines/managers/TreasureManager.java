package io.github.lijinhong11.supermines.managers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import io.github.lijinhong11.supermines.utils.ItemUtils;
import java.util.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreasureManager extends AbstractFileObjectManager<Treasure> {
    private final Map<String, Treasure> treasures = new HashMap<>();

    public TreasureManager() {
        super("data/treasures.yml");

        load();
    }

    private void load() {
        for (Treasure object : super.getAll()) {
            treasures.put(object.getId(), object);
        }
    }

    @Override
    protected Treasure getObject(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();

        if (id == null) {
            return null;
        }

        int chance = section.getInt("chance");
        if (chance <= 0 || chance > 100) {
            chance = 1;
            section.set("chance", 1);
        }

        return new Treasure(
                id,
                MiniMessage.miniMessage().deserialize(section.getString("displayName", id)),
                ItemUtils.deserializeFromBytes(section.getObject("itemStack", byte[].class)),
                chance,
                section.getStringList("matchedMaterials").stream()
                        .map(Material::getMaterial)
                        .toList());
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, Treasure object) {
        section.set("id", object.getId());
        section.set("displayName", MiniMessage.miniMessage().serialize(object.getDisplayName()));
        section.set("itemStack", ItemUtils.serializeToBytes(object.getItemStack()));
        section.set("chance", object.getChance());
        section.set("matchedMaterials", object.getMatchedMaterials().stream().map(Material::toString).toList());
    }

    @Override
    public void saveAndClose() {
        for (Treasure treasure : treasures.values()) {
            super.putObject(treasure.getId(), treasure);
        }
    }

    public void addTreasure(Treasure treasure) {
        Preconditions.checkNotNull(treasure, "treasure cannot be null");

        if (treasures.containsKey(treasure.getId())) {
            throw new IllegalArgumentException("treasure with ID " + treasure.getId() + " already exists");
        }

        treasures.put(treasure.getId(), treasure);
        super.putObject(treasure.getId(), treasure);
    }

    public @Nullable Treasure getTreasure(@NotNull String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "treasure id cannot be null or empty");

        return treasures.get(id);
    }

    public void removeTreasure(@NotNull String key) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "treasure id cannot be null or empty");

        treasures.remove(key);
        super.remove(key);
    }

    public Collection<Treasure> getAllTreasures() {
        return treasures.values();
    }

    public Set<String> getAllTreasureIds() {
        return treasures.keySet();
    }
}
