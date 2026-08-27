package io.github.lijinhong11.supermines.task;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.atomic.AtomicLong;

public class RegenPointResetTask extends AbstractTask {
    private final RegenPoint regenPoint;
    private final long generation;
    private final AtomicLong nextResetTime = new AtomicLong();

    public RegenPointResetTask(RegenPoint regenPoint, long generation, long delayMillis) {
        this.regenPoint = regenPoint;
        this.generation = generation;
        refreshNextResetTime(delayMillis);
    }

    @Override
    protected void run(ScheduledTask task) {
        SuperMines.getInstance().getRegenPointManager().runRespawnTask(regenPoint, generation, this);
    }

    public long getNextResetTime() {
        return nextResetTime.get();
    }

    public void refreshNextResetTime(long delayMillis) {
        nextResetTime.set(System.currentTimeMillis() + Math.max(0L, delayMillis));
    }
}
