package io.github.lijinhong11.supermines;

import com.tcoded.folialib.FoliaLib;
import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.mdatabase.DatabaseParameters;
import io.github.lijinhong11.mdatabase.enums.DatabaseType;
import io.github.lijinhong11.mdatabase.impl.SQLConnections;
import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import io.github.lijinhong11.supermines.integrates.MiniPlaceholderExtension;
import io.github.lijinhong11.supermines.integrates.PlaceholderAPIExtension;
import io.github.lijinhong11.supermines.listeners.BlockListener;
import io.github.lijinhong11.supermines.listeners.PlayerListener;
import io.github.lijinhong11.supermines.listeners.WandListener;
import io.github.lijinhong11.supermines.listeners.WorldEditListener;
import io.github.lijinhong11.supermines.managers.MineManager;
import io.github.lijinhong11.supermines.managers.PlayerDataManager;
import io.github.lijinhong11.supermines.managers.RankManager;
import io.github.lijinhong11.supermines.managers.TreasureManager;
import io.github.lijinhong11.supermines.message.LanguageManager;
import io.github.lijinhong11.supermines.task.TaskMaker;
import io.github.lijinhong11.supermines.utils.Constants;
import java.io.File;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperMines extends JavaPlugin {
    private static SuperMines instance;

    private MineManager mineManager;
    private TreasureManager treasureManager;
    private RankManager rankManager;
    private PlayerDataManager playerDataManager;

    private LanguageManager languageManager;
    private FoliaLib foliaLibImpl;
    private TaskMaker taskMaker;

    @Override
    public void onLoad() {
        instance = this;

        saveDefaultConfig();
        saveConfig();

        foliaLibImpl = new FoliaLib(this);
    }

    @Override
    public void onEnable() {
        languageManager = new LanguageManager(this);

        treasureManager = new TreasureManager();
        mineManager = new MineManager();
        rankManager = new RankManager();
        taskMaker = new TaskMaker(foliaLibImpl);

        new SuperMinesCommand().register();

        setupDatabase();
        setupListeners();
        setupPlaceholders();

        taskMaker.startup();
    }

    @Override
    public void onDisable() {
        taskMaker.close();

        mineManager.saveAndClose();
        treasureManager.saveAndClose();
        rankManager.saveAndClose();
        playerDataManager.saveAndClose();
    }

    private void setupDatabase() {
        ConfigurationSection storage = getConfig().createSection("storage");
        ConfigurationSection remote = storage.createSection("remote");

        DatabaseType type = DatabaseType.getByName(storage.getString("type", "SQLITE"));

        if (type == null) {
            getLogger().warning("Invalid storage type, using SQLite instead.");
            type = DatabaseType.SQLITE;
        }

        String ip = remote.getString("ip");
        int port = remote.getInt("port");
        String database = remote.getString("database");
        String username = remote.getString("username");
        String password = remote.getString("password");

        DatabaseConnection conn =
                switch (type) {
                    case SQLITE -> {
                        String path = new File(getDataFolder(), Constants.StringsAndComponents.DATABASE_FILE)
                                .getAbsolutePath();
                        yield SQLConnections.sqlite(path, new DatabaseParameters());
                    }
                    case MYSQL ->
                        SQLConnections.mysql(ip, port, database, username, password, new DatabaseParameters());
                    case MARIADB ->
                        SQLConnections.mariadb(ip, port, database, username, password, new DatabaseParameters());
                    case POSTGRESQL ->
                        SQLConnections.postgresql(ip, port, database, username, password, new DatabaseParameters());
                };

        playerDataManager = new PlayerDataManager(conn);
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new WandListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
            new WorldEditListener();
        }
    }

    private void setupPlaceholders() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExtension().register();
        }

        if (getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            MiniPlaceholderExtension.register();
        }
    }

    public static SuperMines getInstance() {
        return instance;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public TaskMaker getTaskMaker() {
        return taskMaker;
    }
}
