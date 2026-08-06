package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

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
public class OrGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final List<IGenerateCondition> conditions;

    public OrGenerateCondition(ConfigurationSection cs) {
        this(loadSubConditions(cs));
    }

    public OrGenerateCondition(List<IGenerateCondition> conditions) {
        this.conditions = conditions;
    }

    public List<IGenerateCondition> getConditions() {
        return conditions;
    }

    @Override
    public String key() {
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
