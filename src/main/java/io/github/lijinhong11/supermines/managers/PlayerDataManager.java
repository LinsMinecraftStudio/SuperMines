package io.github.lijinhong11.supermines.managers;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractDatabaseObjectManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable public PlayerData getPlayerData(String name) {
        for (PlayerData object : playerDataMap.values()) {
            if (object.getPlayerName().equals(name)) {
                return object;
            }
        }

        return null;
    }

    public @Nullable PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public @NotNull PlayerData getOrCreatePlayerData(@NotNull UUID playerUUID) {
        PlayerData playerData = getPlayerData(playerUUID);

        if (playerData == null) {
            playerData = new PlayerData(Bukkit.getOfflinePlayer(playerUUID).getName(), playerUUID, Rank.DEFAULT);
            super.saveObject(playerData);
            playerDataMap.put(playerUUID, playerData);
        }

        return playerData;
    }

    @Override
    public void saveAndClose() {
        for (PlayerData playerData : playerDataMap.values()) {
            super.saveObject(playerData);
        }

        super.close();
    }
}
