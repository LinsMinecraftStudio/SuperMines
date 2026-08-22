package io.github.lijinhong11.supermines.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class AbstractTask implements Consumer<ScheduledTask> {
    private volatile ScheduledTask task;
    private final AtomicBoolean cancelled = new AtomicBoolean();

    public void bind(ScheduledTask task) {
        this.task = task;
        if (cancelled.get() && !task.isCancelled()) {
            task.cancel();
        }
    }

    @Override
    public void accept(ScheduledTask scheduledTask) {
        bind(scheduledTask);
        if (!cancelled.get()) {
            run(scheduledTask);
        }
    }

    protected abstract void run(ScheduledTask task);

    public void cancel() {
        cancelled.set(true);
        ScheduledTask current = task;
        if (current != null && !current.isCancelled()) current.cancel();
    }

    public boolean isCancelled() {
        return cancelled.get();
    }
}
