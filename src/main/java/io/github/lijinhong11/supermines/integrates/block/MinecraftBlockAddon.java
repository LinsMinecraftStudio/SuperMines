package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MinecraftBlockAddon extends BlockAddon {
    public static final MinecraftBlockAddon INSTANCE;

    static {
        INSTANCE = new MinecraftBlockAddon();
    }

    private MinecraftBlockAddon() {}

    public static AddonBlock createForMaterial(Material material) {
        if (material == null) {
            return null;
        }

        if (material.isAir()) {
            return null;
        }

        return new AddonBlock("", material.toString(), l -> l.getBlock().setType(material), new ItemStack(material));
    }

    @Override
    public AddonBlock getBlock(String id) {
        Material material = Material.matchMaterial(id.toUpperCase());
        return createForMaterial(material);
    }

    @Override
    public AddonBlock getBlock(Location loc) {
        return createForMaterial(loc.getBlock().getType());
    }

    @Override
    public void removeBlock(Location loc) {
        loc.getBlock().setType(Material.AIR);
    }

    @Override
    public void addBlockSuggestions(List<String> suggestions) {
        for (Material material : Material.values()) {
            suggestions.add(material.toString().toLowerCase());
        }
    }

    @Override
    public List<String> getKey() {
        return List.of("minecraft", "");
    }
}
