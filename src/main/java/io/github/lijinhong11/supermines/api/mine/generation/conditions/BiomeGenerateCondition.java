package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * A condition that only lets blocks generate when the target block is located in one of the
 * configured world biomes.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: biome
 * biomes:
 *   - minecraft:plains
 * }</pre>
 */
public class BiomeGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final Set<Key> biomes;

    public BiomeGenerateCondition(ConfigurationSection cs) {
        this(cs.getStringList("biomes"));
    }

    public BiomeGenerateCondition(Collection<String> biomes) {
        Preconditions.checkArgument(biomes != null && !biomes.isEmpty(), "biomes cannot be null or empty");

        this.biomes = new HashSet<>();
        biomes.forEach(biome -> this.biomes.add(NamespacedKey.fromString(biome)));
    }

    public Set<Key> getBiomes() {
        return biomes;
    }

    public Set<String> getStringBiomes() {
        return biomes.stream().map(Key::asString).collect(Collectors.toSet());
    }

    @Override
    public @NonNull String key() {
        return "biome";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        Biome biome = target.toLocation(mine.getWorld()).getBlock().getBiome();
        return biomes.contains(biome.key());
    }

    @Override
    public @NonNull Material icon() {
        return Material.OAK_SAPLING;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.biome.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.biome",
                        MessageReplacement.replace("%biomes%", String.join(", ", getStringBiomes())));
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
