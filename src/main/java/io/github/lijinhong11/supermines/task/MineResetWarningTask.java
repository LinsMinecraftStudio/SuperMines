package io.github.lijinhong11.supermines.task;

import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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
    public void run(ScheduledTask ScheduledTask) {
        boolean broadcast = SuperMines.getInstance().getConfig().getBoolean("mine.broadcast-reset-messages", true);
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getScheduler()
                    .run(
                            SuperMines.getInstance(),
                            playerTask -> {
                                if (broadcast || mine.isPlayerInMine(p)) {
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(
                                                    p,
                                                    "mine.reset_warning",
                                                    mineName,
                                                    MessageReplacement.replace(
                                                            "%time%", StringUtils.formatCountdown(second)));
                                }
                            },
                            null);
        }
    }
}
