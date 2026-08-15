package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event before SuperMines processes a successful block break inside a mine.
 *
 * <p>This event is cancellable.
 */
public final class BlockBreakInMineEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Mine mine;
    private final Player player;
    private final PackedBlock block;
    private boolean cancelled;

    public BlockBreakInMineEvent(@NotNull Mine mine, @NotNull Player player, @Nullable PackedBlock block) {
        super(false);
        this.mine = mine;
        this.player = player;
        this.block = block;
    }

    public @NotNull Mine getMine() {
        return mine;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @Nullable PackedBlock getBlock() {
        return block;
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
