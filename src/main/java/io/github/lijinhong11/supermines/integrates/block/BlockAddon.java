package io.github.lijinhong11.supermines.integrates.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BlockAddon {
    private static final List<BlockAddon> blockAddons = new ArrayList<>();

    public static void init() {
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            blockAddons.add(new ItemsAdderBlockAddon());
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            blockAddons.add(new NexoBlockAddon());
        }

        blockAddons.add(MinecraftBlockAddon.INSTANCE);
    }

    public static BlockAddon getBlockAddon(String key) {
        return blockAddons.stream().filter(b -> b.getKey().contains(key)).findFirst().orElse(null);
    }

    public static AddonBlock getAddonBlock(String keyID) {
        int index = keyID.indexOf(':');
        if (index == -1) {
            return getAddonBlock("", keyID);
        } else {
            String key = keyID.substring(0, index);
            String id = keyID.substring(index);
            return getAddonBlock(key, id);
        }
    }

    public static List<String> getBlockSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for (BlockAddon addon : blockAddons) {
            addon.addBlockSuggestions(suggestions);
        }

        return suggestions;
    }

    public static AddonBlock getAddonBlock(String key, String id) {
        Optional<BlockAddon> addon = blockAddons.stream().filter(b -> b.getKey().contains(key)).findFirst();
        return addon.map(blockAddon -> blockAddon.getBlock(id)).orElse(null);
    }

    public static AddonBlock getAddonBlockByLocation(Location loc) {
        for (BlockAddon addon : blockAddons) {
            AddonBlock block = addon.getBlock(loc);
            if (block != null) {
                return block;
            }
        }

        return MinecraftBlockAddon.INSTANCE.getBlock(loc);
    }

    public static void removeAddonBlock(Location loc) {
        blockAddons.forEach(b -> b.removeBlock(loc));
    }

    public static List<AddonBlock> getAllBlocks() {
        List<AddonBlock> blocks = new ArrayList<>();

        for (BlockAddon addon : blockAddons) {
            List<String> suggestions = new ArrayList<>();
            addon.addBlockSuggestions(suggestions);
            blocks.addAll(suggestions.parallelStream().map(BlockAddon::getAddonBlock).toList());
        }

        return blocks;
    }

    public abstract AddonBlock getBlock(String id);

    public abstract AddonBlock getBlock(Location loc);

    public abstract void removeBlock(Location loc);

    public abstract void addBlockSuggestions(List<String> suggestions);

    public abstract List<String> getKey();
}
