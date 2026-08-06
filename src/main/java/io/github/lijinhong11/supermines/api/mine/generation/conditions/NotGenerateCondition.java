package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

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
public class NotGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final IGenerateCondition inner;

    public NotGenerateCondition(ConfigurationSection cs) {
        this(ConditionLoader.deserialize(cs.getConfigurationSection("inner")));
    }

    public NotGenerateCondition(IGenerateCondition inner) {
        Preconditions.checkNotNull(inner, "inner cannot be null");

        this.inner = inner;
    }

    public IGenerateCondition getInner() {
        return inner;
    }

    @Override
    public String key() {
        return "not";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        return !inner.canGenerate(mine, target);
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
