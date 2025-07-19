package io.github.lijinhong11.supermines.api.data;

import java.util.UUID;

public class PlayerData {
    private final String playerName;
    private final UUID playerUUID;
    private Rank rank;

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
}
