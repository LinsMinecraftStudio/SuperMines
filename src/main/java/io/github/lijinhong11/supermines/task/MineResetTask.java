package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

class MineResetTask extends AbstractTask {
    private final Mine mine;
    private final AtomicLong nextResetTime = new AtomicLong();

    MineResetTask(Mine mine) {
        this.mine = mine;
        refreshNextResetTime();
    }

    public long getNextResetTime() {
        return nextResetTime.get();
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        refreshNextResetTime();
        doReset();
    }

    private void doReset() {
        CuboidArea ca = mine.getArea();
        List<BlockPos> blockPosList = ca.asPosList();
        Map<AddonBlock, Double> blockSpawnEntries = mine.getBlockSpawnEntries();
        Map<BlockPos, AddonBlock> generated = new HashMap<>();

        if (blockSpawnEntries.isEmpty()) {
            return;
        }

        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            Material material = loc.getBlock().getType();
            if (mine.isOnlyFillAirWhenRegenerate() && !material.isAir()) continue;

            AddonBlock selected = null;
            for (Map.Entry<AddonBlock, Double> entry : blockSpawnEntries.entrySet()) {
                if (NumberUtils.matchChance(entry.getValue())) {
                    selected = entry.getKey();
                    break;
                }
            }

            if (selected == null && !blockSpawnEntries.isEmpty()) {
                selected = NumberUtils.weightedRandom(blockSpawnEntries);
            }

            generated.put(pos, selected);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (mine.getTeleportLocation() != null && mine.isPlayerInMine(p)) {
                p.teleportAsync(mine.getTeleportLocation());
            }

            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(p, "mine.reset", MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
        }

        TaskMaker tm = SuperMines.getInstance().getTaskMaker();
        if (!mine.isOnlyFillAirWhenRegenerate()) {
            for (BlockPos pos : blockPosList) {
                Location loc = pos.toLocation(mine.getWorld());
                tm.runSync(loc, () -> loc.getBlock().setType(Material.AIR));
            }
        }

        for (Map.Entry<BlockPos, AddonBlock> entry : generated.entrySet()) {
            Location loc = entry.getKey().toLocation(mine.getWorld());
            tm.runSync(loc, () -> entry.getValue().place(loc));
        }

        mine.setBlocksBroken(0);
    }

    public void refreshNextResetTime() {
        this.nextResetTime.set(System.currentTimeMillis() + mine.getRegenerateSeconds() * 1000L);
    }
}
