package io.github.lijinhong11.supermines.api.iface;

import io.github.lijinhong11.mittellib.configuration.ReadWriteObject;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.supermines.api.mine.Mine;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IGenerateCondition extends ReadWriteObject {
    @NotNull String key();

    boolean canGenerate(@NotNull Mine mine, @NotNull BlockPos target);

    @NotNull Material icon();

    /**
     * The display name shown in edit GUIs, filled in by the provider.
     *
     * @param player the viewing player
     * @return the display name component
     */
    @NotNull Component getDisplayName(@NotNull Player player);

    /**
     * The lore (description) shown in edit GUIs, filled in by the provider.
     *
     * @param player the viewing player
     * @return the lore lines
     */
    @NotNull List<Component> getLore(@NotNull Player player);
}
