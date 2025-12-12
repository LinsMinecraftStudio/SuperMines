package io.github.lijinhong11.supermines.api.data;

import io.github.lijinhong11.mdatabase.serialization.annotations.Column;
import io.github.lijinhong11.mdatabase.serialization.annotations.Converter;
import io.github.lijinhong11.mdatabase.serialization.annotations.PrimaryKey;
import io.github.lijinhong11.mdatabase.serialization.annotations.Table;
import io.github.lijinhong11.supermines.managers.database.RankConverter;
import io.github.lijinhong11.supermines.managers.database.StringRankSet;

import java.util.UUID;

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
    private StringRankSet rank;

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

    public StringRankSet getRank() {
        return rank;
    }

    public void setRank(StringRankSet rank) {
        this.rank = rank;
    }

    public void addRank(Rank rank) {
        this.rank.add(rank);
    }

    public int getMinedBlocks() {
        return minedBlocks;
    }

    public void addMinedBlocks(int amount) {
        this.minedBlocks += amount;
    }
}
