package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.message.SyncLanguageManager;
import io.github.lijinhong11.mittellib.utils.NumberUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class MineResetWarningTask extends AbstractTask {
    private final Mine mine;
    private final int second;

    MineResetWarningTask(Mine mine, int second) {
        this.mine = mine;
        this.second = second;
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        boolean broadcast = SuperMines.getInstance().getConfig().getBoolean("mine.broadcast-reset-messages", true);
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (broadcast || mine.isPlayerInMine(p)) {
                SuperMines.getInstance().getLanguageManager().sendMessage(
                        p,
                        "mine.reset_warning",
                        mineName,
                        MessageReplacement.replace("%time%", NumberUtils.formatSeconds(p, second)));
            }
        }
    }
}
