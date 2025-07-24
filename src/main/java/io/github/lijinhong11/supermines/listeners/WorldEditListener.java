package io.github.lijinhong11.supermines.listeners;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldEditListener {
    private final WorldEdit we = WorldEdit.getInstance();

    public WorldEditListener() {
        we.getEventBus().register(this);
    }

    @Subscribe
    public void onWorldEdit(EditSessionEvent e) {
        if (e.getActor() == null) {
            return;
        }

        CommandSender sender = BukkitAdapter.adapt(e.getActor());
        if (!(sender instanceof Player p)) {
            return;
        }

        if (e.getActor() == null) return;
        LocalSession sess = we.getSessionManager().get(BukkitAdapter.adapt(p));

        if (sess.getSelectionWorld() == null || !sess.isSelectionDefined(sess.getSelectionWorld())) {
            return;
        }

        World world = p.getWorld();

        try {
            Region sel = sess.getSelection(sess.getSelectionWorld());
            if (!(sel instanceof CuboidRegion cub)) {
                return;
            }

            BlockVector3 pos1 = cub.getMinimumPoint();
            BlockVector3 pos2 = cub.getMaximumPoint();

            SuperMinesCommand.handlePos(p, BukkitAdapter.adapt(world, pos1), BukkitAdapter.adapt(world, pos2));
        } catch (IncompleteRegionException ignored) {
        }
    }
}
