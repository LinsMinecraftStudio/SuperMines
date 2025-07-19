package io.github.lijinhong11.supermines.listeners;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void placeBlock(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Location loc = e.getBlock().getLocation();

        if (SuperMines.getInstance().getMineManager().getMine(loc) == null) {
            return;
        }

        if (p.isOp() || p.hasPermission(Constants.Permission.BYPASS_NO_PLACE)) {
            return;
        }

        e.setCancelled(true);
        SuperMines.getInstance().getLanguageManager().sendMessage(p, "mine.no-place-permission");
    }
}
