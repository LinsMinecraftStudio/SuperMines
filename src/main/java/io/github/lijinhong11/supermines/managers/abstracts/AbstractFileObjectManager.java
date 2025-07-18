package io.github.lijinhong11.supermines.managers.abstracts;

import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractFileObjectManager<T> {
    private final File configFile;
    private final YamlConfiguration config;

    public AbstractFileObjectManager(@NotNull String configPath) {
        if (Strings.isNullOrEmpty(configPath)) {
            throw new IllegalArgumentException("configPath cannot be null or empty");
        }

        File file = new File(SuperMines.getInstance().getDataFolder(), configPath);
        if (!file.exists()) {
            SuperMines.getInstance().saveResource(configPath, false);
        }

        this.configFile = file;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public @NotNull final List<T> getAll() {
        Set<String> keys = config.getKeys(false);
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys.stream().map(this::deserialize).toList();
    }

    public final void remove(@NotNull String key) {
        config.set(key, null);

        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final T deserialize(@NotNull String key) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section == null) {
            return null;
        }

        return deserialize(section);
    }

    protected abstract T deserialize(@NotNull ConfigurationSection section);

    public final void serialize(String key, T object) {
        ConfigurationSection section = config.createSection(key);
        serialize(section, object);

        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void serialize(@NotNull ConfigurationSection section, T object);
}
