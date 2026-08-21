package io.github.lijinhong11.supermines.utils;

import io.github.lijinhong11.supermines.SuperMines;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Sounds {
    private static final Registry<Sound> REGISTRY = Registry.SOUNDS;

    public static @Nullable Sound getSound(@NotNull String key) {
        NamespacedKey k = NamespacedKey.fromString(key);
        if (k == null) {
            SuperMines.getInstance().getLogger().severe(key + " doesn't represents a namespaced key");
            return null;
        }

        return getSound(k);
    }

    public static @Nullable Sound getSound(Key key) {
        if (key == null) {
            return null;
        }

        return REGISTRY.get(key);
    }

    public static @Nullable Key getSoundKey(@NotNull Sound sound) {
        return REGISTRY.getKey(sound);
    }
}
