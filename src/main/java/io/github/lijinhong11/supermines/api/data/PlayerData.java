package io.github.lijinhong11.supermines.api.data;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mdatabase.serialization.annotations.Column;
import io.github.lijinhong11.mdatabase.serialization.annotations.Converter;
import io.github.lijinhong11.mdatabase.serialization.annotations.PrimaryKey;
import io.github.lijinhong11.mdatabase.serialization.annotations.Table;
import io.github.lijinhong11.supermines.managers.database.RankConverter;
import io.github.lijinhong11.supermines.managers.database.StringRankSet;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents player data stored in the database, including mining statistics
 * and rank information.
 * <p>
 * Note: This is an important database object. So do not use reflection to edit
 * it anyway.
 */
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

    public PlayerData() {}

    public PlayerData(String playerName, UUID playerUUID, StringRankSet rank) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.rank = rank;
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
        this.minedBlocks += amount;
    }
}
