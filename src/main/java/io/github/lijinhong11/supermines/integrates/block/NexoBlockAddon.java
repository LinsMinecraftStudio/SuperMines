package io.github.lijinhong11.supermines.integrates.block;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class NexoBlockAddon extends BlockAddon {
    NexoBlockAddon() {}

    @Override
    public AddonBlock getBlock(String id) {
        CustomBlockMechanic block = NexoBlocks.customBlockMechanic(id);
        if (block == null) {
            return null;
        }

        return new AddonBlock("nexo", id, l -> NexoBlocks.place(id, l));
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
