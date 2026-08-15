package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.miniplaceholders.api.MiniPlaceholders;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public final class PlaceholderGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final String placeholder;
    private final String compareContent;
    private final ParseType parseType;

    public PlaceholderGenerateCondition(ConfigurationSection cs) {
        this(cs.getString("placeholder"), cs.getString("compareContent"), ParseType.valueOf(cs.getString("parseType")));
    }

    public PlaceholderGenerateCondition(String placeholder, String compareContent, ParseType parseType) {
        this.placeholder = placeholder;
        this.compareContent = compareContent;
        this.parseType = parseType;
    }

    @Override
    public @NonNull String key() {
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
    public @NonNull Material icon() {
        return Material.GLASS_BOTTLE;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.placeholder.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.placeholder",
                        MessageReplacement.replace("%placeholder%", placeholder),
                        MessageReplacement.replace("%compareContent%", compareContent),
                        MessageReplacement.replace("%parseType%", parseType.toString()));
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("placeholder", placeholder);
        cs.set("compareContent", compareContent);
        cs.set("parseType", parseType.toString());
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getCompareContent() {
        return compareContent;
    }

    public ParseType getParseType() {
        return parseType;
    }

    public enum ParseType {
        PLACEHOLDERAPI,
        MINIPLACEHOLDERS
    }
}
