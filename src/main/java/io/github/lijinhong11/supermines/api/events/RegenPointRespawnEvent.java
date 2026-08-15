package io.github.lijinhong11.supermines.api.events;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event before the selected block is placed at a regen point.
 *
 * <p>This event is cancellable.
 */
public final class RegenPointRespawnEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Cause {
        SCHEDULED,
        MANUAL
    }

    private final RegenPoint regenPoint;
    private final Cause cause;
    private PackedBlock block;
    private boolean cancelled;

    public RegenPointRespawnEvent(@NotNull RegenPoint regenPoint, @NotNull PackedBlock block, @NotNull Cause cause) {
        super(false);
        this.regenPoint = regenPoint;
        this.block = block;
        this.cause = cause;
    }

    public @NotNull RegenPoint getRegenPoint() {
        return regenPoint;
    }

    public @NotNull PackedBlock getBlock() {
        return block;
    }

    public void setBlock(@NotNull PackedBlock block) {
        Preconditions.checkNotNull(block, "block cannot be null");
        this.block = block;
    }

    public @NotNull Cause getCause() {
        return cause;
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
