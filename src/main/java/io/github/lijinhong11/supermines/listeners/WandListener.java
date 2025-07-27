package io.github.lijinhong11.supermines.listeners;

import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WandListener implements Listener {
    @EventHandler
    public void onWandUse(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || !isWand(item)) return;

        Player p = e.getPlayer();
        if (!p.hasPermission(Constants.Permission.POS_SET)) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        Location loc = block.getLocation();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) { // pos2
            SuperMinesCommand.handlePos(p, false, loc);
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) { // pos1
            SuperMinesCommand.handlePos(p, true, loc);
        }

        e.setCancelled(true);
    }

    private boolean isWand(ItemStack item) {
        if (item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(Constants.Keys.WAND_KEY);
    }
}
