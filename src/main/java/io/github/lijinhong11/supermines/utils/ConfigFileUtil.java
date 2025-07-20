package io.github.lijinhong11.supermines.utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.plugin.Plugin;

public class ConfigFileUtil {
    private ConfigFileUtil() {}

    /**
     * Complete configuration(key and value, comments, etc)
     *
     * @param resourceFile the resource file you want to complete
     */
    public static void completeFile(Plugin plugin, String resourceFile) {
        if (plugin == null) {
            return;
        }

        InputStream stream = plugin.getResource(resourceFile);
        File file = new File(plugin.getDataFolder(), resourceFile);
        if (!file.exists()) {
            if (stream != null) {
                plugin.saveResource(resourceFile, false);
                return;
            }
            plugin.getLogger().warning("File completion of '" + resourceFile + "' is failed.");
            return;
        }

        if (stream == null) {
            plugin.getLogger().warning("File completion of '" + resourceFile + "' is failed.");
            return;
        }

        try {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
            YamlConfiguration configuration2 = YamlConfiguration.loadConfiguration(file);


            for (String key : configuration.getKeys(true)) {
                Object value = configuration.get(key);
                if (value instanceof List<?>) {
                    List<?> list2 = configuration2.getList(key);
                    if (list2 == null) {
                        configuration2.set(key, value);
                        continue;
                    }
                }

                if (!configuration2.contains(key)) {
                    configuration2.set(key, value);
                }

                if (!configuration.getComments(key).equals(configuration2.getComments(key))) {
                    configuration2.setComments(key, configuration.getComments(key));
                }

                YamlConfigurationOptions options1 = configuration.options();
                YamlConfigurationOptions options2 = configuration2.options();

                if (!options2.getHeader().equals(options1.getHeader())) {
                    options2.setHeader(options1.getHeader());
                }
            }

            configuration2.save(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "File completion of '" + resourceFile + "' is failed.", e);
        }
    }

    /**
     * Complete language file (keys and values, comments, etc.) FORCE SYNC
     *
     * @param plugin plugin instance
     * @param resourceFile the language file you want to complete
     */
    public static void completeLangFile(Plugin plugin, String resourceFile) {
        InputStream stream = plugin.getResource(resourceFile);
        File file = new File(plugin.getDataFolder(), resourceFile);

        if (!file.exists()) {
            if (stream != null) {
                plugin.saveResource(resourceFile, false);
                return;
            }
            plugin.getLogger().warning("File completion of '" + resourceFile + "' is failed.");
            return;
        }

        if (stream == null) {
            plugin.getLogger().warning("File completion of '" + resourceFile + "' is failed.");
            return;
        }

        try {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
            YamlConfiguration configuration2 = YamlConfiguration.loadConfiguration(file);

            Set<String> keys = configuration.getKeys(true);
            for (String key : keys) {
                Object value = configuration.get(key);
                if (value instanceof List<?>) {
                    List<?> list2 = configuration2.getList(key);
                    if (list2 == null) {
                        configuration2.set(key, value);
                        continue;
                    }
                }
                if (!configuration2.contains(key)) {
                    configuration2.set(key, value);
                }
                if (!configuration.getComments(key).equals(configuration2.getComments(key))) {
                    configuration2.setComments(key, configuration.getComments(key));
                }
            }
            for (String key : configuration2.getKeys(true)) {
                if (configuration2.contains(key) & !configuration.contains(key)) {
                    configuration2.set(key, null);
                }
            }
            configuration2.save(file);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "File completion of '" + resourceFile + "' is failed.", e);
        }
    }
}