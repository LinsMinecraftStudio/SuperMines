package io.github.lijinhong11.supermines.managers.abstracts;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.mdatabase.sql.conditions.Condition;
import io.github.lijinhong11.supermines.SuperMines;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabaseObjectManager<T> {
    private final DatabaseConnection connection;
    private final Class<T> clazz;

    public AbstractDatabaseObjectManager(DatabaseConnection connection, Class<T> clazz) {
        this.connection = connection;
        this.clazz = clazz;

        setup();
    }

    private void setup() {
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

    protected void saveObject(@NotNull T t) {
        try {
            connection.insertObject(clazz, t);
        } catch (SQLException e) {
            SuperMines.getInstance().getLogger().severe("""
                    Failed to create/load player data table!
                    The plugin will disabled...
                    """);
            Bukkit.getPluginManager().disablePlugin(SuperMines.getInstance());
        }
    }

    protected void saveObject(@NotNull T t, @NotNull Condition condition) {
        try {
            connection.upsertObject(clazz, t, condition);
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

    public abstract void saveAndClose();

    protected void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
