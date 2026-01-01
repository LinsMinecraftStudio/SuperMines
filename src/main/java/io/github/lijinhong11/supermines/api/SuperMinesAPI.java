package io.github.lijinhong11.supermines.api;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * The main API class for SuperMines plugin.
 */
public class SuperMinesAPI {
    private SuperMinesAPI() {
        throw new IllegalStateException(); // no one loves do that, right?
    }

    /**
     * Gets a mine by its ID.
     *
     * @param id the mine ID
     * @return the mine with the specified ID, or null if not found
     */
    public static @Nullable Mine getMine(String id) {
        return SuperMines.getInstance().getMineManager().getMine(id);
    }

    /**
     * Gets a mine at the specified location.
     *
     * @param loc the location to check
     * @return the mine at the specified location, or null if no mine exists at that
     * location
     */
    public static @Nullable Mine getMine(Location loc) {
        return SuperMines.getInstance().getMineManager().getMine(loc);
    }

    /**
     * Gets all registered mines.
     *
     * @return a collection containing all mines
     */
    public static @NotNull Collection<Mine> getMines() {
        return SuperMines.getInstance().getMineManager().getAllMines();
    }

    /**
     * Gets a treasure by its ID.
     *
     * @param id the treasure ID
     * @return the treasure with the specified ID, or null if not found
     */
    public static @Nullable Treasure getTreasure(String id) {
        return SuperMines.getInstance().getTreasureManager().getTreasure(id);
    }

    /**
     * Gets all registered treasures.
     *
     * @return a collection containing all treasures
     */
    public static @NotNull Collection<Treasure> getTreasures() {
        return SuperMines.getInstance().getTreasureManager().getAllTreasures();
    }

    /**
     * Gets a rank.
     * <p>
     * Note: You can't get default rank from it. See {@link Rank#DEFAULT}
     *
     * @return a rank (the default rank is not gettable)
     */
    public static @Nullable Rank getRank(String id) {
        return SuperMines.getInstance().getRankManager().getRank(id);
    }

    /**
     * Gets all ranks. (the default rank is not included)
     *
     * @return a collection, which contains all ranks (the default rank is not
     * included)
     */
    public static @NotNull Collection<Rank> getRanks() {
        return SuperMines.getInstance().getRankManager().getAllRanks();
    }

    /**
     * Gets player data by player name.
     *
     * @param name the player name
     * @return the player data, or null if not found
     */
    public static @Nullable PlayerData getPlayerData(@NotNull String name) {
        return SuperMines.getInstance().getPlayerDataManager().getPlayerData(name);
    }

    /**
     * Gets or creates player data for the specified player UUID.
     * <p>
     * If the player data does not exist, it will be created with default values.
     *
     * @param playerUUID the player UUID
     * @return the player data (never null)
     */
    public static @NotNull PlayerData getOrCreatePlayerData(@NotNull UUID playerUUID) {
        return SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(playerUUID);
    }

    /**
     * Adds a new mine to the system.
     *
     * @param mine the mine to add
     * @throws IllegalArgumentException if a mine with the same ID already exists
     */
    public static void addMine(@NotNull Mine mine) {
        SuperMines.getInstance().getMineManager().addMine(mine);
    }

    /**
     * Removes a mine from the system.
     *
     * @param id the ID of the mine to remove
     */
    public static void removeMine(@NotNull String id) {
        SuperMines.getInstance().getMineManager().removeMine(id);
    }

    /**
     * Adds a new treasure to the system.
     *
     * @param treasure the treasure to add
     * @throws IllegalArgumentException if a treasure with the same ID already
     *                                  exists
     */
    public static void addTreasure(@NotNull Treasure treasure) {
        SuperMines.getInstance().getTreasureManager().addTreasure(treasure);
    }

    /**
     * Removes a treasure from the system.
     *
     * @param id the ID of the treasure to remove
     */
    public static void removeTreasure(@NotNull String id) {
        SuperMines.getInstance().getTreasureManager().removeTreasure(id);
    }

    /**
     * Adds a new rank to the system.
     *
     * @param rank the rank to add
     * @throws IllegalArgumentException if a rank with the same ID already exists
     */
    public static void addRank(@NotNull Rank rank) {
        SuperMines.getInstance().getRankManager().addRank(rank);
    }

    /**
     * Removes a rank from the system.
     *
     * @param id the ID of the rank to remove
     */
    public static void removeRank(@NotNull String id) {
        SuperMines.getInstance().getRankManager().removeRank(id);
    }
}
