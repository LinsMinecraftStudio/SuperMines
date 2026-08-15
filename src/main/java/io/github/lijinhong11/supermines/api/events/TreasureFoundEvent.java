package io.github.lijinhong11.supermines.api.events;

import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an event before a Treasure reward is granted by a mine or regen point.
 *
 * <p>This event is cancellable.
 */
public final class TreasureFoundEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Source {
        MINE,
        REGEN_POINT
    }

    private final Treasure treasure;
    private final Player player;
    private final Source source;
    private final Mine mine;
    private final RegenPoint regenPoint;
    private boolean cancelled;

    public TreasureFoundEvent(@NotNull Treasure treasure, @NotNull Player player, @NotNull Mine mine) {
        this(treasure, player, Source.MINE, mine, null);
    }

    public TreasureFoundEvent(@NotNull Treasure treasure, @NotNull Player player, @NotNull RegenPoint regenPoint) {
        this(treasure, player, Source.REGEN_POINT, null, regenPoint);
    }

    private TreasureFoundEvent(Treasure treasure, Player player, Source source, Mine mine, RegenPoint regenPoint) {
        super(!Bukkit.isPrimaryThread());
        this.treasure = treasure;
        this.player = player;
        this.source = source;
        this.mine = mine;
        this.regenPoint = regenPoint;
    }

    public @NotNull Treasure getTreasure() {
        return treasure;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull Source getSource() {
        return source;
    }

    public @Nullable Mine getMine() {
        return mine;
    }

    public @Nullable RegenPoint getRegenPoint() {
        return regenPoint;
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
