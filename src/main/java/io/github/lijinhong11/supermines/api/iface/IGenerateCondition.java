package io.github.lijinhong11.supermines.api.iface;

import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.jetbrains.annotations.NotNull;

public interface IGenerateCondition extends ReadWriteObject {
    String key();

    boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target);
}
