package io.github.lijinhong11.supermines.managers.abstracts;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseObjectManager<T> {
    private final DatabaseConnection connection;
    private final Class<T> clazz;

    public AbstractDatabaseObjectManager(DatabaseConnection connection, Class<T> clazz) {
        this.connection = connection;
        this.clazz = clazz;

        load();
    }

    private void load() {
        try {
            connection.createTableByClass(clazz);
        } catch (SQLException e) {
            SuperMines.getInstance().getLogger().severe("""
                    Failed to create/load player data table!
                    The plugin will disabled...
                    """);
            Bukkit.getPluginManager().disablePlugin(SuperMines.getInstance());
        }
    }

    protected final List<T> getAll() {
        try {
            return connection.selectMulti(clazz);
        } catch (SQLException e) {
            SuperMines.getInstance().getLogger().severe("""
                    Failed to create/load player data table!
                    The plugin will disabled...
                    """);
            Bukkit.getPluginManager().disablePlugin(SuperMines.getInstance());
        }

        return new ArrayList<>();
    }
}
