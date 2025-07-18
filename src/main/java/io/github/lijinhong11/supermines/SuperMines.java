package io.github.lijinhong11.supermines;

import io.github.lijinhong11.supermines.managers.MineManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperMines extends JavaPlugin {
    private static SuperMines instance;

    private MineManager mineManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        mineManager = new MineManager();
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
}
