package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;

import java.util.Objects;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AddonBlock block)) {
            return false;
        }

        return Objects.equals(getKey(), block.getKey())
                && Objects.equals(getId(), block.getId())
                && Objects.equals(placer, block.placer);
    }
}
