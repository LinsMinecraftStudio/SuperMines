package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event when a mine resets.
 */
public class MineResetEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;

    public MineResetEvent(Mine mine) {
        super(false);

        this.mine = mine;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Mine getMine() {
        return mine;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
