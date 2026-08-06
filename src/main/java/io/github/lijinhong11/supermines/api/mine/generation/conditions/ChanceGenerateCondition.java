package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that only lets a block generate with a configurable probability.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: chance
 * chance: 0.5
 * }</pre>
 */
public class ChanceGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final double chance;

    public ChanceGenerateCondition(ConfigurationSection cs) {
        this(cs.getDouble("chance"));
    }

    public ChanceGenerateCondition(double chance) {
        Preconditions.checkArgument(chance >= 0 && chance <= 1, "chance must be between 0 and 1");

        this.chance = chance;
    }

    public double getChance() {
        return chance;
    }

    @Override
    public String key() {
        return "chance";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("chance", chance);
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
