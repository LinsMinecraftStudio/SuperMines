package io.github.lijinhong11.supermines.migration;

public enum MigrationSource {
    CATAMINES("CataMines"),
    MINERESETLITE("MineResetLite");

    private final String pluginName;

    MigrationSource(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
