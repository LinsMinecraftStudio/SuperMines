package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Deserializes {@link IGenerateCondition}s from config sections based on their {@code condition}
 * key. Used to load the built-in conditions and to support nested composite conditions.
 */
public final class ConditionLoader {
    private static final Map<String, Function<ConfigurationSection, IGenerateCondition>> LOADERS = new HashMap<>();

    static {
        LOADERS.put("miney", MineYGenerateCondition::new);
        LOADERS.put("placeholder", PlaceholderGenerateCondition::new);
        LOADERS.put("surface", SurfaceGenerateCondition::new);
        LOADERS.put("border", BorderGenerateCondition::new);
        LOADERS.put("biome", BiomeGenerateCondition::new);
        LOADERS.put("and", AndGenerateCondition::new);
        LOADERS.put("or", OrGenerateCondition::new);
        LOADERS.put("not", NotGenerateCondition::new);
    }

    private ConditionLoader() {}

    /**
     * Registers a custom condition type so it can be deserialized by key.
     *
     * @param key the {@code condition} key used in config
     * @param loader the loader that builds the condition from a config section
     */
    public static void register(
            @NotNull String key, @NotNull Function<ConfigurationSection, IGenerateCondition> loader) {
        Preconditions.checkArgument(!key.isEmpty(), "key cannot be empty");
        Preconditions.checkNotNull(loader, "loader cannot be null");

        LOADERS.put(key.toLowerCase(java.util.Locale.ROOT), loader);
    }

    /**
     * Deserializes a condition from a config section. The section must contain a {@code condition}
     * key that matches a registered condition type.
     *
     * @param cs the config section to read
     * @return the deserialized condition
     * @throws IllegalArgumentException if the {@code condition} key is missing or unknown
     */
    public static IGenerateCondition deserialize(@NotNull ConfigurationSection cs) {
        String key = cs.getString("condition");
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("condition key is missing");
        }

        Function<ConfigurationSection, IGenerateCondition> loader = LOADERS.get(key.toLowerCase(java.util.Locale.ROOT));
        if (loader == null) {
            throw new IllegalArgumentException("Unknown generation condition type: " + key);
        }

        return loader.apply(cs);
    }
}
