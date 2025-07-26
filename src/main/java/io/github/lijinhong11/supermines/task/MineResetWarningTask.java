package io.github.lijinhong11.supermines.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.message.LanguageManager;
import io.github.lijinhong11.supermines.message.MessageReplacement;
import io.github.lijinhong11.supermines.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class MineResetWarningTask extends AbstractTask {
    private static final LanguageManager lm = SuperMines.getInstance().getLanguageManager();

    private final Mine mine;
    private final int second;

    MineResetWarningTask(Mine mine, int second) {
        this.mine = mine;
        this.second = second;
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            lm.sendMessage(p, "mine.reset_warning", mineName, MessageReplacement.replace("%time%", NumberUtils.formatSeconds(p, second)));
        }

        lm.consoleMessage("mine.reset_warning", mineName, MessageReplacement.replace("%time%", NumberUtils.formatSeconds(second)));
    }
}
