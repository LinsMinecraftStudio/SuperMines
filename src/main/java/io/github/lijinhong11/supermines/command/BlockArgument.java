package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;

public class BlockArgument extends Argument<PackedBlock> {
    public BlockArgument(String nodeName) {
        super(nodeName, StringArgumentType.string());

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
        try {
            return ContentProviders.getBlock(new StringReader(commandContext.getArgument(s, String.class)).readQuotedString());
        } catch (CommandSyntaxException e) {
            return ContentProviders.getBlock(commandContext.getArgument(s, String.class));
        }
    }
}
