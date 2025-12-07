package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockAddon {
    private static final List<BlockAddon> blockAddons = new ArrayList<>();

    public static void init() {
        blockAddons.add(MinecraftBlockAddon.INSTANCE);

        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            blockAddons.add(new ItemsAdderBlockAddon());
        }
    }

    public abstract AddonBlock getBlock(String id);

    public abstract void addBlockSuggestions(String input, List<String> suggestions);

    public abstract List<String> getKey();
}
