package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class TreasureManager extends AbstractFileObjectManager<Treasure> {
    public TreasureManager() {
        super("data/treasures.yml");
    }

    @Override
    protected Treasure deserialize(@NotNull ConfigurationSection section) {
        return null;
    }

    @Override
    protected void serialize(@NotNull ConfigurationSection section, Treasure object) {

    }
}
