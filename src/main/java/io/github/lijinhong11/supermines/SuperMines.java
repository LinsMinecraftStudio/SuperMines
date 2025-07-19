package io.github.lijinhong11.supermines;

import com.tcoded.folialib.FoliaLib;
import io.github.lijinhong11.supermines.managers.MineManager;
import io.github.lijinhong11.supermines.managers.TreasureManager;
import io.github.lijinhong11.supermines.message.LanguageManager;
import io.github.lijinhong11.supermines.task.TaskMaker;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperMines extends JavaPlugin {
    private static SuperMines instance;

    private MineManager mineManager;
    private TreasureManager treasureManager;
    private LanguageManager languageManager;
    private FoliaLib foliaLibImpl;
    private TaskMaker taskMaker;

    @Override
    public void onLoad() {
        instance = this;
        foliaLibImpl = new FoliaLib(this);
    }

    @Override
    public void onEnable() {
        mineManager = new MineManager();
        treasureManager = new TreasureManager();
        languageManager = new LanguageManager(this);
        taskMaker = new TaskMaker(foliaLibImpl);


        taskMaker.startup();
    }

    @Override
    public void onDisable() {

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

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public TaskMaker getTaskMaker() {
        return taskMaker;
    }
}
