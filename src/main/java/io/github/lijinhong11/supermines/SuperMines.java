package io.github.lijinhong11.supermines;

import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.mdatabase.DatabaseParameters;
import io.github.lijinhong11.mdatabase.enums.DatabaseType;
import io.github.lijinhong11.mdatabase.impl.DatabaseConnections;
import io.github.lijinhong11.mittellib.MittelLib;
import io.github.lijinhong11.mittellib.message.SyncLanguageManager;
import io.github.lijinhong11.mittellib.utils.ConfigFileUtils;
import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import io.github.lijinhong11.supermines.integrates.placeholders.SuperMinesPlaceholders;
import io.github.lijinhong11.supermines.listeners.BlockListener;
import io.github.lijinhong11.supermines.listeners.PlayerListener;
import io.github.lijinhong11.supermines.listeners.WandListener;
import io.github.lijinhong11.supermines.listeners.WorldEditListener;
import io.github.lijinhong11.supermines.managers.MineManager;
import io.github.lijinhong11.supermines.managers.PlayerDataManager;
import io.github.lijinhong11.supermines.managers.RankManager;
import io.github.lijinhong11.supermines.managers.RegenPointManager;
import io.github.lijinhong11.supermines.managers.TreasureManager;
import io.github.lijinhong11.supermines.task.TaskMaker;
import io.github.lijinhong11.supermines.utils.Constants;
import java.io.File;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class SuperMines extends JavaPlugin {
    private static SuperMines instance;

    private MineManager mineManager;
    private TreasureManager treasureManager;
    private RankManager rankManager;
    private RegenPointManager regenPointManager;
    private PlayerDataManager playerDataManager;

    private SyncLanguageManager languageManager;

    private TaskMaker taskMaker;

    public static SuperMines getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        ConfigFileUtils.completeFile(this, "config.yml");
    }

    @Override
    public void onEnable() {
        getLogger()
                .info(
                        """

                ==============================
                       SuperMines v%s
                        Author: mmmjjkx
                           Enjoy :)
                ==============================
                """
                                .formatted(getDescription().getVersion()));

        languageManager = MittelLib.getInstance().getLanguageManager(this);

        treasureManager = new TreasureManager();
        rankManager = new RankManager();
        regenPointManager = new RegenPointManager();
        mineManager = new MineManager();
        taskMaker = new TaskMaker();

        setupDatabase();
        setupListeners();
        setupPlaceholders();

        SuperMinesCommand.register(this);

        new Metrics(this, 28631);

        taskMaker.startup();
        regenPointManager.startup();
    }

    @Override
    public void onDisable() {
        taskMaker.close();

        mineManager.saveAndClose();
        treasureManager.saveAndClose();
        rankManager.saveAndClose();
        regenPointManager.saveAndClose();
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

        DatabaseConnection conn = DatabaseConnections.createByType(
                type,
                new File(getDataFolder(), Constants.DATABASE_FILE),
                ip,
                port,
                database,
                username,
                password,
                new DatabaseParameters());

        playerDataManager = new PlayerDataManager(conn);
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new WandListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (getServer().getPluginManager().isPluginEnabled("WorldEdit")
                || getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            new WorldEditListener();
        }
    }

    private void setupPlaceholders() {
        new SuperMinesPlaceholders().register();
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

    public RegenPointManager getRegenPointManager() {
        return regenPointManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public SyncLanguageManager getLanguageManager() {
        return languageManager;
    }

    public TaskMaker getTaskMaker() {
        return taskMaker;
    }
}
