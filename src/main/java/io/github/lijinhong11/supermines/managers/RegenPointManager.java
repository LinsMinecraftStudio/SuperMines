package io.github.lijinhong11.supermines.managers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.events.RegenPointBreakEvent;
import io.github.lijinhong11.supermines.api.events.RegenPointRespawnEvent;
import io.github.lijinhong11.supermines.api.events.TreasureFoundEvent;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import io.github.lijinhong11.supermines.integrates.skills.SkillsBlockPlace;
import io.github.lijinhong11.supermines.managers.abstracts.AbstractFileObjectManager;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.Sounds;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages global regenerable ore points.
 *
 * <p>Regen points are fully independent of the mine reset system: each point
 * is a single block that respawns on a fixed delay after being broken. Lookups
 * are O(1) through a location-keyed map, and only the affected block position
 * is ever touched (no area-wide scanning or reset).
 */
public class RegenPointManager extends AbstractFileObjectManager<RegenPoint> {
    private static final int DEFAULT_RESPAWN_SECONDS = 0;

    private final Map<String, RegenPoint> points = new ConcurrentHashMap<>();
    private final Map<String, RegenPoint> byLocation = new ConcurrentHashMap<>();
    private final Map<String, ScheduledTask> respawnTasks = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> respawnGenerations = new ConcurrentHashMap<>();
    private final Set<String> deferredPointIds = ConcurrentHashMap.newKeySet();
    private ScheduledTask pendingSave;
    private volatile boolean closing;

    public RegenPointManager() {
        super("data/regen-points.yml");

        load();
    }

    public int getDefaultRespawnSeconds() {
        return DEFAULT_RESPAWN_SECONDS;
    }

    private void load() {
        for (RegenPoint object : super.getAll()) {
            if (object == null) {
                continue;
            }

            String locationKey = keyOf(object.getWorld().getName(), object.getPos());
            RegenPoint conflict = byLocation.get(locationKey);
            if (conflict != null) {
                SuperMines.getInstance()
                        .getLogger()
                        .warning("Skipping regen point '%s': location is already used by '%s'"
                                .formatted(object.getId(), conflict.getId()));
                continue;
            }

            points.put(object.getId(), object);
            byLocation.put(locationKey, object);
        }
    }

    /**
     * Restores persisted respawn tasks after the plugin scheduler is ready.
     */
    public void startup() {
        long now = System.currentTimeMillis();
        for (RegenPoint point : points.values()) {
            if (point.getRespawnSeconds() == 0) {
                if (point.getRespawnAt() > 0) {
                    point.setRespawnAt(0);
                    saveRegenPoint(point);
                }
                continue;
            }

            if (point.getRespawnAt() <= 0) {
                continue;
            }

            scheduleRespawn(point, Math.max(1L, point.getRespawnAt() - now));
        }
    }

    @Override
    protected RegenPoint getObject(@NotNull ConfigurationSection section) {
        String id = section.getCurrentPath();
        String worldName = section.getString("world");
        if (id == null || Strings.isNullOrEmpty(worldName)) {
            SuperMines.getInstance()
                    .getLogger()
                    .warning("Skipping malformed regen point section: " + section.getCurrentPath());
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            SuperMines.getInstance()
                    .getLogger()
                    .warning("Skipping regen point '%s': world '%s' is not loaded".formatted(id, worldName));
            deferredPointIds.add(id);
            return null;
        }

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");

        WeightedRandomMap<PackedBlock> blocks = new WeightedRandomMap<>();
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String blockId : blocksSection.getKeys(false)) {
                PackedBlock configured = ContentProviders.getBlock(blockId);
                double weight = blocksSection.getDouble(blockId);
                if (configured != null && weight > 0) {
                    blocks.put(configured, weight);
                }
            }
        }

        PackedBlock block =
                blocks.isEmpty() ? ContentProviders.getBlock(section.getString("block", "")) : blocks.randomOne();
        if (block == null) {
            SuperMines.getInstance()
                    .getLogger()
                    .warning("Skipping regen point '%s': no valid respawn blocks".formatted(id));
            return null;
        }

        int respawnSeconds = section.getInt("respawnSeconds", getDefaultRespawnSeconds());
        if (respawnSeconds < 0) {
            respawnSeconds = getDefaultRespawnSeconds();
        }

        String displayName = section.getString("displayName", id);
        Component name = ComponentUtils.deserialize(displayName);

        RegenPoint point = new RegenPoint(id, world, new BlockPos(x, y, z), block, respawnSeconds);
        if (!blocks.isEmpty()) {
            point.replaceBlocks(blocks);
        }
        point.setDisplayName(name);
        point.setRespawnAt(section.getLong("respawnAt", 0L));
        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String treasureId : rewardsSection.getKeys(false)) {
                double chance = rewardsSection.getDouble(treasureId);
                if (chance > 0 && chance <= 100) {
                    point.setRewardChance(treasureId, chance);
                }
            }
        }

        return point;
    }

    @Override
    protected void putObject(@NotNull ConfigurationSection section, RegenPoint object) {
        section.set("displayName", ComponentUtils.serialize(object.getDisplayName()));
        section.set("world", object.getWorld().getName());
        section.set("x", object.getPos().x());
        section.set("y", object.getPos().y());
        section.set("z", object.getPos().z());
        ConfigurationSection blocks = section.createSection("blocks");
        for (Map.Entry<PackedBlock, Double> entry : object.getBlocks().object2DoubleEntrySet()) {
            blocks.set(entry.getKey().getId(), entry.getValue());
        }
        section.set("respawnSeconds", object.getRespawnSeconds());
        if (!object.getRewardChances().isEmpty()) {
            ConfigurationSection rewards = section.createSection("rewards");
            object.getRewardChances().forEach(rewards::set);
        }
        if (object.getRespawnAt() > 0) {
            section.set("respawnAt", object.getRespawnAt());
        }
    }

    @Override
    public synchronized void saveAndClose() {
        closing = true;
        cancelAllRespawns();
        if (pendingSave != null) {
            pendingSave.cancel();
            pendingSave = null;
        }
        for (RegenPoint point : points.values()) {
            super.writeObject(point.getId(), point);
        }

        super.saveConfig();
    }

    public synchronized void addRegenPoint(@NotNull RegenPoint point) {
        Preconditions.checkNotNull(point, "point cannot be null");
        Preconditions.checkArgument(
                point.getId().matches(Constants.ID_PATTERN), "regen point ID contains invalid characters");

        if (points.containsKey(point.getId())) {
            throw new IllegalArgumentException("regen point with ID " + point.getId() + " already exists");
        }

        String key = keyOf(point.getWorld().getName(), point.getPos());
        if (byLocation.containsKey(key)) {
            throw new IllegalArgumentException("a regen point already exists at that location");
        }

        points.put(point.getId(), point);
        byLocation.put(key, point);
        super.putObject(point.getId(), point);
    }

    public synchronized void loadDeferredPoints(@NotNull World world) {
        for (String id : Set.copyOf(deferredPointIds)) {
            RegenPoint point = super.getObject(id);
            if (point == null || point.getWorld() != world) {
                continue;
            }

            String locationKey = keyOf(world.getName(), point.getPos());
            if (byLocation.containsKey(locationKey)) {
                SuperMines.getInstance()
                        .getLogger()
                        .warning("Skipping deferred regen point '%s': location is already in use".formatted(id));
                deferredPointIds.remove(id);
                continue;
            }

            points.put(id, point);
            byLocation.put(locationKey, point);
            deferredPointIds.remove(id);
            if (point.getRespawnSeconds() == 0 && point.getRespawnAt() > 0) {
                point.setRespawnAt(0);
                saveRegenPoint(point);
            } else if (point.getRespawnSeconds() > 0 && point.getRespawnAt() > 0) {
                scheduleRespawn(point, Math.max(1L, point.getRespawnAt() - System.currentTimeMillis()));
            }
        }
    }

    public synchronized void unloadWorld(@NotNull World world) {
        for (RegenPoint point : java.util.List.copyOf(points.values())) {
            if (point.getWorld() != world) continue;

            cancelRespawn(point);
            nextGeneration(point);
            points.remove(point.getId(), point);
            byLocation.remove(keyOf(world.getName(), point.getPos()), point);
            deferredPointIds.add(point.getId());
        }
    }

    public synchronized void removeRegenPoint(@NotNull String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "regen point ID cannot be null or empty");

        RegenPoint point = points.remove(id);
        if (point == null) {
            return;
        }

        cancelRespawn(point);
        respawnGenerations.remove(id);
        byLocation.remove(keyOf(point.getWorld().getName(), point.getPos()));
        super.remove(id);
    }

    /**
     * Persists a changed point without exposing the file manager internals.
     *
     * @param point the changed point
     */
    public synchronized void saveRegenPoint(@NotNull RegenPoint point) {
        Preconditions.checkArgument(points.get(point.getId()) == point, "point is not managed by this manager");

        super.writeObject(point.getId(), point);
        scheduleSave();
    }

    public synchronized void setRespawnSeconds(@NotNull RegenPoint point, int seconds) {
        Preconditions.checkArgument(points.get(point.getId()) == point, "point is not managed by this manager");

        point.setRespawnSeconds(seconds);
        if (seconds == 0) {
            cancelRespawn(point);
            point.setRespawnAt(0);
        } else if (point.getRespawnAt() > 0 || respawnTasks.containsKey(point.getId())) {
            point.setRespawnAt(System.currentTimeMillis() + seconds * 1000L);
            scheduleRespawn(point, seconds * 1000L);
        }
        saveRegenPoint(point);
    }

    public @Nullable RegenPoint getRegenPoint(@Nullable String id) {
        if (id == null) {
            return null;
        }

        return points.get(id);
    }

    public @Nullable RegenPoint getRegenPoint(@NotNull Location loc) {
        return getRegenPoint(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public @Nullable RegenPoint getRegenPoint(@NotNull String worldName, int x, int y, int z) {
        return byLocation.get(keyOf(worldName, x, y, z));
    }

    public Collection<RegenPoint> getAllRegenPoints() {
        return List.copyOf(points.values());
    }

    public Set<String> getAllRegenPointIds() {
        return Set.copyOf(points.keySet());
    }

    /**
     * Called when a block at a regen point location is broken. Schedules the
     * block to respawn after the configured delay, then flushes the block back
     * into place.
     *
     * @param loc the location of the broken block
     */
    public boolean canBreakPoint(@NotNull Location loc, @NotNull Player player) {
        RegenPoint point = getRegenPoint(loc);
        if (point == null) {
            return true;
        }

        if (respawnTasks.containsKey(point.getId())) {
            return true;
        }

        RegenPointBreakEvent event = new RegenPointBreakEvent(point, player);
        event.callEvent();
        return !event.isCancelled();
    }

    public void commitPointBlockBroken(@NotNull Location loc, @NotNull Player player) {
        RegenPoint point = getRegenPoint(loc);
        if (point == null || respawnTasks.containsKey(point.getId())) {
            return;
        }

        giveIndependentRewards(point, player);
        if (point.getRespawnSeconds() <= 0) {
            return;
        }

        point.setRespawnAt(System.currentTimeMillis() + Math.max(1, point.getRespawnSeconds()) * 1000L);
        saveRegenPoint(point);
        scheduleRespawn(point, Math.max(1L, point.getRespawnAt() - System.currentTimeMillis()));
    }

    /**
     * Regenerates the block of the given point immediately.
     *
     * @param point the regen point
     */
    public void respawn(@NotNull RegenPoint point) {
        respawn(point, RegenPointRespawnEvent.Cause.SCHEDULED, currentGeneration(point));
    }

    private void respawn(@NotNull RegenPoint point, @NotNull RegenPointRespawnEvent.Cause cause, long generation) {
        if (closing || points.get(point.getId()) != point || currentGeneration(point) != generation) return;
        Location loc = point.getLocation();
        if (loc.getWorld() == null) {
            return;
        }

        try {
            PackedBlock selected = point.selectBlock();
            if (selected == null) return;

            RegenPointRespawnEvent event = new RegenPointRespawnEvent(point, selected, cause);
            event.callEvent();
            if (event.isCancelled()) {
                if (cause == RegenPointRespawnEvent.Cause.SCHEDULED) {
                    long retrySeconds = Math.max(
                            1L,
                            SuperMines.getInstance()
                                    .getConfig()
                                    .getLong("regen-point.cancelled-respawn-retry-seconds", 5L));
                    point.setRespawnAt(System.currentTimeMillis() + retrySeconds * 1000L);
                    saveRegenPoint(point);
                    scheduleRespawn(point, retrySeconds * 1000L);
                }
                return;
            }

            cancelRespawn(point);

            if (!loc.getBlock().getType().isAir()) {
                ContentProviders.destroyBlock(loc);
            }

            event.getBlock().place(loc);
            SkillsBlockPlace.markAsEarnable(loc);

            point.setRespawnAt(0);
            saveRegenPoint(point);

            playRespawnEffect(loc);
        } catch (RuntimeException exception) {
            SuperMines.getInstance()
                    .getLogger()
                    .warning("Failed to respawn regen point '" + point.getId() + "': " + exception);
            if (!closing && points.get(point.getId()) == point && point.getRespawnSeconds() > 0) {
                long retryMillis = 5000L;
                point.setRespawnAt(System.currentTimeMillis() + retryMillis);
                saveRegenPoint(point);
                scheduleRespawn(point, retryMillis);
            }
        }
    }

    private void giveIndependentRewards(RegenPoint point, Player player) {
        for (Map.Entry<String, Double> entry : point.getRewardChances().entrySet()) {
            Treasure treasure = SuperMines.getInstance().getTreasureManager().getTreasure(entry.getKey());
            if (treasure != null && ThreadLocalRandom.current().nextDouble(100D) < entry.getValue()) {
                TreasureFoundEvent event = new TreasureFoundEvent(treasure, player, point);
                event.callEvent();
                if (!event.isCancelled()) {
                    treasure.giveToPlayer(player, false, point.getLocation());
                    PlayerData data =
                            SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                    data.addTreasureGot(treasure.getId());
                    SuperMines.getInstance().getPlayerDataManager().savePlayerData(data);
                }
            }
        }
    }

    /**
     * Immediately respawns a managed point and clears its pending task.
     *
     * @param point the point to respawn
     */
    public synchronized void respawnNow(@NotNull RegenPoint point) {
        if (closing || points.get(point.getId()) != point) return;
        cancelRespawn(point);
        long generation = nextGeneration(point);
        SuperMines.getInstance()
                .getTaskMaker()
                .runSync(point.getLocation(), () -> respawn(point, RegenPointRespawnEvent.Cause.MANUAL, generation));
    }

    public boolean isPending(@NotNull RegenPoint point) {
        return respawnTasks.containsKey(point.getId()) || point.getRespawnAt() > 0;
    }

    private synchronized void scheduleRespawn(RegenPoint point, long delayMillis) {
        if (closing || points.get(point.getId()) != point) return;
        cancelRespawn(point);
        long generation = nextGeneration(point);
        long delayTicks = Math.max(1L, (delayMillis + 49L) / 50L);
        ScheduledTask task = SuperMines.getInstance()
                .getTaskMaker()
                .runSyncDelayed(
                        point.getLocation(),
                        delayTicks,
                        () -> respawn(point, RegenPointRespawnEvent.Cause.SCHEDULED, generation));
        if (task != null) {
            respawnTasks.put(point.getId(), task);
        }
    }

    private void cancelRespawn(RegenPoint point) {
        ScheduledTask task = respawnTasks.remove(point.getId());
        if (task != null) {
            task.cancel();
        }
    }

    private void cancelAllRespawns() {
        for (ScheduledTask task : respawnTasks.values()) {
            task.cancel();
        }
        respawnTasks.clear();
        respawnGenerations.values().forEach(AtomicLong::incrementAndGet);
    }

    private synchronized void scheduleSave() {
        if (closing) return;
        if (pendingSave != null) {
            return;
        }

        pendingSave = Bukkit.getGlobalRegionScheduler()
                .runDelayed(
                        SuperMines.getInstance(),
                        task -> {
                            synchronized (this) {
                                pendingSave = null;
                                super.saveConfig();
                            }
                        },
                        20L);
    }

    private long currentGeneration(RegenPoint point) {
        return respawnGenerations
                .computeIfAbsent(point.getId(), id -> new AtomicLong())
                .get();
    }

    private long nextGeneration(RegenPoint point) {
        return respawnGenerations
                .computeIfAbsent(point.getId(), id -> new AtomicLong())
                .incrementAndGet();
    }

    private void playRespawnEffect(Location loc) {
        ConfigurationSection section = SuperMines.getInstance().getConfig().getConfigurationSection("regen-point");
        if (section == null) {
            return;
        }

        if (section.getBoolean("particles", true)) {
            try {
                Particle particle = Particle.valueOf(section.getString("particle", "HAPPY_VILLAGER"));
                loc.getWorld()
                        .spawnParticle(
                                particle,
                                loc.getX() + 0.5,
                                loc.getY() + 0.5,
                                loc.getZ() + 0.5,
                                12,
                                0.3,
                                0.3,
                                0.3,
                                0.01);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (section.getBoolean("sound", true)) {
            Sound sound = Sounds.getSound(section.getString("sound-name", ""));
            if (sound == null) {
                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }

            loc.getWorld().playSound(loc, sound, 0.5f, 1.2f);
        }
    }

    private static String keyOf(String worldName, BlockPos pos) {
        return keyOf(worldName, pos.x(), pos.y(), pos.z());
    }

    private static String keyOf(String worldName, int x, int y, int z) {
        return worldName + ':' + x + ':' + y + ':' + z;
    }
}
