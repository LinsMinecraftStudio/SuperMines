package io.github.lijinhong11.supermines.api.mine.generation.conditions;

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

public final class MineYGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final int minYInMine;
    private final int maxYInMine;

    @Override
    public @NonNull String key() {
        return "mineY";
    }

    public MineYGenerateCondition(ConfigurationSection cs) {
        this.minYInMine = cs.getInt("minYInMine");
        this.maxYInMine = cs.getInt("maxYInMine");
    }

    public MineYGenerateCondition(int minYInMine, int maxYInMine) {
        this.minYInMine = minYInMine;
        this.maxYInMine = maxYInMine;
    }

    public int getMinYInMine() {
        return minYInMine;
    }

    public int getMaxYInMine() {
        return maxYInMine;
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        AreaOfBlocks area = mine.getArea();
        int mineHeight = 0;
        int mineY = -512;
        if (area instanceof CuboidArea ca) {
            mineHeight = ca.getMax().y() - ca.getMin().y();
            mineY = ca.getMin().y();
        } else if (area instanceof SphereArea(BlockPos center, int radius)) {
            mineHeight = radius * 2 + 1;
            mineY = center.y() - radius;
        }

        if (mineHeight == 0 && mineY == -512) {
            throw new UnsupportedOperationException("Unsupported AreaOfBlocks implementation");
        }

        return target.y() - mineY >= minYInMine && target.y() - mineY <= maxYInMine;
    }

    @Override
    public @NonNull Material icon() {
        return Material.LADDER;
    }

    @Override
    public @NonNull Component getDisplayName(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponent(player, "gui.mine-management.block_spawn_entries.conditions.types.mineY.name");
    }

    @Override
    public @NonNull List<Component> getLore(@NotNull Player player) {
        return SuperMines.getInstance()
                .getLanguageManager()
                .getMsgComponentList(
                        player,
                        "gui.mine-management.block_spawn_entries.conditions.description.mineY",
                        MessageReplacement.replace("%min%", String.valueOf(minYInMine)),
                        MessageReplacement.replace("%max%", String.valueOf(maxYInMine)));
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("minYInMine", minYInMine);
        cs.set("maxYInMine", maxYInMine);
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }
}
