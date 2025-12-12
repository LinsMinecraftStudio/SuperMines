package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import io.github.lijinhong11.supermines.integrates.block.BlockAddon;

public class BlockArgument extends CustomArgument<AddonBlock, String> {
    public BlockArgument(String nodeName) {
        super(new StringArgument(nodeName), s -> {
            String input = s.input();
            return BlockAddon.getAddonBlock(input);
        });

        includeSuggestions(ArgumentSuggestions.strings(BlockAddon.getBlockSuggestions()));
    }
}
