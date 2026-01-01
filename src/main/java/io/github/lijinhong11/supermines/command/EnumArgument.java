package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.executors.CommandArguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumArgument<E extends Enum<E>> extends Argument<E> {
    private final Class<E> enumClass;

    public EnumArgument(String argName, Class<E> enumClass) {
        super(argName, StringArgumentType.string());

        this.enumClass = enumClass;

        List<String> suggestions = new ArrayList<>(Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).toList());

        includeSuggestions(ArgumentSuggestions.strings(suggestions));
    }

    @Override
    public Class<E> getPrimitiveType() {
        return enumClass;
    }

    @Override
    public CommandAPIArgumentType getArgumentType() {
        return CommandAPIArgumentType.PRIMITIVE_STRING;
    }

    @Override
    public <Source> E parseArgument(CommandContext<Source> commandContext, String s, CommandArguments commandArguments) {
        return Enum.valueOf(enumClass, s.toUpperCase());
    }
}
