package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event when a mine resets.
 * <p>
 * Note: it will be triggered <b>asynchronously</b>
 */
public class MineResetEvent extends Event {
    private final Mine mine;

    public MineResetEvent(Mine mine) {
        super(true);
        this.mine = mine;
    }

    public Mine getMine() {
        return mine;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return new HandlerList();
    }

    public static HandlerList getHandlerList() {
        return new HandlerList();
    }
}
