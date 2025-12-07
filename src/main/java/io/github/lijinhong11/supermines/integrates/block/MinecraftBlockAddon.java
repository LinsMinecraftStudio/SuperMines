package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Material;

import java.util.List;

public class MinecraftBlockAddon extends BlockAddon {
    public static final MinecraftBlockAddon INSTANCE;

    static {
        INSTANCE = new MinecraftBlockAddon();
    }

    private MinecraftBlockAddon() {}

    public static AddonBlock createForMaterial(Material material) {
        return new AddonBlock(l -> l.getBlock().setType(material));
    }

    @Override
    public AddonBlock getBlock(String id) {
        return null;
    }

    @Override
    public void addBlockSuggestions(String input, List<String> suggestions) {

    }

    @Override
    public List<String> getKey() {
        return List.of("minecraft", "mc"); //or no key
    }
}
