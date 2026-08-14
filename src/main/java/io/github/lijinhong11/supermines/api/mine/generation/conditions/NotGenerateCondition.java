package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * A condition that inverts the result of a single nested sub-condition.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: not
 * inner:
 *   condition: border
 *   mode: RIM
 * }</pre>
 */
public record NotGenerateCondition(IGenerateCondition inner) implements IGenerateCondition, ReadWriteObject {
    public NotGenerateCondition(ConfigurationSection cs) {
        this(ConditionLoader.deserialize(cs.getConfigurationSection("inner")));
    }

    public NotGenerateCondition {
        Preconditions.checkNotNull(inner, "inner cannot be null");
    }

    @Override
    public @NonNull String key() {
        return "not";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        return !inner.canGenerate(mine, target);
    }

    @Override
    public @NonNull Material icon() {
        return Material.RED_WOOL;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.not.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.not",
                        MessageReplacement.replace("%inner%", inner.key()));
    }

    @Override
    public void write(ConfigurationSection cs) {
        if (inner instanceof ReadWriteObject rw) {
            rw.write(cs.createSection("inner"));
        }
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
