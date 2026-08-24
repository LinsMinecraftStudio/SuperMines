package io.github.lijinhong11.supermines.managers.abstracts;

import com.google.common.base.Strings;
import io.github.lijinhong11.supermines.SuperMines;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractFileObjectManager<T> {
    private final File configFile;
    private YamlConfiguration config;

    protected AbstractFileObjectManager(@NotNull String configPath) {
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

    @NotNull protected final List<T> getAll() {
        Set<String> keys = config.getKeys(false);
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys.stream().map(this::getObject).toList();
    }

    protected final void remove(@NotNull String key) {
        config.set(key, null);

        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final T getObject(@NotNull String key) {
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section == null) {
            return null;
        }

        return getObject(section);
    }

    protected final ConfigurationSection getConfigurationSection(@NotNull String key) {
        return config.getConfigurationSection(key);
    }

    protected abstract T getObject(@NotNull ConfigurationSection section);

    protected final void putObject(String key, T object) {
        writeObject(key, object);
        saveConfig();
    }

    /**
     * Writes the object into the in-memory configuration without saving to the
     * disk. Call {@link #saveConfig()} to flush all pending writes at once.
     */
    protected final void writeObject(String key, T object) {
        ConfigurationSection section = config.createSection(key);
        putObject(section, object);
    }

    /**
     * Saves the in-memory configuration to the disk. Useful to batch several
     * {@link #writeObject(String, Object)} calls into a single disk write.
     */
    protected final void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected final void reloadConfiguration() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    protected abstract void putObject(@NotNull ConfigurationSection section, T object);

    public abstract void saveAndClose();
}
