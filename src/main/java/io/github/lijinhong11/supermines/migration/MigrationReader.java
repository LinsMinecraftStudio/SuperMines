package io.github.lijinhong11.supermines.migration;

import io.github.lijinhong11.supermines.migration.AbstractMineMigrator.MineData;
import java.io.Reader;
import java.util.Map;

/**
 * Delegates mine reading to the migrator registered for the requested source.
 */
public final class MigrationReader {
    private MigrationReader() {}

    public static MineData read(Reader reader, MigrationSource source) {
        return source.getMigrator().read(reader);
    }

    public static MineData read(Reader reader, MigrationSource source, Map<?, ?> sourceConfig) {
        return source.getMigrator().read(reader, sourceConfig);
    }
}
