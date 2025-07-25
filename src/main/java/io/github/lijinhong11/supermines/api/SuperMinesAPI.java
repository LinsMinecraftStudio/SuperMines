package io.github.lijinhong11.supermines.api;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SuperMinesAPI {
    public static @Nullable Mine getMine(String id) {
        return SuperMines.getInstance().getMineManager().getMine(id);
    }

    public static @Nullable Mine getMine(Location loc) {
        return SuperMines.getInstance().getMineManager().getMine(loc);
    }

    public static @Nullable Treasure getTreasure(String id) {
        return SuperMines.getInstance().getTreasureManager().getTreasure(id);
    }

    public static @Nullable Rank getRank(String id) {
        return SuperMines.getInstance().getRankManager().getRank(id);
    }

    public static @Nullable PlayerData getPlayerData(String name) {
        return SuperMines.getInstance().getPlayerDataManager().getPlayerData(name);
    }

    public static @NotNull PlayerData getOrCreatePlayerData(@NotNull UUID playerUUID) {
        return SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(playerUUID);
    }

    public static void addMine(@NotNull Mine mine) {
        SuperMines.getInstance().getMineManager().addMine(mine);
    }

    public static void removeMine(@NotNull String id) {
        SuperMines.getInstance().getMineManager().removeMine(id);
    }

    public static void addTreasure(@NotNull Treasure treasure) {
        SuperMines.getInstance().getTreasureManager().addTreasure(treasure);
    }

    public static void removeTreasure(@NotNull String id) {
        SuperMines.getInstance().getTreasureManager().removeTreasure(id);
    }

    public static void addRank(@NotNull Rank rank) {
        SuperMines.getInstance().getRankManager().addRank(rank);
    }

    public static void removeRank(@NotNull String id) {
        SuperMines.getInstance().getRankManager().removeRank(id);
    }
}
