package io.github.lijinhong11.supermines.task;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class TaskMaker {
    private final Map<String, MineResetTask> resetTasks;
    private final Map<String, Map<Integer, MineResetWarningTask>> resetWarningTasks;

    public TaskMaker() {
        resetTasks = new HashMap<>();
        resetWarningTasks = new HashMap<>();
    }

    public void startup() {
        for (Mine mine : SuperMines.getInstance().getMineManager().getAllMines()) {
            if (mine.getRegenerateSeconds() < 1) {
                continue;
            }

            startMineResetTask(mine);

            for (int second : mine.getWarningSeconds()) {
                startMineWarningTask(mine, second);
            }
        }
    }

    public void runSync(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(SuperMines.getInstance(), t -> runnable.run());
    }

    public void runSync(Location loc, Runnable runnable) {
        if (loc.getWorld() == null) {
            return;
        }

        Bukkit.getRegionScheduler().run(SuperMines.getInstance(), loc, t -> runnable.run());
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
                resetWarningTasks.computeIfAbsent(mine.getId(), id -> new HashMap<>());

        if (warningMap.containsKey(warningSeconds)) {
            return;
        }

        MineResetWarningTask task = new MineResetWarningTask(mine, warningSeconds);

        long delayMillis = resetTask.getNextResetTime() - System.currentTimeMillis() - warningSeconds * 1000L;
        if (delayMillis <= 0) {
            Bukkit.getAsyncScheduler().runNow(SuperMines.getInstance(), task);
            Bukkit.getAsyncScheduler()
                    .runDelayed(
                            SuperMines.getInstance(),
                            t -> {
                                Bukkit.getAsyncScheduler()
                                        .runAtFixedRate(
                                                SuperMines.getInstance(),
                                                task,
                                                delayMillis / 50L,
                                                mine.getRegenerateSeconds(),
                                                TimeUnit.SECONDS);
                                warningMap.put(warningSeconds, task);
                            },
                            delayMillis / 50,
                            TimeUnit.SECONDS);
            return;
        }

        warningMap.put(warningSeconds, task);
        Bukkit.getAsyncScheduler()
                .runAtFixedRate(
                        SuperMines.getInstance(),
                        task,
                        delayMillis / 50L,
                        mine.getRegenerateSeconds(),
                        TimeUnit.SECONDS);
    }

    public void startMineResetTask(Mine mine) {
        MineResetTask task = new MineResetTask(mine);
        Bukkit.getAsyncScheduler()
                .runAtFixedRate(SuperMines.getInstance(), task, 1L, mine.getRegenerateSeconds(), TimeUnit.SECONDS);
        resetTasks.put(mine.getId(), task);
    }

    public void runMineResetTaskNow(Mine mine) {
        MineResetTask mrt = new MineResetTask(mine, true);
        Bukkit.getAsyncScheduler().runNow(SuperMines.getInstance(), mrt);
    }

    public void cancelMineResetTask(Mine mine) {
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
        MineResetWarningTask task =
                resetWarningTasks.getOrDefault(mine.getId(), new HashMap<>()).get(restSeconds);
        if (task != null) {
            task.cancel();
        }
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
        for (Map<Integer, MineResetWarningTask> task : resetWarningTasks.values()) {
            task.values().forEach(AbstractTask::cancel);
        }

        for (MineResetTask task : resetTasks.values()) {
            task.cancel();
        }

        resetWarningTasks.clear();
        resetTasks.clear();
    }
}
