package io.github.lijinhong11.supermines.integrates.block;

import dev.lone.itemsadder.api.CustomBlock;

import java.util.List;

public class ItemsAdderBlockAddon extends BlockAddon {
    @Override
    public AddonBlock getBlock(String id) {
        CustomBlock block = CustomBlock.getInstance(id);
        if (block == null) {
            return null;
        }

        return new AddonBlock(block::place);
    }

    @Override
    public void addBlockSuggestions(String input, List<String> suggestions) {
        if (input.startsWith("ia")) {

        } else if (input.startsWith("itemsadder")) {

        }
    }

    @Override
    public List<String> getKey() {
        return List.of("itemsadder", "ia");
    }
}
