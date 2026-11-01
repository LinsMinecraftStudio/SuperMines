package io.github.lijinhong11.supermines.migration;

import static org.junit.jupiter.api.Assertions.*;

import io.github.lijinhong11.supermines.api.mine.MessageBroadcastMode;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.error.YAMLException;

class MigrationReaderTest {
    private static final AbstractMineMigrator MRL = MigrationSource.MINERESETLITE.getMigrator();

    @Test
    void importsCataMinesWithoutLoadingSerializedPluginClasses() throws IOException {
        var mine = read(cata(), MigrationSource.CATAMINES);
        assertEquals("quarry_A", mine.id());
        assertEquals("world", mine.world());
        assertEquals(-32, mine.pos1().y());
        assertEquals(600, mine.resetSeconds());
        assertEquals(Set.of(60, 10), mine.warnings());
        assertEquals(75, mine.blocks().get("minecraft:stone"));
        assertEquals(25, mine.blocks().get("minecraft:oak_log[axis=x]"));
        assertTrue(mine.onlyFillAir());
        assertEquals(0.5, mine.teleport().x());
        assertEquals(90, mine.teleport().yaw());
        assertEquals(15, mine.teleport().pitch());
        assertTrue(mine.notes().isEmpty());
        assertNull(mine.messageBroadcastMode());
    }

    @Test
    void convertsMineResetLiteMinutesAndKeepsUnallocatedAir() throws IOException {
        var mine = read(mrl(), MigrationSource.MINERESETLITE);
        assertEquals("quarry_B", mine.id());
        assertEquals(600, mine.resetSeconds());
        assertEquals(Set.of(300, 60), mine.warnings());
        assertEquals(0.5, mine.blocks().get("1:0"));
        assertEquals(0.25, mine.blocks().get("35:14"));
        assertEquals(0.25, mine.blocks().get("minecraft:air"));
        assertTrue(mine.onlyFillAir());
        assertNull(mine.teleport());
        assertEquals(MessageBroadcastMode.ALL, mine.messageBroadcastMode());
    }

    @ParameterizedTest
    @CsvSource({
        "false,false,false,ALL",
        "false,true,false,IN_MINE",
        "false,false,true,IN_WORLD",
        "false,true,true,IN_MINE",
        "true,false,false,SILENT",
        "true,true,false,SILENT",
        "true,false,true,SILENT",
        "true,true,true,SILENT"
    })
    void importsMrlMessageModeWithTheOriginalPriority(
            boolean silent, boolean nearby, boolean worldOnly, MessageBroadcastMode expected) throws IOException {
        var config = Map.of("broadcast-nearby-only", nearby, "broadcast-in-world-only", worldOnly);
        var mine = MigrationReader.read(
                new StringReader(mrl() + "  isSilent: " + silent + "\n"), MigrationSource.MINERESETLITE, config);
        assertEquals(expected, mine.messageBroadcastMode());
        assertEquals(silent, mine.notes().contains("silent-warnings"));
        assertEquals(Set.of(300, 60), mine.warnings());
    }

    @Test
    void readsMrlGlobalConfigFromTheSourceDataFolder(@TempDir Path folder) throws IOException {
        Files.writeString(
                folder.resolve("config.yml"), "broadcast-in-world-only: true\nbroadcast-nearby-only: false\n");
        var config = MRL.readConfiguration(folder);
        var mine = MigrationReader.read(new StringReader(mrl()), MigrationSource.MINERESETLITE, config);
        assertEquals(MessageBroadcastMode.IN_WORLD, mine.messageBroadcastMode());
    }

    @Test
    void usesMrlDefaultsWhenGlobalConfigIsMissingOrEmpty(@TempDir Path folder) throws IOException {
        assertTrue(MRL.readConfiguration(folder).isEmpty());
        Files.writeString(folder.resolve("config.yml"), "# empty source configuration\n");
        var config = MRL.readConfiguration(folder);
        assertTrue(config.isEmpty());
        assertEquals(
                MessageBroadcastMode.ALL,
                MigrationReader.read(new StringReader(mrl()), MigrationSource.MINERESETLITE, config)
                        .messageBroadcastMode());
    }

    @Test
    void rejectsInvalidGlobalMessageConfigRatherThanBroadeningTheAudience(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("config.yml"), "broadcast-nearby-only: invalid\n");
        assertThrows(IllegalArgumentException.class, () -> MRL.readConfiguration(folder));
        Files.writeString(
                folder.resolve("config.yml"), "broadcast-in-world-only: true\nbroadcast-in-world-only: false\n");
        assertThrows(YAMLException.class, () -> MRL.readConfiguration(folder));
    }

    @Test
    void leavesCataMinesMessageModeAtTheSmDefault() throws IOException {
        var mine = MigrationReader.read(
                new StringReader(cata()), MigrationSource.CATAMINES, Map.of("broadcast-nearby-only", true));
        assertNull(mine.messageBroadcastMode());
    }

    @Test
    void keepsStoppedCataMineSchedulesDisabled() throws IOException {
        var mine = read(cata().replace("isStopped: false", "isStopped: true"), MigrationSource.CATAMINES);
        assertEquals(0, mine.resetSeconds());
        assertTrue(mine.warnings().isEmpty());
    }

    @Test
    void reportsPercentageResetsWithoutInventingATimer() throws IOException {
        var mine = read(cata().replace("resetMode: TIME", "resetMode: PERCENTAGE"), MigrationSource.CATAMINES);
        assertEquals(0, mine.resetSeconds());
        assertTrue(mine.notes().contains("percentage-reset"));
        var mixed = read(cata().replace("resetMode: TIME", "resetMode: TIME_PERCENTAGE"), MigrationSource.CATAMINES);
        assertEquals(600, mixed.resetSeconds());
        assertTrue(mixed.notes().contains("percentage-reset"));
    }

    @Test
    void importsModernMineResetLiteTeleportFieldsAndReportsUnsupportedSettings() throws IOException {
        var mine = read(mrl() + """
                  tpX: 12
                  tpY: 70
                  tpZ: 24
                  tpYaw: 180
                  tpPitch: 10
                  resetPercent: 0.2
                  structure: [BEDROCK]
                """, MigrationSource.MINERESETLITE);
        assertEquals(70, mine.teleport().y());
        assertEquals(180, mine.teleport().yaw());
        assertEquals(Set.of("percentage-reset", "structure"), Set.copyOf(mine.notes()));
    }

    @Test
    void ignoresUnsetMineResetLiteTeleportSentinel() throws IOException {
        var mine = read(mrl() + "  tpY: -2147483647\n", MigrationSource.MINERESETLITE);
        assertNull(mine.teleport());
    }

    @Test
    void keepsFixedSurfaceSeparateFromTheWeightedPool() throws IOException {
        var mine = read(mrl().replace("surface: ''", "surface: '2:0'"), MigrationSource.MINERESETLITE);
        assertEquals("2:0", mine.surface());
        assertFalse(mine.blocks().containsKey("2:0"));
    }

    @Test
    void doesNotPadCompositionsThatAlreadyContainAFullWeight() throws IOException {
        var mine = read(mrl().replace("'1:0': 0.5", "STONE: 0.75"), MigrationSource.MINERESETLITE);
        assertFalse(mine.blocks().containsKey("minecraft:air"));
        assertEquals(0.75, mine.blocks().get("STONE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", ".nan", ".inf", "oops"})
    void rejectsInvalidBlockWeights(String weight) throws IOException {
        String yaml = mrl().replace("'1:0': 0.5", "'1:0': " + weight);
        assertThrows(IllegalArgumentException.class, () -> read(yaml, MigrationSource.MINERESETLITE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "2147483647", "1.5", ".inf"})
    void rejectsInvalidOrOverflowingResetTimes(String time) throws IOException {
        String yaml = mrl().replace("resetDelay: 10", "resetDelay: " + time);
        assertThrows(IllegalArgumentException.class, () -> read(yaml, MigrationSource.MINERESETLITE));
    }

    @Test
    void rejectsIncompleteCornersAndInvalidIds() throws IOException {
        String missing = cata().replace("      x: -10.0\n", "");
        assertThrows(IllegalArgumentException.class, () -> read(missing, MigrationSource.CATAMINES));
        String invalidId = cata().replace("name: quarry_A", "name: invalid.name");
        assertThrows(IllegalArgumentException.class, () -> read(invalidId, MigrationSource.CATAMINES));
    }

    @Test
    void rejectsDuplicateKeysAndUnknownSchemas() {
        assertThrows(YAMLException.class, () -> read("mine: {}\nmine: {}", MigrationSource.MINERESETLITE));
        assertThrows(IllegalArgumentException.class, () -> read("regions: {}", MigrationSource.CATAMINES));
        assertThrows(YAMLException.class, () -> read("!!java.lang.Object {}", MigrationSource.CATAMINES));
    }

    private static AbstractMineMigrator.MineData read(String yaml, MigrationSource source) {
        return MigrationReader.read(new StringReader(yaml), source);
    }

    private static String cata() throws IOException {
        return fixture("catamines.yml");
    }

    private static String mrl() throws IOException {
        return fixture("mineresetlite.mine.yml");
    }

    private static String fixture(String name) throws IOException {
        try (var stream = MigrationReaderTest.class.getResourceAsStream("/migration/" + name)) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
