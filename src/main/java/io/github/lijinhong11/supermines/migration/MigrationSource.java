package io.github.lijinhong11.supermines.migration;

import io.github.lijinhong11.supermines.migration.impl.CataMinesMigrator;
import io.github.lijinhong11.supermines.migration.impl.MineResetLiteMigrator;

public enum MigrationSource {
    CATAMINES("CataMines", new CataMinesMigrator()),
    MINERESETLITE("MineResetLite", new MineResetLiteMigrator());

    private final String pluginName;
    private final AbstractMineMigrator migrator;

    MigrationSource(String pluginName, AbstractMineMigrator migrator) {
        this.pluginName = pluginName;
        this.migrator = migrator;
    }

    public String getPluginName() {
        return pluginName;
    }

    public AbstractMineMigrator getMigrator() {
        return migrator;
    }
}
