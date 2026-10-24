package io.github.lijinhong11.supermines.migration;

import com.sk89q.worldedit.world.registry.LegacyMapper;

/** Loaded only when WorldEdit or FAWE is available. */
final class LegacyMineBlocks {
    private LegacyMineBlocks() {}

    static String resolve(String id) {
        String[] parts = id.split(":");
        var block = LegacyMapper.getInstance()
                .getBlockFromLegacy(Integer.parseInt(parts[0]), parts.length == 2 ? Integer.parseInt(parts[1]) : 0);
        if (block == null) throw new IllegalArgumentException("Unknown legacy block: " + id);
        return block.getAsString();
    }
}
