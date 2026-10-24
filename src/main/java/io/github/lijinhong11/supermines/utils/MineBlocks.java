package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Resolves mine entries, including vanilla block states and air from migrated mines. */
public final class MineBlocks {
    private MineBlocks() {}

    public static @Nullable PackedBlock resolve(String id) {
        String normalized = id.toLowerCase(Locale.ROOT);
        if (!normalized.contains(":") || normalized.startsWith("minecraft:")) {
            try {
                return new VanillaBlock(Bukkit.createBlockData(normalized));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return ContentProviders.getBlock(id);
    }

    private record VanillaBlock(BlockData data) implements PackedBlock {
        @Override
        public void place(@NotNull Location location) {
            location.getBlock().setBlockData(data.clone(), false);
        }

        @Override
        public @NotNull String getId() {
            return data.getAsString(true);
        }

        @Override
        public ItemStack toItem() {
            return data.getMaterial().isItem() ? new ItemStack(data.getMaterial()) : null;
        }
    }
}
