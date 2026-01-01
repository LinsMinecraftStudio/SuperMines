package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.function.Consumer;

public abstract class AbstractTask implements Consumer<WrappedTask> {
    private WrappedTask task;

    @Override
    public void accept(WrappedTask wrappedTask) {
        this.task = wrappedTask;
        run(wrappedTask);
    }

    protected abstract void run(WrappedTask task);

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
