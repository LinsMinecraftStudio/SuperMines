package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class RankManager extends AbstractFileObjectManager<Rank> {
    protected RankManager() {
        super("data/ranks.yml");
    }

    @Override
    protected Rank getObject(@NotNull ConfigurationSection section) {
        return null;
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, Rank object) {

    }
}
