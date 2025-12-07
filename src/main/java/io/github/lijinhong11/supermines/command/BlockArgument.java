package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import java.util.Arrays;

import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import org.bukkit.Material;

public class BlockArgument extends CustomArgument<AddonBlock, String> {
    public BlockArgument(String nodeName) {
        super(new StringArgument(nodeName), s -> {
            String input = s.input();
            int index = input.indexOf(':');
            String key = index == -1 ? "minecraft" : input.substring(index);
            String id = index == -1 ? input : input.substring(key.length());

            return null;
        });

        includeSuggestions(ArgumentSuggestions.strings(
                Arrays.stream(Material.values()).map(Material::toString).toList()));
    }
}
