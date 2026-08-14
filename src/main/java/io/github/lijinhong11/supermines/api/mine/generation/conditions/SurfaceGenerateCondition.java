package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.AreaOfBlocks;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.math.SphereArea;
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
 * A condition that only lets blocks generate within a given distance from the top surface of the
 * mine, so ores can be restricted to the visible surface layer.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: surface
 * depth: 1
 * }</pre>
 */
public record SurfaceGenerateCondition(int depth) implements IGenerateCondition, ReadWriteObject {
    public SurfaceGenerateCondition(ConfigurationSection cs) {
        this(cs.getInt("depth", 1));
    }

    public SurfaceGenerateCondition {
        Preconditions.checkArgument(depth >= 1, "depth must be greater than or equal to 1");
    }

    @Override
    public @NonNull String key() {
        return "surface";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        int topY = getTopY(mine);
        return target.y() > topY - depth;
    }

    @Override
    public @NonNull Material icon() {
        return Material.GRASS_BLOCK;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.surface.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.surface",
                        MessageReplacement.replace("%depth%", String.valueOf(depth)));
    }

    private static int getTopY(Mine mine) {
        AreaOfBlocks area = mine.getArea();
        if (area instanceof CuboidArea ca) {
            return ca.getMax().y();
        } else if (area instanceof SphereArea(BlockPos center, int radius)) {
            return center.y() + radius;
        }

        throw new UnsupportedOperationException("Unsupported AreaOfBlocks implementation");
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("depth", depth);
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
