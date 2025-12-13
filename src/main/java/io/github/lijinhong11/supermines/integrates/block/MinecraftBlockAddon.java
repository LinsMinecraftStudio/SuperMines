package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;

import java.util.List;

public class MinecraftBlockAddon extends BlockAddon {
    public static final MinecraftBlockAddon INSTANCE;

    static {
        INSTANCE = new MinecraftBlockAddon();
    }

    private MinecraftBlockAddon() {}

    @Contract("!null -> !null")
    public static AddonBlock createForMaterial(Material material) {
        if (material == null) {
            return null;
        }

        return new AddonBlock("", material.toString(), l -> l.getBlock().setType(material));
    }

    @Override
    public AddonBlock getBlock(String id) {
        Material material = Material.matchMaterial(id);
        return createForMaterial(material);
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
        return List.of("minecraft", "mc", "");
    }
}
