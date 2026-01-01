package io.github.lijinhong11.supermines.integrates.block;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NexoBlockAddon extends BlockAddon {
    NexoBlockAddon() {
    }

    @Override
    public AddonBlock getBlock(String id) {
        CustomBlockMechanic block = NexoBlocks.customBlockMechanic(id);
        if (block == null) {
            return null;
        }

        Optional<ItemBuilder> itemBuilderOptional = NexoItems.optionalItemFromId(block.getItemID());
        if (itemBuilderOptional.isEmpty()) {
            return null;
        }

        ItemStack item = itemBuilderOptional.get().build();

        return new AddonBlock("nexo", id, l -> NexoBlocks.place(id, l), item);
    }

    @Override
    public AddonBlock getBlock(Location loc) {
        CustomBlockMechanic block = NexoBlocks.customBlockMechanic(loc);
        if (block == null) {
            return null;
        }

        String id = block.getItemID();

        Optional<ItemBuilder> itemBuilderOptional = NexoItems.optionalItemFromId(id);
        if (itemBuilderOptional.isEmpty()) {
            return null;
        }

        ItemStack item = itemBuilderOptional.get().build();

        return new AddonBlock("nexo", id, l -> NexoBlocks.place(id, l), item);
    }

    @Override
    public void removeBlock(Location loc) {
        NexoBlocks.remove(loc);
    }

    @Override
    public void addBlockSuggestions(List<String> suggestions) {
        Arrays.stream(NexoBlocks.stringBlockIDs()).forEach(s -> suggestions.add("nexo:" + s));
    }

    @Override
    public List<String> getKey() {
        return List.of("nexo");
    }
}
