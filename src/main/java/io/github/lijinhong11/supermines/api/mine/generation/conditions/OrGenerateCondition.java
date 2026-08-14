package io.github.lijinhong11.supermines.api.mine.generation.conditions;

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
 * A composite condition that passes when at least one sub-condition passes.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: or
 * conditions:
 *   - condition: border
 *     mode: RIM
 *   - condition: chance
 *     chance: 0.2
 * }</pre>
 */
public record OrGenerateCondition(List<IGenerateCondition> conditions) implements IGenerateCondition, ReadWriteObject {
    public OrGenerateCondition(ConfigurationSection cs) {
        this(loadSubConditions(cs));
    }

    @Override
    public @NonNull String key() {
        return "or";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        for (IGenerateCondition condition : conditions) {
            if (condition.canGenerate(mine, target)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NonNull Material icon() {
        return Material.ORANGE_WOOL;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.or.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.or",
                        MessageReplacement.replace("%amount%", String.valueOf(conditions.size())));
    }

    private static List<IGenerateCondition> loadSubConditions(ConfigurationSection cs) {
        return AndGenerateCondition.getSubGenerateConditions(cs);
    }

    @Override
    public void write(ConfigurationSection cs) {
        for (int i = 0; i < conditions.size(); i++) {
            IGenerateCondition condition = conditions.get(i);
            if (condition instanceof ReadWriteObject rw) {
                rw.write(cs.createSection("conditions." + i));
            }
        }
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
