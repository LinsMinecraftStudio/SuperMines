package io.github.lijinhong11.supermines.task;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TaskMaker {
    public static final long MAX_DELAY_SECONDS = 86400L;
    private final Map<String, MineResetTask> resetTasks;
    private final Map<String, Map<Integer, MineResetWarningTask>> resetWarningTasks;
    private final Map<String, AtomicBoolean> resetInProgress = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> resetGenerations = new ConcurrentHashMap<>();
    private volatile boolean closing;

    public TaskMaker() {
        resetTasks = new ConcurrentHashMap<>();
        resetWarningTasks = new ConcurrentHashMap<>();
    }

    public void startup() {
        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            startMineTasks(mine);
        }
    }

    public void startMineTasks(Mine mine) {
        if (mine.getRegenerateSeconds() < 1) return;
        startMineResetTask(mine);
        for (int second : mine.getWarningSeconds()) {
            startMineWarningTask(mine, second);
        }
    }

    public void runSync(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(SuperMines.getInstance(), t -> runnable.run());
    }

    public boolean runSync(Location loc, Runnable runnable) {
        if (loc.getWorld() == null) {
            return false;
        }

        try {
            Bukkit.getRegionScheduler().run(SuperMines.getInstance(), loc, t -> runnable.run());
            return true;
        } catch (RuntimeException exception) {
            SuperMines.getInstance().getLogger().warning("Failed to schedule region task at " + loc + ": " + exception);
            return false;
        }
    }

    public void runSync(Player player, Runnable runnable) {
        player.getScheduler().run(SuperMines.getInstance(), t -> runnable.run(), null);
    }

    public ScheduledTask runSyncDelayed(Location loc, long delayTicks, Runnable runnable) {
        if (loc.getWorld() == null) {
            return null;
        }

        return Bukkit.getRegionScheduler().runDelayed(SuperMines.getInstance(), loc, t -> runnable.run(), delayTicks);
    }

    public void startMineWarningTask(Mine mine, int warningSeconds) {
        MineResetTask resetTask = resetTasks.get(mine.getId());

        if (resetTask == null) {
            return;
        }

        Map<Integer, MineResetWarningTask> warningMap =
                resetWarningTasks.computeIfAbsent(mine.getId(), id -> new ConcurrentHashMap<>());

        if (warningSeconds < 1 || warningSeconds >= mine.getRegenerateSeconds()) return;
        MineResetWarningTask task = new MineResetWarningTask(mine, warningSeconds);
        if (warningMap.putIfAbsent(warningSeconds, task) != null) return;

        long delayMillis = resetTask.getNextResetTime() - System.currentTimeMillis() - warningSeconds * 1000L;
        if (delayMillis <= 0) {
            delayMillis = safeMillis(mine.getRegenerateSeconds());
        }

        try {
            ScheduledTask handle = Bukkit.getGlobalRegionScheduler()
                    .runAtFixedRate(
                            SuperMines.getInstance(),
                            task,
                            toTicks(delayMillis),
                            safeTicks(mine.getRegenerateSeconds()));
            task.bind(handle);
        } catch (RuntimeException exception) {
            warningMap.remove(warningSeconds, task);
            throw exception;
        }
    }

    public void startMineResetTask(Mine mine) {
        if (closing || mine.getRegenerateSeconds() < 1) return;
        MineResetTask existing = resetTasks.remove(mine.getId());
        if (existing != null) existing.cancel();
        resetInProgress.computeIfAbsent(mine.getId(), id -> new AtomicBoolean()).set(false);
        long generation = resetGenerations
                .computeIfAbsent(mine.getId(), id -> new AtomicLong())
                .incrementAndGet();
        MineResetTask task = new MineResetTask(mine, false, generation);
        resetTasks.put(mine.getId(), task);
        long periodTicks = safeTicks(mine.getRegenerateSeconds());
        ScheduledTask handle = Bukkit.getGlobalRegionScheduler()
                .runAtFixedRate(SuperMines.getInstance(), task, periodTicks, periodTicks);
        task.bind(handle);
    }

    public void runMineResetTaskNow(Mine mine) {
        if (closing) return;
        long generation = resetGenerations
                .computeIfAbsent(mine.getId(), id -> new AtomicLong())
                .get();
        MineResetTask task = new MineResetTask(mine, true, generation);
        ScheduledTask handle = Bukkit.getGlobalRegionScheduler().run(SuperMines.getInstance(), task);
        task.bind(handle);
    }

    public void cancelMineResetTask(Mine mine) {
        resetGenerations.computeIfAbsent(mine.getId(), id -> new AtomicLong()).incrementAndGet();
        resetInProgress.computeIfAbsent(mine.getId(), id -> new AtomicBoolean()).set(false);
        MineResetTask task = resetTasks.get(mine.getId());
        if (task != null) {
            task.cancel();
        }

        cancelMineResetWarningTasks(mine);

        resetTasks.remove(mine.getId());
    }

    public long getMineUntilResetTime(Mine mine) {
        MineResetTask task = resetTasks.get(mine.getId());
        if (task == null) {
            return -1;
        }

        return Math.max(0, task.getNextResetTime() - System.currentTimeMillis());
    }

    public void cancelMineWarningTask(Mine mine, int restSeconds) {
        Map<Integer, MineResetWarningTask> warningMap = resetWarningTasks.get(mine.getId());
        if (warningMap == null) return;
        MineResetWarningTask task = warningMap.remove(restSeconds);
        if (task != null) {
            task.cancel();
        }
        if (warningMap.isEmpty()) resetWarningTasks.remove(mine.getId(), warningMap);
    }

    public void restartMineResetTask(Mine mine) {
        cancelMineResetTask(mine);

        startMineResetTask(mine);
        for (int second : mine.getWarningSeconds()) {
            startMineWarningTask(mine, second);
        }
    }

    private void cancelMineResetWarningTasks(Mine mine) {
        Map<Integer, MineResetWarningTask> tasks = resetWarningTasks.get(mine.getId());
        if (tasks != null) {
            tasks.values().forEach(AbstractTask::cancel);
        }

        resetWarningTasks.remove(mine.getId());
    }

    public void close() {
        closing = true;
        for (Map<Integer, MineResetWarningTask> task : resetWarningTasks.values()) {
            task.values().forEach(AbstractTask::cancel);
        }

        for (MineResetTask task : resetTasks.values()) {
            task.cancel();
        }

        resetWarningTasks.clear();
        resetTasks.clear();
        resetInProgress.clear();
    }

    public void reload() {
        close();
        resetGenerations.clear();
        closing = false;
    }

    boolean tryBeginReset(String mineId, long generation) {
        if (closing || !isResetGenerationActive(mineId, generation)) return false;
        return resetInProgress
                .computeIfAbsent(mineId, id -> new AtomicBoolean())
                .compareAndSet(false, true);
    }

    void finishReset(String mineId, long generation) {
        if (!isResetGenerationActive(mineId, generation)) return;
        AtomicBoolean state = resetInProgress.get(mineId);
        if (state != null) state.set(false);
    }

    void completeReset(Mine mine, long generation, boolean manualReset) {
        if (!isResetGenerationActive(mine.getId(), generation)) return;
        finishReset(mine.getId(), generation);
        if (manualReset && mine.getRegenerateSeconds() > 0) {
            restartMineResetTask(mine);
        }
    }

    boolean isResetGenerationActive(String mineId, long generation) {
        AtomicLong current = resetGenerations.get(mineId);
        return !closing && current != null && current.get() == generation;
    }

    void abortReset(String mineId, long generation) {
        if (!isResetGenerationActive(mineId, generation)) return;
        resetGenerations.get(mineId).incrementAndGet();
        AtomicBoolean state = resetInProgress.get(mineId);
        if (state != null) state.set(false);
    }

    private static long toTicks(long millis) {
        return millis <= 0 ? 1L : (millis - 1L) / 50L + 1L;
    }

    private static long safeMillis(long seconds) {
        return Math.multiplyExact(Math.min(seconds, MAX_DELAY_SECONDS), 1000L);
    }

    private static long safeTicks(long seconds) {
        return Math.max(1L, Math.multiplyExact(Math.min(seconds, MAX_DELAY_SECONDS), 20L));
    }
}
