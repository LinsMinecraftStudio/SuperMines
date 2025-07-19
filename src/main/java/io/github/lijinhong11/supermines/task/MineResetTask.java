package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.pos.BlockPos;
import io.github.lijinhong11.supermines.api.pos.CuboidArea;
import io.github.lijinhong11.supermines.message.MessageReplacement;
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
        Map<Material, Integer> blockSpawnEntries = mine.getBlockSpawnEntries();
        Map<BlockPos, Material> generated = new HashMap<>();
        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            Material material = loc.getBlock().getType();
            if (mine.isOnlyFillAirWhenRegenerate() && !material.isAir()) {
                continue;
            }

            for (Map.Entry<Material, Integer> entry : blockSpawnEntries.entrySet()) {
                if (matchChance(entry.getValue())) {
                    generated.put(pos, entry.getKey());
                    break;
                }
            }
        }

        for (Map.Entry<BlockPos, Material> entry : generated.entrySet()) {
            Location loc = entry.getKey().toLocation(mine.getWorld());
            loc.getBlock().setType(entry.getValue());
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "mine.reset", MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
        }
    }

    private boolean matchChance(int chance) {
        return chance >= 100 || chance >= Math.random() * 100;
    }
}
