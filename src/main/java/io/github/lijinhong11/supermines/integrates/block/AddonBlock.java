package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

//TODO: use MittelLib (although it is not finished)
public class AddonBlock {
    private final String key;
    private final String id;
    private final ItemStack item;
    private final Consumer<Location> placer;

    AddonBlock(String key, String id, Consumer<Location> placer, ItemStack item) {
        this.key = key;
        this.id = id;
        this.item = item;
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

    public ItemStack toItem() {
        return item;
    }

    @Override
    public String toString() {
        return (key.isBlank() ? "" : key + ":") + id;
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
