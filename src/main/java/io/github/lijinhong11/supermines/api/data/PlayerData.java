package io.github.lijinhong11.supermines.api.data;

import io.github.lijinhong11.mdatabase.serialization.annotations.Column;
import io.github.lijinhong11.mdatabase.serialization.annotations.Converter;
import io.github.lijinhong11.mdatabase.serialization.annotations.Table;
import io.github.lijinhong11.supermines.managers.database.RankConverter;

import java.util.UUID;

@Table(name = "player_data")
public class PlayerData {
    @Column
    private String playerName;
    @Column
    private UUID playerUUID;
    @Column
    private int minedBlocks;
    @Converter(RankConverter.class)
    private Rank rank;

    public PlayerData() {
    }

    public PlayerData(String playerName, UUID playerUUID, Rank rank) {
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

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public int getMinedBlocks() {
        return minedBlocks;
    }

    public void setMinedBlocks(int minedBlocks) {
        this.minedBlocks = minedBlocks;
    }
}
