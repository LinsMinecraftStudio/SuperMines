package io.github.lijinhong11.supermines.listeners;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.command.SuperMinesCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Copies a WorldEdit cuboid selection to the SuperMines position selection.
 *
 * <p>WorldEdit and FAWE both expose their edit events through the WorldEdit event bus.
 */
public final class WorldEditListener {
    private final WorldEdit we = WorldEdit.getInstance();

    public WorldEditListener() {
        we.getEventBus().register(this);
    }

    public void close() {
        we.getEventBus().unregister(this);
    }

    @Subscribe
    public void onWorldEdit(EditSessionEvent e) {
        if (e.getStage() != EditSession.Stage.BEFORE_HISTORY || e.getActor() == null || e.getWorld() == null) {
            return;
        }

        Actor actor = e.getActor();
        if (!actor.isPlayer()) {
            return;
        }

        Player player = Bukkit.getPlayer(actor.getUniqueId());
        if (player == null) {
            return;
        }

        LocalSession session = we.getSessionManager().get(actor);
        com.sk89q.worldedit.world.World selectionWorld = session.getSelectionWorld();

        if (selectionWorld == null || !session.isSelectionDefined(selectionWorld)) {
            return;
        }

        World world = Bukkit.getWorld(selectionWorld.getName());
        if (world == null) {
            return;
        }

        try {
            Region sel = session.getSelection(selectionWorld);
            if (!(sel instanceof CuboidRegion cub)) {
                return;
            }

            BlockVector3 pos1 = cub.getMinimumPoint();
            BlockVector3 pos2 = cub.getMaximumPoint();

            Location loc1 = BukkitAdapter.adapt(world, pos1);
            Location loc2 = BukkitAdapter.adapt(world, pos2);

            SuperMines plugin = SuperMines.getInstance();
            plugin.getTaskMaker().runSync(player, () -> SuperMinesCommand.setWorldEditSelection(player, loc1, loc2));
        } catch (IncompleteRegionException ignored) {
        }
    }
}
