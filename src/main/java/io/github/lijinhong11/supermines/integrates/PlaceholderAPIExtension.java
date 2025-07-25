package io.github.lijinhong11.supermines.integrates;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIExtension extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "supermines";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mmmjjkx";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");

        return "";
    }
}
