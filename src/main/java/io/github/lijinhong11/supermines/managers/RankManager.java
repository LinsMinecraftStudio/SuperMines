package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RankManager extends AbstractFileObjectManager<Rank> {
    private final Map<String, Rank> ranks = new HashMap<>();

    public RankManager() {
        super("data/ranks.yml");

        load();
    }

    private void load() {
        for (Rank object : super.getAll()) {
            ranks.put(object.getId(), object);
        }
    }

    @Override
    protected Rank getObject(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();
        String displayName = section.getString("displayName", id);
        return new Rank(section.getInt("level", 1), id, ComponentUtils.deserialize(displayName));
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, Rank object) {
        section.set("level", object.getLevel());
        section.set("displayName", ComponentUtils.serialize(object.getDisplayName()));
    }

    @Override
    public void saveAndClose() {
        for (Rank rank : ranks.values()) {
            super.putObject(rank.getId(), rank);
        }
    }

    public void addRank(@NotNull Rank rank) {
        if (ranks.containsKey(rank.getId())) {
            throw new IllegalArgumentException("Rank with ID " + rank.getId() + " already exists");
        }

        ranks.put(rank.getId(), rank);
        super.putObject(rank.getId(), rank);
    }

    public @Nullable Rank getRank(@Nullable String id) {
        if (id == null) {
            return null;
        }

        return ranks.get(id);
    }

    public void removeRank(@NotNull String id) {
        if (!ranks.containsKey(id)) {
            throw new IllegalArgumentException("Rank with ID " + id + " does not exist");
        }

        ranks.remove(id);
        super.remove(id);
    }

    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }

    public Set<String> getAllRankIds() {
        return ranks.keySet();
    }
}
