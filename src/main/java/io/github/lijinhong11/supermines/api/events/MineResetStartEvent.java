package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event before a mine reset starts generating blocks.
 *
 * <p>This event is cancellable.
 */
public final class MineResetStartEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;
    private final boolean manual;
    private boolean cancelled;

    public MineResetStartEvent(@NotNull Mine mine, boolean manual) {
        super(false);
        this.mine = mine;
        this.manual = manual;
    }

    public @NotNull Mine getMine() {
        return mine;
    }

    public boolean isManual() {
        return manual;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
