package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

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
            startMineResetTask(mine);
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

        long delayMillis = resetTask.getNextResetTime() - System.currentTimeMillis() - warningSeconds * 1000L;
        if (delayMillis <= 0) {
            MineResetWarningTask task = new MineResetWarningTask(mine, warningSeconds);
            scheduler.runNextTick(task);
            return;
        }

        MineResetWarningTask task = new MineResetWarningTask(mine, warningSeconds);

        Map<Integer, MineResetWarningTask> warningMap = resetWarningTasks
                .computeIfAbsent(mine.getId(), id -> new HashMap<>());

        if (warningMap.containsKey(warningSeconds)) {
            return;
        }

        warningMap.put(warningSeconds, task);

        scheduler.runLaterAsync(task, delayMillis / 50);
    }

    public void startMineResetTask(Mine mine) {
        MineResetTask task = new MineResetTask(mine);
        scheduler.runTimerAsync(task, mine.getRegenerateSeconds() * 20L, mine.getRegenerateSeconds() * 20L);
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
