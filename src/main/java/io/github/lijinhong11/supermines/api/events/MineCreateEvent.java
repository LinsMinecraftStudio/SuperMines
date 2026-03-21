package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event when mine is created.
 */
public class MineCreateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;

    public MineCreateEvent(Mine mine) {
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
