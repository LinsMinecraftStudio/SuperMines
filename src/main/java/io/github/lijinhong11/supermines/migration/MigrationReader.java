package io.github.lijinhong11.supermines.migration;

import io.github.lijinhong11.supermines.utils.Constants;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Reads plain YAML without invoking the source plugin's Bukkit deserializers. */
public final class MigrationReader {
    private MigrationReader() {}

    public static MineData read(Reader reader, MigrationSource source) {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Map<?, ?> root = map(new Yaml(new SafeConstructor(options)).load(reader), "document");
        return source == MigrationSource.CATAMINES ? readCataMines(root) : readMineResetLite(root);
    }

    private static MineData readCataMines(Map<?, ?> root) {
        Map<?, ?> mine = map(root.get("Mine"), "Mine (CataMines 2.x)");
        String id = id(mine);
        Map<?, ?> region = map(mine.get("region"), "region");
        if (!"CUBOID".equalsIgnoreCase(text(region, "type", "CUBOID"))) {
            throw new IllegalArgumentException("Unsupported CataMines region type");
        }
        String world = text(region, "world", null);
        Position pos1 = position(map(region.get("p1"), "region.p1"), world);
        Position pos2 = position(map(region.get("p2"), "region.p2"), world);
        Map<String, Double> blocks = new LinkedHashMap<>();
        List<String> notes = new ArrayList<>();
        if (!(mine.get("composition") instanceof List<?> composition)) {
            throw new IllegalArgumentException("Missing composition list");
        }
        for (Object value : composition) {
            Map<?, ?> block = map(value, "composition entry");
            addBlock(blocks, text(block, "block", null), number(block.get("chance"), "chance"));
            if (block.get("lootTable") instanceof List<?> loot && !loot.isEmpty()) {
                addNote(notes, "loot-tables");
            }
        }
        int seconds = seconds(value(mine, "resetDelay", 0), 1);
        String mode = text(mine, "resetMode", "TIME");
        if (!Set.of("TIME", "PERCENTAGE", "TIME_PERCENTAGE").contains(mode)) {
            throw new IllegalArgumentException("Unknown reset mode: " + mode);
        }
        if (!"TIME".equals(mode)) addNote(notes, "percentage-reset");
        if ("PERCENTAGE".equals(mode) || flag(mine, "isStopped", false)) seconds = 0;
        Set<Integer> warnings = new LinkedHashSet<>();
        if (mine.get("warn") instanceof Map<?, ?> warn && flag(warn, "enableWarn", false)) {
            warnings = warnings(warn.get("warnSeconds"), 1, seconds);
        }
        if (number(value(mine, "minEfficiencyLvl", 0), "minEfficiencyLvl") > 0) {
            addNote(notes, "tool-restrictions");
        }
        if (mine.get("teleportResetLocation") != null) addNote(notes, "reset-teleport");
        return new MineData(
                id,
                world,
                pos1,
                pos2,
                blocks,
                seconds,
                flag(mine, "replaceMode", false),
                optionalPosition(mine.get("teleportLocation"), world),
                null,
                warnings,
                notes);
    }

    private static MineData readMineResetLite(Map<?, ?> root) {
        Map<?, ?> mine = map(root.get("mine"), "mine");
        String id = id(mine);
        String world = text(mine, "world", null);
        Position pos1 =
                new Position(world, coordinate(mine, "minX"), coordinate(mine, "minY"), coordinate(mine, "minZ"), 0, 0);
        Position pos2 =
                new Position(world, coordinate(mine, "maxX"), coordinate(mine, "maxY"), coordinate(mine, "maxZ"), 0, 0);
        Map<String, Double> blocks = new LinkedHashMap<>();
        Map<?, ?> composition = map(mine.get("composition"), "composition");
        double total = 0;
        for (Map.Entry<?, ?> entry : composition.entrySet()) {
            double weight = number(entry.getValue(), "composition weight");
            addBlock(blocks, entry.getKey().toString(), weight);
            total += weight;
        }
        if (!Double.isFinite(total)) throw new IllegalArgumentException("Composition total is not finite");
        // MineResetLite stores fractions and pads an incomplete composition with air.
        if (total < 1) blocks.merge("minecraft:air", 1 - total, Double::sum);
        int seconds = seconds(value(mine, "resetDelay", 0), 60);
        List<String> notes = new ArrayList<>();
        String surface = text(mine, "surface", "");
        if (number(value(mine, "resetPercent", -1), "resetPercent") > 0) addNote(notes, "percentage-reset");
        if (mine.get("structure") instanceof List<?> structure && !structure.isEmpty()) addNote(notes, "structure");
        Position teleport = optionalPosition(mine.get("teleportLocation"), world);
        if (teleport == null && mine.containsKey("tpY") && coordinate(mine, "tpY") != -Integer.MAX_VALUE) {
            teleport = new Position(
                    world,
                    coordinate(mine, "tpX"),
                    coordinate(mine, "tpY"),
                    coordinate(mine, "tpZ"),
                    (float) number(value(mine, "tpYaw", 0), "tpYaw"),
                    (float) number(value(mine, "tpPitch", 0), "tpPitch"));
        }
        return new MineData(
                id,
                world,
                pos1,
                pos2,
                blocks,
                seconds,
                flag(mine, "fillMode", false),
                teleport,
                surface.isEmpty() ? null : surface,
                warnings(mine.get("resetWarnings"), 60, seconds),
                notes);
    }

    private static void addNote(List<String> notes, String note) {
        if (!notes.contains(note)) notes.add(note);
    }

    private static String id(Map<?, ?> mine) {
        String id = text(mine, "name", null);
        if (!id.matches(Constants.ID_PATTERN)) throw new IllegalArgumentException("Invalid mine ID: " + id);
        return id;
    }

    private static void addBlock(Map<String, Double> blocks, String block, double weight) {
        if (weight < 0) throw new IllegalArgumentException("Negative block weight: " + block);
        if (weight == 0) return;
        blocks.merge(block, weight, Double::sum);
        if (!Double.isFinite(blocks.get(block))) throw new IllegalArgumentException("Block weight is not finite");
    }

    private static Set<Integer> warnings(Object value, int multiplier, int interval) {
        Set<Integer> warnings = new LinkedHashSet<>();
        if (value == null) return warnings;
        if (!(value instanceof List<?> list)) throw new IllegalArgumentException("Invalid warning list");
        for (Object entry : list) {
            int seconds = seconds(entry, multiplier);
            if (seconds > 0 && seconds < interval) warnings.add(seconds);
        }
        return warnings;
    }

    private static int seconds(Object value, int multiplier) {
        double duration = number(value, "reset time");
        double seconds = duration * multiplier;
        if (duration != Math.rint(duration) || seconds < 0 || seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid reset time: " + value);
        }
        return (int) seconds;
    }

    private static Position optionalPosition(Object value, String world) {
        return value == null ? null : position(map(value, "location"), world);
    }

    private static Position position(Map<?, ?> map, String defaultWorld) {
        return new Position(
                text(map, "world", defaultWorld),
                number(map.get("x"), "x"),
                number(map.get("y"), "y"),
                number(map.get("z"), "z"),
                (float) number(value(map, "yaw", 0), "yaw"),
                (float) number(value(map, "pitch", 0), "pitch"));
    }

    private static Object value(Map<?, ?> map, String key, Object fallback) {
        return map.containsKey(key) ? map.get(key) : fallback;
    }

    private static int coordinate(Map<?, ?> map, String key) {
        double value = number(map.get(key), key);
        if (value != Math.rint(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid coordinate: " + key);
        }
        return (int) value;
    }

    private static Map<?, ?> map(Object value, String label) {
        if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("Missing or invalid " + label);
        return map;
    }

    private static String text(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        if (value == null) {
            if (fallback == null) throw new IllegalArgumentException("Missing " + key);
            return fallback;
        }
        String text = value.toString();
        if (text.isBlank() && !"".equals(fallback)) throw new IllegalArgumentException("Empty " + key);
        return text;
    }

    private static double number(Object value, String label) {
        try {
            double number = Double.parseDouble(String.valueOf(value));
            if (Double.isFinite(number)) return number;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Invalid " + label + ": " + value);
    }

    private static boolean flag(Map<?, ?> map, String key, boolean fallback) {
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
            List<String> notes) {}
}
