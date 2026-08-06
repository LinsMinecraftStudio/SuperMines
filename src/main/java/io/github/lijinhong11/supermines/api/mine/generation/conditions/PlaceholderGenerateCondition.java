package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class PlaceholderGenerateCondition implements IGenerateCondition {
    private String placeholder;
    private String compareContent;
    private ParseType parseType;

    public PlaceholderGenerateCondition(ConfigurationSection cs) {
        this(cs.getString("placeholder"), cs.getString("compareContent"), ParseType.valueOf(cs.getString("parseType")));
    }

    public PlaceholderGenerateCondition(String placeholder, String compareContent, ParseType parseType) {
        this.placeholder = placeholder;
        this.compareContent = compareContent;
        this.parseType = parseType;
    }

    @Override
    public String key() {
        return "placeholder";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && parseType == ParseType.PLACEHOLDERAPI) {
            return PlaceholderAPI.setPlaceholders(null, placeholder).equals(compareContent);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders") && parseType == ParseType.MINIPLACEHOLDERS) {
            Component component =
                    MiniMessage.miniMessage().deserialize(placeholder, MiniPlaceholders.getGlobalPlaceholders());
            return PlainTextComponentSerializer.plainText().serialize(component).equals(compareContent);
        }

        return false;
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("placeholder", placeholder);
        cs.set("compareContent", compareContent);
        cs.set("parseType", parseType.toString());
    }

    @Override
    public void read(ConfigurationSection cs) {
        this.placeholder = cs.getString("placeholder");
        this.compareContent = cs.getString("compareContent");
        this.parseType = ParseType.valueOf(cs.getString("parseType"));
    }

    public enum ParseType {
        PLACEHOLDERAPI,
        MINIPLACEHOLDERS
    }
}
