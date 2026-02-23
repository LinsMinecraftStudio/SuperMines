package io.github.lijinhong11.supermines.managers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.item.MittelItem;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
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

        double chance = section.getDouble("chance");
        if (chance <= 0 || chance > 100) {
            chance = 1;
            section.set("chance", 1);
        }

        ItemStack item = null;
        if (section.contains("item")) {
            ConfigurationSection itemSection = section.getConfigurationSection("item");
            if (itemSection != null) {
                item = MittelItem.readFromSection(itemSection).get();
            }
        } else if (section.contains("itemStack")) {
            item = section.getObject("itemStack", byte[].class) == null
                    ? null
                    : ItemStack.deserializeBytes(section.getObject("itemStack", byte[].class));
            if (item != null) {
                ConfigurationSection cs = section.createSection("item");
                new MittelItem(item).write(cs);

                section.set("itemStack", null);
            }
        }

        return new Treasure(
                id,
                MiniMessage.miniMessage().deserialize(section.getString("displayName", id)),
                item,
                chance,
                section.getStringList("matchedBlocks").stream()
                        .map(ContentProviders::getBlock)
                        .collect(Collectors.toSet()),
                section.getStringList("commands"));
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, Treasure object) {
        section.set("id", object.getId());
        section.set("displayName", MiniMessage.miniMessage().serialize(object.getDisplayName()));
        section.set("chance", object.getChance());
        section.set(
                "matchedBlocks",
                object.getMatchedBlocks().stream().map(PackedBlock::getId).toList());
        if (object.getSerializableItemStack() != null) {
            ConfigurationSection cs = section.createSection("item");
            object.getSerializableItemStack().write(cs);
        }
        if (object.getConsoleCommands() != null) {
            section.set("commands", object.getConsoleCommands());
        }
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

    public @Nullable Treasure getTreasure(@Nullable String id) {
        if (id == null) {
            return null;
        }

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
