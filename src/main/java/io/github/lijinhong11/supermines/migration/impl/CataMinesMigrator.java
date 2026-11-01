package io.github.lijinhong11.supermines.migration.impl;

import io.github.lijinhong11.supermines.migration.AbstractMineMigrator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts CataMines 2.x mine settings into the shared migration data model.
 */
public final class CataMinesMigrator extends AbstractMineMigrator {
    @Override
    protected MineData readMine(Map<?, ?> root, Map<?, ?> sourceConfig) {
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
                null,
                notes);
    }
}
