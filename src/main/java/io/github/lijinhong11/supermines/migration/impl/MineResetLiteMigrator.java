package io.github.lijinhong11.supermines.migration.impl;

import io.github.lijinhong11.supermines.api.mine.MessageBroadcastMode;
import io.github.lijinhong11.supermines.migration.AbstractMineMigrator;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts MineResetLite mine settings into the shared migration data model.
 */
public final class MineResetLiteMigrator extends AbstractMineMigrator {

    /**
     * Reads MRL's global broadcast flags once for all mines in a migration.
     * Missing configuration uses the original plugin's defaults.
     */
    @Override
    public Map<?, ?> readConfiguration(Path dataFolder) throws IOException {
        Path config = dataFolder.resolve("config.yml");
        if (Files.notExists(config)) return Map.of();
        try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            Map<?, ?> values = readDocument(reader);
            flag(values, "broadcast-nearby-only", false);
            flag(values, "broadcast-in-world-only", false);
            return values;
        }
    }

    @Override
    protected MineData readMine(Map<?, ?> root, Map<?, ?> config) {
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
        MessageBroadcastMode mode = messageMode(mine, config);
        var resetWarnings = warnings(mine.get("resetWarnings"), 60, seconds);
        if (mode == MessageBroadcastMode.SILENT && !resetWarnings.isEmpty()) addNote(notes, "silent-warnings");
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
                resetWarnings,
                mode,
                notes);
    }

    private static MessageBroadcastMode messageMode(Map<?, ?> mine, Map<?, ?> config) {
        if (flag(mine, "isSilent", false)) return MessageBroadcastMode.SILENT;
        // MRL's "nearby" option checks mine boundaries and takes priority over the world flag.
        if (flag(config, "broadcast-nearby-only", false)) return MessageBroadcastMode.IN_MINE;
        if (flag(config, "broadcast-in-world-only", false)) return MessageBroadcastMode.IN_WORLD;
        return MessageBroadcastMode.ALL;
    }
}
