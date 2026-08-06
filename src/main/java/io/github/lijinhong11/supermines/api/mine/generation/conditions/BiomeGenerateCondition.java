package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that only lets blocks generate when the target block is located in one of the
 * configured world biomes.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: biome
 * biomes:
 *   - PLAINS
 *   - FOREST
 * }</pre>
 */
public class BiomeGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final Set<String> biomes;

    public BiomeGenerateCondition(ConfigurationSection cs) {
        this(cs.getStringList("biomes"));
    }

    public BiomeGenerateCondition(Collection<String> biomes) {
        Preconditions.checkArgument(biomes != null && !biomes.isEmpty(), "biomes cannot be null or empty");

        this.biomes = new HashSet<>();
        biomes.forEach(biome -> this.biomes.add(biome.toUpperCase()));
    }

    public Set<String> getBiomes() {
        return biomes;
    }

    @Override
    public String key() {
        return "biome";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        Biome biome = target.toLocation(mine.getWorld()).getBlock().getBiome();
        return biomes.contains(biome.name());
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("biomes", new ArrayList<>(biomes));
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
