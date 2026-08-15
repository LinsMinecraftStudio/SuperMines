package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event before a mutable mine property is changed.
 *
 * <p>This event is cancellable.
 */
public final class MineEditEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Property {
        DISPLAY_NAME,
        DISPLAY_ICON,
        AREA,
        RESET_SECONDS,
        ONLY_FILL_AIR,
        AUTO_PICKUP,
        REQUIRED_RANK_LEVEL,
        TELEPORT_LOCATION
    }

    private final Mine mine;
    private final Property property;
    private final Object oldValue;
    private final Object newValue;
    private boolean cancelled;

    public MineEditEvent(
            @NotNull Mine mine, @NotNull Property property, @Nullable Object oldValue, @Nullable Object newValue) {
        super(false);
        this.mine = mine;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public @NotNull Mine getMine() {
        return mine;
    }

    public @NotNull Property getProperty() {
        return property;
    }

    public @Nullable Object getOldValue() {
        return oldValue;
    }

    public @Nullable Object getNewValue() {
        return newValue;
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
