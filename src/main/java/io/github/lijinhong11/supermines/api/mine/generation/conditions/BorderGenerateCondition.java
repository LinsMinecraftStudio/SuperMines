package io.github.lijinhong11.supermines.api.mine.generation.conditions;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.AreaOfBlocks;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.iface.IGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A condition that only lets blocks generate on the rim (edge) or in the core (interior) of the
 * mine area.
 *
 * <p>Config keys:
 *
 * <pre>{@code
 * condition: border
 * mode: RIM
 * }</pre>
 *
 * <p>{@code mode} accepts {@code RIM} (default) or {@code CORE}.
 */
public class BorderGenerateCondition implements IGenerateCondition, ReadWriteObject {
    private final Mode mode;

    public BorderGenerateCondition(ConfigurationSection cs) {
        this(Mode.valueOf(cs.getString("mode", "RIM")));
    }

    public BorderGenerateCondition(Mode mode) {
        Preconditions.checkNotNull(mode, "mode cannot be null");

        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public String key() {
        return "border";
    }

    @Override
    public boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target) {
        boolean onRim = isOnRim(mine.getArea(), target);
        return mode == Mode.RIM ? onRim : !onRim;
    }

    private static boolean isOnRim(AreaOfBlocks area, BlockPos target) {
        return !area.contains(target.plus(1, 0, 0))
                || !area.contains(target.plus(-1, 0, 0))
                || !area.contains(target.plus(0, 0, 1))
                || !area.contains(target.plus(0, 0, -1));
    }

    @Override
    public void write(ConfigurationSection cs) {
        cs.set("mode", mode.name());
    }

    @Override
    public void read(ConfigurationSection cs) {
        throw new UnsupportedOperationException();
    }

    public enum Mode {
        RIM,
        CORE
    }
}
