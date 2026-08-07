package io.github.lijinhong11.supermines.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.function.Consumer;

public abstract class AbstractTask implements Consumer<ScheduledTask> {
    private ScheduledTask task;

    @Override
    public void accept(ScheduledTask ScheduledTask) {
        this.task = ScheduledTask;
        run(ScheduledTask);
    }

    protected abstract void run(ScheduledTask task);

    public void cancel() {
        if (task == null) {
            return;
        }

        if (task.isCancelled()) {
            return;
        }

        task.cancel();
    }
}
