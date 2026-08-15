package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event before a mine is removed.
 *
 * <p>This event is cancellable. Cancelling keeps the mine registered.
 */
public final class MineRemoveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;
    private boolean cancelled;

    public MineRemoveEvent(@NotNull Mine mine) {
        super(false);
        this.mine = mine;
    }

    public @NotNull Mine getMine() {
        return mine;
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
