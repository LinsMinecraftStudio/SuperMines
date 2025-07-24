package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MineResetTask extends AbstractTask {
    private final Mine mine;

    MineResetTask(Mine mine) {
        this.mine = mine;
    }

    @Override
    public void run(WrappedTask wrappedTask) {
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

        TaskMaker tm = SuperMines.getInstance().getTaskMaker();
        for (BlockPos pos : mine.getArea().asPosList()) {
            Location loc = pos.toLocation(mine.getWorld());
            tm.runSync(loc, () -> loc.getBlock().setType(Material.AIR));
        }

        for (Map.Entry<BlockPos, Material> entry : generated.entrySet()) {
            Location loc = entry.getKey().toLocation(mine.getWorld());
            tm.runSync(loc, () -> loc.getBlock().setType(entry.getValue()));
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "mine.reset", MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
        }
    }
}
