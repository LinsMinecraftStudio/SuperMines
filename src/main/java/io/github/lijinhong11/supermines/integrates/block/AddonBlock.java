package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;

import java.util.function.Consumer;

public class AddonBlock {
    private final String key;
    private final String id;
    private final Consumer<Location> placer;

    AddonBlock(String key, String id, Consumer<Location> placer) {
        this.key = key;
        this.id = id;
        this.placer = placer;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public void place(Location loc) {
        placer.accept(loc);
    }

    public void remove(Location loc) {

    }
}
