package io.github.lijinhong11.supermines.listeners;

import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.events.BlockBreakInMineEvent;
import io.github.lijinhong11.supermines.api.events.TreasureFoundEvent;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

    public static boolean isPlayerAutoPickupActive(Player player) {
        if (!SuperMines.getInstance().getConfig().getBoolean("mine.auto-pickup.enabled", false)) return false;
        String perm = SuperMines.getInstance().getConfig().getString("mine.auto-pickup.permission", "");
        if (!perm.isEmpty() && !player.hasPermission(perm)) return false;

        return getPlayerAutoPickup(player);
    }

    public static void togglePlayerAutoPickup(Player player) {
        PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
        data.setAutoPickup(!data.isAutoPickup());
    }

    public static boolean getPlayerAutoPickup(Player player) {
        PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
        return data.isAutoPickup();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void placeBlock(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (SuperMines.getInstance().getRegenPointManager().getRegenPoint(loc) != null) {
            if (p.isOp() || SuperMines.getInstance().getConfig().getBoolean("regen-point.allow-place", false)) {
                return;
            }

            e.setCancelled(true);
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "regen-point.no-place");
            return;
        }

        if (SuperMines.getInstance().getMineManager().getMine(loc) == null) {
            return;
        }

        if (p.isOp() || SuperMines.getInstance().getConfig().getBoolean("mine.allow-place", false)) {
            return;
        }

        e.setCancelled(true);
        SuperMines.getInstance().getLanguageManager().sendMessage(p, "mine.no-place");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Player player = e.getPlayer();

        Mine mine = SuperMines.getInstance().getMineManager().getMine(loc);

        if (mine != null && !mine.canMine(player)) {
            e.setCancelled(true);
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "mine.no-enough-rank");
            return;
        }

        var brokenBlock = ContentProviders.getBlockByLocation(loc);
        if (mine != null) {
            BlockBreakInMineEvent event = new BlockBreakInMineEvent(mine, player, brokenBlock);
            event.callEvent();
            if (event.isCancelled()) {
                e.setCancelled(true);
                return;
            }
        }

        if (!SuperMines.getInstance().getRegenPointManager().canBreakPoint(loc, player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void commitBlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Player player = e.getPlayer();
        Mine mine = SuperMines.getInstance().getMineManager().getMine(loc);

        SuperMines.getInstance().getRegenPointManager().commitPointBlockBroken(loc, player);

        if (mine == null) {
            return;
        }

        PlayerData playerData =
                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());

        playerData.addMinedBlocks(1);
        mine.plusBlocksBroken();

        boolean autoPickup = mine.isAutoPickup() || isPlayerAutoPickupActive(player);

        List<Treasure> treasures = mine.getTreasures();
        if (!treasures.isEmpty()) {
            var brokenBlock = ContentProviders.getBlockByLocation(loc);
            WeightedRandomMap<Treasure> weightedTreasures = new WeightedRandomMap<>();
            for (Treasure treasure : treasures) {
                if (treasure.getMatchedBlocks().contains(brokenBlock) && treasure.getWeight() > 0) {
                    weightedTreasures.put(treasure, treasure.getWeight());
                }
            }

            Treasure selected = weightedTreasures.randomOne();
            if (selected != null) {
                TreasureFoundEvent event = new TreasureFoundEvent(selected, player, mine);
                event.callEvent();
                if (!event.isCancelled()) {
                    selected.giveToPlayer(player, !autoPickup);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDropItems(BlockDropItemEvent e) {
        Location loc = e.getBlock().getLocation();
        Mine mine = SuperMines.getInstance().getMineManager().getMine(loc);
        if (mine == null) return;

        Player player = e.getPlayer();
        if (!mine.isAutoPickup() && !isPlayerAutoPickupActive(player)) return;

        List<Item> drops = e.getItems();
        for (Item item : drops) {
            ItemStack stack = item.getItemStack();
            player.getInventory().addItem(stack).values().forEach(leftover -> player.getWorld()
                    .dropItemNaturally(player.getLocation(), leftover));
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromBlockExplosions(BlockExplodeEvent e) {
        if (!SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.explosions", true)) return;

        e.blockList()
                .removeIf(block ->
                        SuperMines.getInstance().getRegenPointManager().getRegenPoint(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromEntityExplosions(EntityExplodeEvent e) {
        if (!SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.explosions", true)) return;

        e.blockList()
                .removeIf(block ->
                        SuperMines.getInstance().getRegenPointManager().getRegenPoint(block.getLocation()) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromPistonExtend(BlockPistonExtendEvent e) {
        if (!SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.pistons", true)) return;

        if (e.getBlocks().stream()
                        .anyMatch(block ->
                                SuperMines.getInstance().getRegenPointManager().getRegenPoint(block.getLocation())
                                        != null)
                || e.getBlocks().stream()
                        .anyMatch(block -> SuperMines.getInstance()
                                        .getRegenPointManager()
                                        .getRegenPoint(block.getRelative(e.getDirection())
                                                .getLocation())
                                != null)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromPistonRetract(BlockPistonRetractEvent e) {
        if (!SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.pistons", true)) return;

        if (e.getBlocks().stream()
                        .anyMatch(block ->
                                SuperMines.getInstance().getRegenPointManager().getRegenPoint(block.getLocation())
                                        != null)
                || e.getBlocks().stream()
                        .anyMatch(block -> SuperMines.getInstance()
                                        .getRegenPointManager()
                                        .getRegenPoint(block.getRelative(
                                                        e.getDirection().getOppositeFace())
                                                .getLocation())
                                != null)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromFluids(BlockFromToEvent e) {
        if (!SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.fluids", true)) return;

        if (SuperMines.getInstance()
                        .getRegenPointManager()
                        .getRegenPoint(e.getToBlock().getLocation())
                != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromPhysics(BlockPhysicsEvent e) {
        if (SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.environmental", true)
                && SuperMines.getInstance()
                                .getRegenPointManager()
                                .getRegenPoint(e.getBlock().getLocation())
                        != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromBurning(BlockBurnEvent e) {
        if (SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.environmental", true)
                && SuperMines.getInstance()
                                .getRegenPointManager()
                                .getRegenPoint(e.getBlock().getLocation())
                        != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromFading(BlockFadeEvent e) {
        if (SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.environmental", true)
                && SuperMines.getInstance()
                                .getRegenPointManager()
                                .getRegenPoint(e.getBlock().getLocation())
                        != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void protectRegenPointsFromEntityChanges(EntityChangeBlockEvent e) {
        if (SuperMines.getInstance().getConfig().getBoolean("regen-point.protection.environmental", true)
                && SuperMines.getInstance()
                                .getRegenPointManager()
                                .getRegenPoint(e.getBlock().getLocation())
                        != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void loadDeferredRegenPoints(WorldLoadEvent e) {
        SuperMines.getInstance().getRegenPointManager().loadDeferredPoints(e.getWorld());
    }
}
