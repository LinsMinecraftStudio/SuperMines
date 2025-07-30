package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import net.kyori.adventure.text.Component;

public class DisplayNameArgument extends CustomArgument<Component, String> {
    public DisplayNameArgument() {
        super(new TextArgument("displayName"), i -> ComponentUtils.deserialize(i.input()));
    }
}
