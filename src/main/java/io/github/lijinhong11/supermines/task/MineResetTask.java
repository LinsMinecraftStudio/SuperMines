package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;

import java.util.function.Consumer;

public class MineResetTask implements Consumer<WrappedTask> {
    private WrappedTask task;

    @Override
    public void accept(WrappedTask wrappedTask) {
        task = wrappedTask;


    }

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
