package io.github.lijinhong11.supermines.task;

import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.events.MineResetEvent;
import io.github.lijinhong11.supermines.api.events.MineResetStartEvent;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.integrates.skills.SkillsBlockPlace;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

class MineResetTask extends AbstractTask {
    private final Mine mine;
    private final boolean manualReset;
    private final long generation;
    private final AtomicLong nextResetTime = new AtomicLong();

    MineResetTask(Mine mine) {
        this(mine, false, 0L);
    }

    MineResetTask(Mine mine, boolean manualReset, long generation) {
        this.mine = mine;
        this.manualReset = manualReset;
        this.generation = generation;
        refreshNextResetTime();
    }

    public long getNextResetTime() {
        return nextResetTime.get();
    }

    @Override
    public void run(ScheduledTask ScheduledTask) {
        if (!manualReset && mine.getRegenerateSeconds() < 1) {
            cancel();
            return;
        }

        TaskMaker taskMaker = SuperMines.getInstance().getTaskMaker();
        if (!taskMaker.tryBeginReset(mine.getId(), generation)) return;
        if (!manualReset) refreshNextResetTime();
        try {
            doReset();
        } catch (RuntimeException exception) {
            SuperMines.getInstance().getLogger().severe("Mine reset failed for '" + mine.getId() + "': " + exception);
            taskMaker.finishReset(mine.getId(), generation);
        }
    }

    private void doReset() {
        MineResetStartEvent event = new MineResetStartEvent(mine, manualReset);
        event.callEvent();
        if (event.isCancelled()) {
            SuperMines.getInstance().getTaskMaker().finishReset(mine.getId(), generation);
            return;
        }

        List<BlockPos> blockPosList = mine.getArea().asPosList();
        WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries = new WeightedRandomMap<>(mine.getBlockSpawnEntries());
        if (blockSpawnEntries.isEmpty()) {
            finishReset();
            return;
        }

        scanBlocks(blockPosList, blockSpawnEntries);
    }

    private void scanBlocks(List<BlockPos> blockPosList, WeightedRandomMap<BlockSpawnEntry> blockSpawnEntries) {
        if (blockPosList.isEmpty()) {
            finishReset();
            return;
        }

        Map<BlockPos, PackedBlock> generated = new ConcurrentHashMap<>();
        List<BlockPos> toDestroy = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(blockPosList.size());
        TaskMaker tm = SuperMines.getInstance().getTaskMaker();

        for (BlockPos pos : blockPosList) {
            Location loc = pos.toLocation(mine.getWorld());
            if (!tm.runSync(loc, () -> {
                if (!tm.isResetGenerationActive(mine.getId(), generation)) return;
                try {
                    Material material = loc.getBlock().getType();
                    if (!mine.isOnlyFillAirWhenRegenerate() || material.isAir()) {
                        BlockSpawnEntry selected = selectEntry(blockSpawnEntries, pos);
                        if (selected != null) {
                            generated.put(pos, selected);
                        }

                        if (!material.isAir()) {
                            toDestroy.add(pos);
                        }
                    }
                } catch (RuntimeException exception) {
                    SuperMines.getInstance()
                            .getLogger()
                            .warning("Failed to scan block " + pos + " in mine '" + mine.getId() + "': " + exception);
                } finally {
                    if (pending.decrementAndGet() == 0 && tm.isResetGenerationActive(mine.getId(), generation)) {
                        if (!mine.isOnlyFillAirWhenRegenerate() || !toDestroy.isEmpty()) {
                            runDestroyPhase(toDestroy, generated);
                        } else {
                            runPlacePhase(generated);
                        }
                    }
                }
            })) {
                tm.abortReset(mine.getId(), generation);
                return;
            }
        }
    }

    private BlockSpawnEntry selectEntry(WeightedRandomMap<BlockSpawnEntry> entries, BlockPos pos) {
        WeightedRandomMap<BlockSpawnEntry> passing = new WeightedRandomMap<>();
        for (BlockSpawnEntry entry : entries.keySet()) {
            if (entry.canGenerate(mine, pos)) {
                passing.put(entry, entries.getWeight(entry));
            }
        }

        return passing.isEmpty() ? null : passing.randomOne();
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
            if (!tm.runSync(loc, () -> {
                if (!tm.isResetGenerationActive(mine.getId(), generation)) return;
                try {
                    ContentProviders.destroyBlock(loc);
                } catch (RuntimeException exception) {
                    SuperMines.getInstance()
                            .getLogger()
                            .warning(
                                    "Failed to destroy block " + pos + " in mine '" + mine.getId() + "': " + exception);
                } finally {
                    if (pending.decrementAndGet() == 0 && tm.isResetGenerationActive(mine.getId(), generation)) {
                        runPlacePhase(generated);
                    }
                }
            })) {
                tm.abortReset(mine.getId(), generation);
                return;
            }
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

            if (!tm.runSync(loc, () -> {
                if (!tm.isResetGenerationActive(mine.getId(), generation)) return;
                try {
                    if (!loc.getBlock().getType().isAir()) {
                        ContentProviders.destroyBlock(loc);
                    }

                    entry.getValue().place(loc);
                    SkillsBlockPlace.markAsEarnable(loc);
                } catch (RuntimeException exception) {
                    SuperMines.getInstance()
                            .getLogger()
                            .warning("Failed to place block " + entry.getKey() + " in mine '" + mine.getId() + "': "
                                    + exception);
                } finally {
                    if (pending.decrementAndGet() == 0 && tm.isResetGenerationActive(mine.getId(), generation)) {
                        finishReset();
                    }
                }
            })) {
                tm.abortReset(mine.getId(), generation);
                return;
            }
        }
    }

    private void finishReset() {
        Bukkit.getGlobalRegionScheduler().run(SuperMines.getInstance(), task -> {
            TaskMaker taskMaker = SuperMines.getInstance().getTaskMaker();
            if (!taskMaker.isResetGenerationActive(mine.getId(), generation)) return;
            boolean broadcast = SuperMines.getInstance().getConfig().getBoolean("mine.broadcast-reset-messages", true);
            try {
                mine.setBlocksBroken(0);
                new MineResetEvent(mine).callEvent();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getScheduler()
                            .run(
                                    SuperMines.getInstance(),
                                    playerTask -> {
                                        boolean inside = mine.isPlayerInMine(player);
                                        if (inside) {
                                            player.teleportAsync(
                                                    mine.getTeleportLocation() != null
                                                            ? mine.getTeleportLocation()
                                                            : mine.getSafeTopLocation());
                                        }

                                        if (broadcast || inside) {
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            player,
                                                            "mine.reset",
                                                            MessageReplacement.replace(
                                                                    "%mine%", mine.getRawDisplayName()));
                                        }
                                    },
                                    null);
                }
            } finally {
                taskMaker.completeReset(mine, generation, manualReset);
            }
        });
    }

    public void refreshNextResetTime() {
        this.nextResetTime.set(System.currentTimeMillis() + mine.getRegenerateSeconds() * 1000L);
    }
}
