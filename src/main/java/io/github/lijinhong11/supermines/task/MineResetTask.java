package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

class MineResetTask extends AbstractTask {
    private final Mine mine;
    private long nextResetTime;

    MineResetTask(Mine mine) {
        this.mine = mine;
    }

    public long getNextResetTime() {
        return nextResetTime;
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        refreshNextResetTime();

        CuboidArea ca = mine.getArea();
        List<BlockPos> blockPosList = ca.asPosList();
        Map<Material, Double> blockSpawnEntries = mine.getBlockSpawnEntries();
        Map<BlockPos, Material> generated = new HashMap<>();
        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            Material material = loc.getBlock().getType();
            if (mine.isOnlyFillAirWhenRegenerate() && !material.isAir()) {
                continue;
            }

            for (Map.Entry<Material, Double> entry : blockSpawnEntries.entrySet()) {
                if (NumberUtils.matchChance(entry.getValue())) {
                    generated.put(pos, entry.getKey());
                    break;
                }
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (mine.getTeleportLocation() != null) {
                if (mine.isPlayerInMine(p)) {
                    p.teleportAsync(mine.getTeleportLocation());
                }
            }

            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(p, "mine.reset", MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
        }

        TaskMaker tm = SuperMines.getInstance().getTaskMaker();
        for (BlockPos pos : mine.getArea().asPosList()) {
            Location loc = pos.toLocation(mine.getWorld());
            tm.runSync(loc, () -> loc.getBlock().setType(Material.AIR));
        }

        for (Map.Entry<BlockPos, Material> entry : generated.entrySet()) {
            Location loc = entry.getKey().toLocation(mine.getWorld());
            tm.runSync(loc, () -> loc.getBlock().setType(entry.getValue()));
        }

        mine.setBlocksBroken(0);
    }

    public void refreshNextResetTime() {
        this.nextResetTime = System.currentTimeMillis() + mine.getRegenerateSeconds() * 1000L;
    }
}
