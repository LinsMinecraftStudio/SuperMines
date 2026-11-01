package io.github.lijinhong11.supermines.migration;

import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.generation.BlockSpawnEntry;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.MineYGenerateCondition;
import io.github.lijinhong11.supermines.api.mine.generation.conditions.SurfaceGenerateCondition;
import io.github.lijinhong11.supermines.utils.MineBlocks;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.yaml.snakeyaml.error.YAMLException;

public final class MineMigration {
    private final SuperMines plugin;

    public MineMigration(SuperMines plugin) {
        this.plugin = plugin;
    }

    /**
     * Must run on the global server thread, like other mine management operations.
     */
    public void migrate(CommandSender sender, MigrationSource source) {
        File directory = new File(plugin.getDataFolder().getParentFile(), source.getPluginName() + "/mines");
        File[] files = directory.listFiles(file -> {
            String name = file.getName().toLowerCase(Locale.ROOT);
            return file.isFile() && (name.endsWith(".yml") || name.endsWith(".yaml"));
        });
        if (files == null || files.length == 0) {
            plugin.getLanguageManager()
                    .sendMessage(
                            sender,
                            "command.migrate.no-files",
                            MessageReplacement.replace("%path%", directory.getPath()));
            return;
        }
        AbstractMineMigrator migrator = source.getMigrator();
        Map<?, ?> sourceConfig;
        try {
            sourceConfig = migrator.readConfiguration(directory.toPath().getParent());
        } catch (IOException | IllegalArgumentException | YAMLException exception) {
            plugin.getLanguageManager()
                    .sendMessage(
                            sender,
                            "command.migrate.config-failed",
                            MessageReplacement.replace("%source%", source.getPluginName()));
            plugin.getLogger()
                    .log(Level.WARNING, "Could not read " + source.getPluginName() + "/config.yml", exception);
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName));
        int imported = 0;
        int skipped = 0;
        for (File file : files) {
            AbstractMineMigrator.MineData data;
            Mine mine;
            try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                data = migrator.read(reader, sourceConfig);
                if (plugin.getMineManager().hasMine(data.id())) {
                    skipped++;
                    plugin.getLanguageManager()
                            .sendMessage(
                                    sender, "command.migrate.exists", MessageReplacement.replace("%mine%", data.id()));
                    continue;
                }
                mine = toMine(data);
                if (!plugin.getMineManager().importMine(mine)) {
                    skipped++;
                    plugin.getLanguageManager()
                            .sendMessage(
                                    sender,
                                    "command.migrate.cancelled",
                                    MessageReplacement.replace("%mine%", data.id()));
                    continue;
                }
            } catch (IOException | IllegalArgumentException | YAMLException exception) {
                skipped++;
                plugin.getLanguageManager()
                        .sendMessage(
                                sender, "command.migrate.failed", MessageReplacement.replace("%file%", file.getName()));
                plugin.getLogger().log(Level.WARNING, "Could not migrate " + file.getPath(), exception);
                continue;
            }
            imported++;
            for (String note : data.notes()) {
                plugin.getLanguageManager()
                        .sendMessage(
                                sender,
                                "command.migrate.notes." + note,
                                MessageReplacement.replace("%mine%", mine.getId()));
            }
            try {
                plugin.getTaskMaker().startMineTasks(mine);
            } catch (RuntimeException exception) {
                plugin.getTaskMaker().cancelMineResetTask(mine);
                plugin.getLanguageManager()
                        .sendMessage(
                                sender,
                                "command.migrate.task-failed",
                                MessageReplacement.replace("%mine%", mine.getId()));
                plugin.getLogger()
                        .log(Level.WARNING, "Could not start tasks for migrated mine " + mine.getId(), exception);
            }
        }
        plugin.getLanguageManager()
                .sendMessage(
                        sender,
                        "command.migrate.complete",
                        MessageReplacement.replace("%source%", source.getPluginName()),
                        MessageReplacement.replace("%imported%", String.valueOf(imported)),
                        MessageReplacement.replace("%skipped%", String.valueOf(skipped)));
    }

    private Mine toMine(AbstractMineMigrator.MineData data) {
        World world = world(data.world());
        Location pos1 = location(data.pos1());
        Location pos2 = location(data.pos2());
        if (pos1.getWorld() != world || pos2.getWorld() != world) {
            throw new IllegalArgumentException("The region corners must be in the mine's world");
        }
        if (Math.min(pos1.getY(), pos2.getY()) < world.getMinHeight()
                || Math.max(pos1.getY(), pos2.getY()) >= world.getMaxHeight()) {
            throw new IllegalArgumentException("Mine is outside the world's height limits");
        }
        Map<String, BlockSpawnEntry> entries = new LinkedHashMap<>();
        WeightedRandomMap<BlockSpawnEntry> blocks = new WeightedRandomMap<>();
        for (Map.Entry<String, Double> entry : data.blocks().entrySet()) {
            PackedBlock block = resolveBlock(entry.getKey());
            BlockSpawnEntry spawn = entries.computeIfAbsent(block.getId(), id -> new BlockSpawnEntry(block));
            double weight = blocks.getDouble(spawn) + entry.getValue();
            if (!Double.isFinite(weight)) throw new IllegalArgumentException("Block weight is not finite");
            blocks.put(spawn, weight);
        }
        if (data.surface() != null) {
            PackedBlock surface = resolveBlock(data.surface());
            int belowSurface =
                    Math.max(pos1.getBlockY(), pos2.getBlockY()) - Math.min(pos1.getBlockY(), pos2.getBlockY()) - 1;
            for (BlockSpawnEntry entry : blocks.keySet()) {
                if (!entry.getId().equals(surface.getId())) {
                    entry.addGenerateCondition(new MineYGenerateCondition(0, belowSurface));
                }
            }
            if (!entries.containsKey(surface.getId())) {
                BlockSpawnEntry entry = new BlockSpawnEntry(surface);
                entry.addGenerateCondition(new SurfaceGenerateCondition(1));
                blocks.put(entry, 1);
            }
        }
        if (blocks.isEmpty()) throw new IllegalArgumentException("The mine has no blocks with a positive weight");
        Mine mine = new Mine(
                data.id(),
                ComponentUtils.text(data.id()),
                world,
                new CuboidArea(BlockPos.fromLocation(pos1), BlockPos.fromLocation(pos2)),
                blocks,
                data.resetSeconds(),
                data.onlyFillAir());
        mine.setWarningSeconds(data.warnings());
        if (data.messageBroadcastMode() != null) mine.setMessageBroadcastMode(data.messageBroadcastMode());
        if (data.teleport() != null) mine.setTeleportLocation(location(data.teleport()));
        return mine;
    }

    private PackedBlock resolveBlock(String id) {
        if (id.matches("\\d+(?::\\d+)?")) {
            if (id.equals("0") || id.equals("0:0")) return MineBlocks.resolve("minecraft:air");
            if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")
                    && !plugin.getServer().getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
                throw new IllegalArgumentException("WorldEdit or FAWE is required to convert legacy block " + id);
            }
            id = LegacyMineBlocks.resolve(id);
        }
        PackedBlock block = MineBlocks.resolve(id);
        if (block == null) throw new IllegalArgumentException("Unknown block: " + id);
        return block;
    }

    private static World world(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) throw new IllegalArgumentException("World is not loaded: " + name);
        return world;
    }

    private static Location location(AbstractMineMigrator.Position position) {
        if (Math.abs(position.x()) > 30_000_000
                || Math.abs(position.z()) > 30_000_000
                || position.y() < Integer.MIN_VALUE
                || position.y() > Integer.MAX_VALUE
                || !Float.isFinite(position.yaw())
                || !Float.isFinite(position.pitch())) {
            throw new IllegalArgumentException("Invalid location coordinates");
        }
        return new Location(
                world(position.world()), position.x(), position.y(), position.z(), position.yaw(), position.pitch());
    }
}
