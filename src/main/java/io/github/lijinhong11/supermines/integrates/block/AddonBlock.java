package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;

import java.util.function.Consumer;

public class AddonBlock {
    private final Consumer<Location> placer;

    AddonBlock(Consumer<Location> placer) {
        this.placer = placer;
    }

    public void place(Location loc) {
        placer.accept(loc);
    }

    public void remove(Location loc) {

    }
}
