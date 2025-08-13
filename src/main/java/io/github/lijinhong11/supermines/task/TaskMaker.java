package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

public class TaskMaker {
    private final PlatformScheduler scheduler;
    private final Map<String, MineResetTask> resetTasks;
    private final Map<String, Map<Integer, MineResetWarningTask>> resetWarningTasks;

    public TaskMaker(FoliaLib f) {
        scheduler = f.getScheduler();
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

    public void runSync(Location loc, Runnable runnable) {
        if (loc.getWorld() == null) {
            return;
        }

        scheduler.runAtLocation(loc, t -> runnable.run());
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

            scheduler.runNextTick(task);
            scheduler.runLaterAsync(
                    t -> {
                        scheduler.runTimerAsync(task, delayMillis / 50L, mine.getRegenerateSeconds() * 20L);
                        warningMap.put(warningSeconds, task);
                    },
                    delayMillis / 50);
            return;
        }

        warningMap.put(warningSeconds, task);
        scheduler.runTimerAsync(task, delayMillis / 50L, mine.getRegenerateSeconds() * 20L);
    }

    public void startMineResetTask(Mine mine) {
        MineResetTask task = new MineResetTask(mine);
        scheduler.runTimerAsync(task, 1L, mine.getRegenerateSeconds() * 20L);
        resetTasks.put(mine.getId(), task);
    }

    public void runMineResetTaskNow(Mine mine) {
        MineResetTask mrt = new MineResetTask(mine);
        scheduler.runNextTick(mrt);
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
