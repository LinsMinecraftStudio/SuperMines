package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;

import dev.jorel.commandapi.executors.CommandArguments;
import io.github.lijinhong11.supermines.integrates.block.AddonBlock;
import io.github.lijinhong11.supermines.integrates.block.BlockAddon;

public class BlockArgument extends Argument<AddonBlock> {
    public BlockArgument(String nodeName) {
        super(nodeName, StringArgumentType.string());

        includeSuggestions(ArgumentSuggestions.strings(BlockAddon.getBlockSuggestions()));
    }


    @Override
    public Class<AddonBlock> getPrimitiveType() {
        return AddonBlock.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.PRIMITIVE_STRING;
    }

    @Override
    public <Source> AddonBlock parseArgument(CommandContext<Source> commandContext, String s, CommandArguments commandArguments) throws CommandSyntaxException {
        return BlockAddon.getAddonBlock(s);
    }
}
