package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;

public class BlockArgument extends Argument<PackedBlock> {
    public BlockArgument(String nodeName) {
        super(nodeName, StringArgumentType.greedyString());

        includeSuggestions(ArgumentSuggestions.strings(ContentProviders.getBlockSuggestions()));
    }

    @Override
    public Class<PackedBlock> getPrimitiveType() {
        return PackedBlock.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.PRIMITIVE_STRING;
    }

    @Override
    public <Source> PackedBlock parseArgument(
            CommandContext<Source> commandContext, String s, CommandArguments commandArguments) {
        return ContentProviders.getBlock(commandContext.getArgument(s, String.class));
    }
}
