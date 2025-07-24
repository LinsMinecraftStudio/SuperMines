package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.mdatabase.sql.conditions.Conditions;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractDatabaseObjectManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager extends AbstractDatabaseObjectManager<PlayerData> {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerDataManager(DatabaseConnection connection) {
        super(connection, PlayerData.class);

        load();
    }

    private void load() {
        for (PlayerData object : super.getAll()) {
            playerDataMap.put(object.getPlayerUUID(), object);
        }
    }

    public PlayerData getPlayerData(String name) {
        for (PlayerData object : playerDataMap.values()) {
            if (object.getPlayerName().equals(name)) {
                return object;
            }
        }
        return null;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    @Override
    public void saveAndClose() {
        for (PlayerData playerData : playerDataMap.values()) {
            super.saveObject(playerData, Conditions.eq("player_uuid", playerData.getPlayerUUID().toString()));
        }

        super.close();
    }
}
