package io.github.lijinhong11.supermines.listeners;

import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void placeBlock(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (SuperMines.getInstance().getMineManager().getMine(loc) == null) {
            return;
        }

        if (p.isOp() || SuperMines.getInstance().getConfig().getBoolean("mine.allow-place", false)) {
            return;
        }

        e.setCancelled(true);
        SuperMines.getInstance().getLanguageManager().sendMessage(p, "mine.no-place");
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Mine mine = SuperMines.getInstance().getMineManager().getMine(loc);
        Player player = e.getPlayer();

        if (mine == null) {
            return;
        }

        if (!mine.canMine(player)) {
            e.setCancelled(true);
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "mine.no-enough-rank");
            return;
        }

        PlayerData playerData =
                SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());

        playerData.addMinedBlocks(1);
        mine.plusBlocksBroken();

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
                selected.giveToPlayer(player, true);
            }
        }
    }
}
