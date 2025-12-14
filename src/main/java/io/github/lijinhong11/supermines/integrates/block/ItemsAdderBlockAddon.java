package io.github.lijinhong11.supermines.integrates.block;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Location;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemsAdderBlockAddon extends BlockAddon {
    ItemsAdderBlockAddon() {}

    @Override
    public AddonBlock getBlock(String id) {
        CustomBlock block = CustomBlock.getInstance(id);
        if (block == null) {
            return null;
        }

        return new AddonBlock("ia", id, block::place, block.getItemStack());
    }

    @Override
    public void removeBlock(Location loc) {
        CustomBlock.remove(loc);
    }

    @Override
    public void addBlockSuggestions(List<String> suggestions) {
        List<String> blocks = ItemsAdder.getNamespacedBlocksNamesInConfig();
        blocks.forEach(s -> suggestions.add("ia:" + s));
        blocks.forEach(s -> suggestions.add("itemsadder:" + s));
    }

    @Override
    public List<String> getKey() {
        return List.of("itemsadder", "ia");
    }
}
