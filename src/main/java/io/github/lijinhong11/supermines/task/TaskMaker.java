package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;

import java.util.HashMap;
import java.util.Map;

public class TaskMaker {
    private final PlatformScheduler scheduler;
    private final Map<String, AbstractTask> resetTasks;

    public TaskMaker(FoliaLib f) {
        scheduler = f.getScheduler();
        resetTasks = new HashMap<>();
    }

    public void startup() {
        for (Mine mine : SuperMines.getInstance().getMineManager().getAll()) {
            startMineResetTask(mine);
        }
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
}
