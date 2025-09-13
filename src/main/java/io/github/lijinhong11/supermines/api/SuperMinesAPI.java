package io.github.lijinhong11.supermines.api;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;

import java.util.Collection;
import java.util.UUID;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperMinesAPI {
    public static @Nullable Mine getMine(String id) {
        return SuperMines.getInstance().getMineManager().getMine(id);
    }

    public static @Nullable Mine getMine(Location loc) {
        return SuperMines.getInstance().getMineManager().getMine(loc);
    }

    public static @NotNull Collection<Mine> getMines() {
        return SuperMines.getInstance().getMineManager().getAllMines();
    }

    public static @Nullable Treasure getTreasure(String id) {
        return SuperMines.getInstance().getTreasureManager().getTreasure(id);
    }

    public static @NotNull Collection<Treasure> getTreasures() {
        return SuperMines.getInstance().getTreasureManager().getAllTreasures();
    }

    public static @Nullable Rank getRank(String id) {
        return SuperMines.getInstance().getRankManager().getRank(id);
    }

    /**
     * Get all ranks (the default rank is not included)
     * @return a collection, which contains all ranks (the default rank is not included)
     */
    public static @NotNull Collection<Rank> getRanks() {
        return SuperMines.getInstance().getRankManager().getAllRanks();
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
