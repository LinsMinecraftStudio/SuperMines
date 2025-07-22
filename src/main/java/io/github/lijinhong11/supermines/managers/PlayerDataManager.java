package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractDatabaseObjectManager;

public class PlayerDataManager extends AbstractDatabaseObjectManager<PlayerData> {
    public PlayerDataManager(DatabaseConnection connection) {
        super(connection, PlayerData.class);
    }

    @Override
    protected void setup() {

    }
}
