package io.github.lijinhong11.supermines.api.data;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mdatabase.serialization.annotations.*;
import io.github.lijinhong11.supermines.managers.database.RankConverter;
import io.github.lijinhong11.supermines.managers.database.StringRankSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents player data stored in the database, including mining statistics
 * and rank information.
 * <p>
 * Note: This is an important database object. So do not use reflection to edit
 * it anyway.
 */
@AutoMigrate
@Table(name = "player_data")
public final class PlayerData {
    @Column(name = "player_uuid")
    @PrimaryKey
    private UUID playerUUID;

    @Column(name = "player_name")
    private String playerName;

    @Column(name = "mined_blocks")
    private int minedBlocks;

    @Converter(RankConverter.class)
    @Column(name = "rank")
    private StringRankSet rank = new StringRankSet();

    @Column(name = "auto_pickup")
    private boolean autoPickup;

    @Column(name = "treasures_got")
    private Map<String, Integer> treasuresGot = new HashMap<>();

    @Column(name = "regen_point_mining_counts")
    private Map<String, Integer> regenPointMiningCounts = new HashMap<>();

    @Column(name = "regen_point_total_mining_counts")
    private Map<String, Integer> regenPointTotalMiningCounts = new HashMap<>();

    public PlayerData() {}

    public PlayerData(String playerName, UUID playerUUID, StringRankSet rank, boolean autoPickup) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.rank = rank;
        this.autoPickup = autoPickup;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public @NotNull StringRankSet getRank() {
        return rank;
    }

    public void setRank(@NotNull StringRankSet rank) {
        Preconditions.checkNotNull(rank, "rank");

        this.rank = rank;
    }

    public void addRank(Rank rank) {
        this.rank.add(rank);
    }

    public void removeRank(Rank rank) {
        this.rank.remove(rank);
    }

    public int getMinedBlocks() {
        return minedBlocks;
    }

    public void addMinedBlocks(int amount) {
        this.minedBlocks = Math.addExact((long) this.minedBlocks, amount) > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : this.minedBlocks + amount;
    }

    public boolean isAutoPickup() {
        return autoPickup;
    }

    public void setAutoPickup(boolean autoPickup) {
        this.autoPickup = autoPickup;
    }

    public int getTreasuresGot(@NotNull String treasureId) {
        return treasuresGot.getOrDefault(treasureId, 0);
    }

    public void addTreasureGot(@NotNull String treasureId) {
        treasuresGot.merge(treasureId, 1, Integer::sum);
    }

    public int addRegenPointMining(@NotNull String pointId) {
        return regenPointMiningCounts.compute(
                pointId, (key, value) -> value == null || value == Integer.MAX_VALUE ? Integer.MAX_VALUE : value + 1);
    }

    public void addRegenPointTotalMining(@NotNull String pointId) {
        regenPointTotalMiningCounts.compute(
                pointId, (key, value) -> value == null || value == Integer.MAX_VALUE ? Integer.MAX_VALUE : value + 1);
    }

    public int getRegenPointTotalMining(@NotNull String pointId) {
        return regenPointTotalMiningCounts.getOrDefault(pointId, 0);
    }

    public void resetRegenPointMining(@NotNull String pointId) {
        regenPointMiningCounts.put(pointId, 0);
    }

    public int getRegenPointMining(@NotNull String pointId) {
        return regenPointMiningCounts.getOrDefault(pointId, 0);
    }
}
