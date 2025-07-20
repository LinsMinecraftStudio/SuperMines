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
    private final Mine mine;
    private final int second;

    MineResetWarningTask(Mine mine, int second) {
        this.mine = mine;
        this.second = second;
    }

    @Override
    public void run(WrappedTask wrappedTask) {
        LanguageManager languageManager = SuperMines.getInstance().getLanguageManager();
        MessageReplacement mineName = MessageReplacement.replace("%mine%", mine.getRawDisplayName());
        for (Player p : Bukkit.getOnlinePlayers()) {
            languageManager.sendMessage(p, "mine.reset_warning", mineName, MessageReplacement.replace("%time%", NumberUtils.formatSeconds(p, second)));
        }

        languageManager.consoleMessage("mine.reset_warning", mineName, MessageReplacement.replace("%time%", NumberUtils.formatSeconds(second)));
    }
}
