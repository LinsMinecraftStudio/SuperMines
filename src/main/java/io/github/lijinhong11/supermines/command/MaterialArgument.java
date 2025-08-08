package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Material;

import java.util.Arrays;

public class MaterialArgument extends CustomArgument<Material, String> {
    public MaterialArgument(String nodeName) {
        super(new StringArgument(nodeName), s -> Material.getMaterial(s.input()));

        includeSuggestions(ArgumentSuggestions.strings(Arrays.stream(Material.values()).map(Material::toString).toList()));
    }
}
