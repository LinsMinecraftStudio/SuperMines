package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.events.MineResetEvent;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.integrates.skills.SkillsBlockPlace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

class MineResetTask extends AbstractTask {
    private final Mine mine;
    private final boolean manualReset;
    private final AtomicLong nextResetTime = new AtomicLong();

    MineResetTask(Mine mine) {
        this(mine, false);
    }

    MineResetTask(Mine mine, boolean manualReset) {
        this.mine = mine;
        this.manualReset = manualReset;
        refreshNextResetTime();
    }

    public long getNextResetTime() {
        return nextResetTime.get();
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        if (!manualReset && mine.getRegenerateSeconds() < 1) {
            cancel();
            return;
        }

        doReset();
    }

    private void doReset() {
        List<BlockPos> blockPosList = mine.getArea().asPosList();
        WeightedRandomMap<PackedBlock> blockSpawnEntries = mine.getBlockSpawnEntries();
        Map<BlockPos, PackedBlock> generated = new HashMap<>();
        List<BlockPos> toDestroy = new ArrayList<>();

        if (blockSpawnEntries.isEmpty()) {
            finishReset();
            return;
        }

        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            Material material = loc.getBlock().getType();
            if (mine.isOnlyFillAirWhenRegenerate() && !material.isAir()) continue;

            PackedBlock selected = blockSpawnEntries.randomOne();
            generated.put(pos, selected);
            if (!material.isAir()) {
                toDestroy.add(pos);
            }
        }

        if (!mine.isOnlyFillAirWhenRegenerate() || !toDestroy.isEmpty()) {
            runDestroyPhase(toDestroy, generated);
            return;
        }

        runPlacePhase(generated);
    }

    private void runDestroyPhase(List<BlockPos> blockPosList, Map<BlockPos, PackedBlock> generated) {
        TaskMaker tm = SuperMines.getInstance().getTaskMaker();
        if (blockPosList.isEmpty()) {
            runPlacePhase(generated);
            return;
        }

        AtomicInteger pending = new AtomicInteger(blockPosList.size());
        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            tm.runSync(loc, () -> {
                ContentProviders.destroyBlock(loc);
                if (pending.decrementAndGet() == 0) {
                    runPlacePhase(generated);
                }
            });
        }
    }

    private void runPlacePhase(Map<BlockPos, PackedBlock> generated) {
        TaskMaker tm = SuperMines.getInstance().getTaskMaker();
        if (generated.isEmpty()) {
            finishReset();
            return;
        }

        AtomicInteger pending = new AtomicInteger(generated.size());
        for (Map.Entry<BlockPos, PackedBlock> entry : generated.entrySet()) {
            Location loc = entry.getKey().toLocation(mine.getWorld());

            tm.runSync(loc, () -> {
                if (!loc.getBlock().getType().isAir()) {
                    ContentProviders.destroyBlock(loc);
                }

                entry.getValue().place(loc);
                SkillsBlockPlace.markAsEarnable(loc);
                if (pending.decrementAndGet() == 0) {
                    finishReset();
                }
            });
        }
    }

    private void finishReset() {
        boolean broadcast = SuperMines.getInstance().getConfig().getBoolean("mine.broadcast-reset-messages", true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (mine.isPlayerInMine(p)) {
                p.teleportAsync(mine.getTeleportLocation() != null ? mine.getTeleportLocation() : mine.getSafeTopLocation());
            }

            if (broadcast || mine.isPlayerInMine(p)) {
                SuperMines.getInstance()
                        .getLanguageManager()
                        .sendMessage(p, "mine.reset", MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
            }
        }

        mine.setBlocksBroken(0);
        refreshNextResetTime();
        new MineResetEvent(mine).callEvent();
    }

    public void refreshNextResetTime() {
        this.nextResetTime.set(System.currentTimeMillis() + mine.getRegenerateSeconds() * 1000L);
    }
}
