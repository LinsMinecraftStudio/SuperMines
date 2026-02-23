package io.github.lijinhong11.supermines;

import com.tcoded.folialib.FoliaLib;
import io.github.lijinhong11.mdatabase.DatabaseConnection;
import io.github.lijinhong11.mdatabase.DatabaseParameters;
import io.github.lijinhong11.mdatabase.enums.DatabaseType;
import io.github.lijinhong11.mdatabase.impl.DatabaseConnections;
import io.github.lijinhong11.mittellib.utils.ConfigFileUtil;
import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import io.github.lijinhong11.supermines.integrates.placeholders.MiniPlaceholderExtension;
import io.github.lijinhong11.supermines.integrates.placeholders.PlaceholderAPIExtension;
import io.github.lijinhong11.supermines.listeners.BlockListener;
import io.github.lijinhong11.supermines.listeners.PlayerListener;
import io.github.lijinhong11.supermines.listeners.WandListener;
import io.github.lijinhong11.supermines.listeners.WorldEditListener;
import io.github.lijinhong11.supermines.managers.MineManager;
import io.github.lijinhong11.supermines.managers.PlayerDataManager;
import io.github.lijinhong11.supermines.managers.RankManager;
import io.github.lijinhong11.supermines.managers.TreasureManager;
import io.github.lijinhong11.supermines.message.LanguageManagerEx;
import io.github.lijinhong11.supermines.task.TaskMaker;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.Metrics;
import java.io.File;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class SuperMines extends JavaPlugin {
    private static SuperMines instance;

    private MineManager mineManager;
    private TreasureManager treasureManager;
    private RankManager rankManager;
    private PlayerDataManager playerDataManager;

    private LanguageManagerEx languageManager;

    private FoliaLib foliaLibImpl;
    private TaskMaker taskMaker;

    public static SuperMines getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        foliaLibImpl = new FoliaLib(this);

        ConfigFileUtil.completeFile(this, "config.yml");
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

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
        } catch (Exception e) {
            getLogger()
                    .info(
                            """

                    ==============================
                    SuperMines detected that you are using Spigot server software.
                    Some important features will not work!!!!
                    You may experience some errors, but I'm sorry.
                    SuperMines suggest you to change to Paper.

                    Why?
                    Paper has a lot of performance improvements and a lot benefits.
                    Some developers are changed to use Paper to develop their plugins.

                    Download Paper and improve your server!

                    You can download Paper @ https://papermc.io/downloads/paper
                    ==============================
                    """);
        }

        languageManager = new LanguageManagerEx(this);

        treasureManager = new TreasureManager();
        rankManager = new RankManager();
        mineManager = new MineManager();
        taskMaker = new TaskMaker(foliaLibImpl);

        setupDatabase();
        setupListeners();
        setupPlaceholders();

        new SuperMinesCommand().register();

        new Metrics(this, 28631);

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

        DatabaseConnection conn = DatabaseConnections.createByType(
                type,
                new File(getDataFolder(), Constants.Texts.DATABASE_FILE),
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

    public LanguageManagerEx getLanguageManager() {
        return languageManager;
    }

    public TaskMaker getTaskMaker() {
        return taskMaker;
    }
}
