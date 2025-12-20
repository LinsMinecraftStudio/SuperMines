package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumArgument<E extends Enum<E>> extends CustomArgument<E, String> {
    public EnumArgument(String argName, Class<E> enumClass) {
        super(new StringArgument(argName), i -> Enum.valueOf(enumClass, i.input().toUpperCase()));

        List<String> suggestions = new ArrayList<>(Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).toList());

        includeSuggestions(ArgumentSuggestions.strings(suggestions));
    }
}
