package io.github.lijinhong11.supermines.integrates.block;

import io.github.lijinhong11.supermines.utils.NullUtils;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class OraxenBlockAddon extends BlockAddon {
    OraxenBlockAddon() {}

    @Override
    public AddonBlock getBlock(String id) {
        Mechanic block = NullUtils.tryAnyNotNull(OraxenBlocks.getNoteBlockMechanic(id), OraxenBlocks.getStringMechanic(id));
        if (block == null) {
            return null;
        }

        Optional<ItemBuilder> itemBuilderOptional = OraxenItems.getOptionalItemById(block.getItemID());
        if (itemBuilderOptional.isEmpty()) {
            return null;
        }

        ItemStack item = itemBuilderOptional.get().build();

        return new AddonBlock("oraxen", id, l -> OraxenBlocks.place(id, l), item);
    }

    @Override
    public AddonBlock getBlock(Location loc) {
        Mechanic block = OraxenBlocks.getNoteBlockMechanic(loc.getBlock());
        if (block == null) {
            return null;
        }

        String id = block.getItemID();

        Optional<ItemBuilder> itemBuilderOptional = OraxenItems.getOptionalItemById(block.getItemID());
        if (itemBuilderOptional.isEmpty()) {
            return null;
        }

        ItemStack item = itemBuilderOptional.get().build();

        return new AddonBlock("oraxen", id, l -> OraxenBlocks.place(id, l), item);
    }

    @Override
    public void removeBlock(Location loc) {
        OraxenBlocks.remove(loc, null);
    }

    @Override
    public void addBlockSuggestions(List<String> suggestions) {
        OraxenBlocks.getBlockIDs().forEach(s -> suggestions.add("oraxen:" + s));
    }

    @Override
    public List<String> getKey() {
        return List.of("oraxen");
    }
}
