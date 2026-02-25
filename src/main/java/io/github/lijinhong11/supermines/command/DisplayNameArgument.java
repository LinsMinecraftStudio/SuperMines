package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import net.kyori.adventure.text.Component;

public class DisplayNameArgument extends Argument<Component> {
    public DisplayNameArgument() {
        super("displayName", StringArgumentType.greedyString());
    }

    @Override
    public Class<Component> getPrimitiveType() {
        return Component.class;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.PRIMITIVE_STRING;
    }

    @Override
    public <Source> Component parseArgument(
            CommandContext<Source> commandContext, String key, CommandArguments commandArguments) {
        return ComponentUtils.deserialize(commandContext.getArgument(key, String.class));
    }
}
