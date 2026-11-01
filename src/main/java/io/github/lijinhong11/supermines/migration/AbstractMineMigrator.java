package io.github.lijinhong11.supermines.migration;

import io.github.lijinhong11.supermines.api.mine.MessageBroadcastMode;
import io.github.lijinhong11.supermines.utils.Constants;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Provides shared YAML reading and validation for source-specific mine migrators.
 * Source plugin Bukkit deserializers are never invoked.
 */
public abstract class AbstractMineMigrator {
    protected AbstractMineMigrator() {}

    public final MineData read(Reader reader) {
        return read(reader, Map.of());
    }

    public final MineData read(Reader reader, Map<?, ?> sourceConfig) {
        return readMine(readDocument(reader), sourceConfig);
    }

    protected abstract MineData readMine(Map<?, ?> root, Map<?, ?> sourceConfig);

    /**
     * Supplies an empty configuration for sources without global migration settings.
     * Subclasses can override this to read and validate their source configuration.
     */
    public Map<?, ?> readConfiguration(Path dataFolder) throws IOException {
        return Map.of();
    }

    protected static Map<?, ?> readDocument(Reader reader) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Object root = new Yaml(new SafeConstructor(options)).load(reader);
        return root == null ? Map.of() : map(root, "document");
    }

    protected static void addNote(List<String> notes, String note) {
        if (!notes.contains(note)) notes.add(note);
    }

    protected static String id(Map<?, ?> mine) {
        String id = text(mine, "name", null);
        if (!id.matches(Constants.ID_PATTERN)) throw new IllegalArgumentException("Invalid mine ID: " + id);
        return id;
    }

    protected static void addBlock(Map<String, Double> blocks, String block, double weight) {
        if (weight < 0) throw new IllegalArgumentException("Negative block weight: " + block);
        if (weight == 0) return;
        blocks.merge(block, weight, Double::sum);
        if (!Double.isFinite(blocks.get(block))) throw new IllegalArgumentException("Block weight is not finite");
    }

    protected static Set<Integer> warnings(Object value, int multiplier, int interval) {
        Set<Integer> warnings = new LinkedHashSet<>();
        if (value == null) return warnings;
        if (!(value instanceof List<?> list)) throw new IllegalArgumentException("Invalid warning list");
        for (Object entry : list) {
            int seconds = seconds(entry, multiplier);
            if (seconds > 0 && seconds < interval) warnings.add(seconds);
        }
        return warnings;
    }

    protected static int seconds(Object value, int multiplier) {
        double duration = number(value, "reset time");
        double seconds = duration * multiplier;
        if (duration != Math.rint(duration) || seconds < 0 || seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid reset time: " + value);
        }
        return (int) seconds;
    }

    protected static Position optionalPosition(Object value, String world) {
        return value == null ? null : position(map(value, "location"), world);
    }

    protected static Position position(Map<?, ?> map, String defaultWorld) {
        return new Position(
                text(map, "world", defaultWorld),
                number(map.get("x"), "x"),
                number(map.get("y"), "y"),
                number(map.get("z"), "z"),
                (float) number(value(map, "yaw", 0), "yaw"),
                (float) number(value(map, "pitch", 0), "pitch"));
    }

    protected static Object value(Map<?, ?> map, String key, Object fallback) {
        return map.containsKey(key) ? map.get(key) : fallback;
    }

    protected static int coordinate(Map<?, ?> map, String key) {
        double value = number(map.get(key), key);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid coordinate: " + key);
        }
        return (int) value;
    }

    protected static Map<?, ?> map(Object value, String label) {
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("Missing or invalid " + label);
        return map;
    }

    protected static String text(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        if (value == null) {
            if (fallback == null) throw new IllegalArgumentException("Missing " + key);
            return fallback;
        }
        String text = value.toString();
        if (text.isBlank() && !"".equals(fallback)) throw new IllegalArgumentException("Empty " + key);
        return text;
    }

    protected static double number(Object value, String label) {
        try {
            double number = Double.parseDouble(String.valueOf(value));
            if (Double.isFinite(number)) return number;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Invalid " + label + ": " + value);
    }

    protected static boolean flag(Map<?, ?> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof Boolean flag) return flag;
        throw new IllegalArgumentException("Invalid boolean: " + key);
    }

    public record Position(String world, double x, double y, double z, float yaw, float pitch) {}

    public record MineData(
            String id,
            String world,
            Position pos1,
            Position pos2,
            Map<String, Double> blocks,
            int resetSeconds,
            boolean onlyFillAir,
            Position teleport,
            String surface,
            Set<Integer> warnings,
            MessageBroadcastMode messageBroadcastMode,
            List<String> notes) {}
}
