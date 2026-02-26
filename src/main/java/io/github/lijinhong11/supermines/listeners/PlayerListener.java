package io.github.lijinhong11.supermines.listeners;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Mine mine = SuperMines.getInstance().getMineManager().getMine(loc);

        if (mine == null) {
            return;
        }

        Block block1 = loc.getBlock();
        Block block2 = loc.clone().add(0, 1, 0).getBlock();
        if (block1.getType().isSolid() || block2.getType().isSolid()) {
            if (mine.getTeleportLocation() == null) {
                p.teleportAsync(mine.getArea().getCenterLocation(block1.getWorld()));
            } else {
                p.teleportAsync(mine.getTeleportLocation());
            }
        }
    }
}
