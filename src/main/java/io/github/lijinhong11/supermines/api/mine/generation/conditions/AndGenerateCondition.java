package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * A composite condition that requires every sub-condition to pass.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: and
 * conditions:
 *   - condition: surface
 *   - condition: chance
 *     chance: 0.5
 * }</pre>
 */
public class AndGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final List<IGenerateCondition> conditions;

    public AndGenerateCondition(ConfigurationSection cs) {
        this(loadSubConditions(cs));
    }

    public AndGenerateCondition(List<IGenerateCondition> conditions) {
        this.conditions = conditions;
    }

    public List<IGenerateCondition> getConditions() {
        return conditions;
    }

    @Override
    public String key() {
        return "and";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        for (IGenerateCondition condition : conditions) {
            if (!condition.canGenerate(mine, target)) {
                return false;
            }
        }

        return true;
    }

    private static List<IGenerateCondition> loadSubConditions(ConfigurationSection cs) {
        return getSubGenerateConditions(cs);
    }

    @NonNull static List<IGenerateCondition> getSubGenerateConditions(ConfigurationSection cs) {
        List<IGenerateCondition> result = new ArrayList<>();
        ConfigurationSection sub = cs.getConfigurationSection("conditions");
        if (sub == null) {
            return result;
        }

        for (String key : sub.getKeys(false)) {
            ConfigurationSection section = sub.getConfigurationSection(key);
            if (section != null) {
                result.add(ConditionLoader.deserialize(section));
            }
        }

        return result;
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
