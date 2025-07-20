package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskMaker {
    private final PlatformScheduler scheduler;
    private final Map<String, MineResetTask> resetTasks;
    private final Map<String, List<MineResetWarningTask>> resetWarningTasks;

    public TaskMaker(FoliaLib f) {
        scheduler = f.getScheduler();
        resetTasks = new HashMap<>();
        resetWarningTasks = new HashMap<>();
    }

    public void startup() {
        for (Mine mine : SuperMines.getInstance().getMineManager().getAll()) {
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
        MineResetWarningTask task = new MineResetWarningTask(mine, warningSeconds);
        int warnTime = mine.getRegenerateSeconds() - warningSeconds;
        if (!resetWarningTasks.containsKey(mine.getId())) {
            resetWarningTasks.put(mine.getId(), new ArrayList<>(List.of(task)));
        } else {
            resetWarningTasks.get(mine.getId()).add(task);
        }
        scheduler.runTimerAsync(task, warnTime * 20L, warnTime * 20L);
    }

    public void startMineResetTask(Mine mine) {
        MineResetTask task = new MineResetTask(mine);
        scheduler.runTimerAsync(task, 1L, mine.getRegenerateSeconds() * 20L);
        resetTasks.put(mine.getId(), task);
    }

    public void cancelMineResetTask(Mine mine) {
        AbstractTask task = resetTasks.get(mine.getId());
        if (task != null) {
            task.cancel();
        }

        resetTasks.remove(mine.getId());
    }

    public void close() {
        for (List<MineResetWarningTask> task : resetWarningTasks.values()) {
            task.forEach(AbstractTask::cancel);
        }

        for (MineResetTask task : resetTasks.values()) {
            task.cancel();
        }

        resetWarningTasks.clear();
        resetTasks.clear();
    }
}
