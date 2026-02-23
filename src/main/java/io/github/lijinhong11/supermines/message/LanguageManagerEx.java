package io.github.lijinhong11.supermines.message;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.mittellib.message.LanguageManager;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LanguageManagerEx extends LanguageManager {
    public LanguageManagerEx(Plugin plugin) {
        super(plugin);
    }

    public List<Component> getMineInfo(@NotNull Player p, @NotNull Mine mine) {
        Preconditions.checkNotNull(mine, "mine cannot be null");
        MessageReplacement world =
                MessageReplacement.replace("%world%", mine.getWorld().getName());
        MessageReplacement regenerateSeconds =
                MessageReplacement.replace("%regenerate_seconds%", String.valueOf(mine.getRegenerateSeconds()));
        MessageReplacement pos1 =
                MessageReplacement.replace("%pos1%", mine.getArea().pos1().toString());
        MessageReplacement pos2 =
                MessageReplacement.replace("%pos2%", mine.getArea().pos2().toString());
        return getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, pos1, pos2);
    }

    public List<Component> getTreasureInfo(@NotNull Player p, @NotNull Treasure treasure) {
        MessageReplacement chance = MessageReplacement.replace("%chance%", String.valueOf(treasure.getChance()));
        MessageReplacement matchedMaterials = MessageReplacement.replace(
                "%matched_materials%",
                String.valueOf(treasure.getMatchedBlocks().size()));
        return getMsgComponentList(p, "gui.treasures.info", chance, matchedMaterials);
    }

    public List<Component> getRankInfo(@NotNull Player p, @NotNull Rank rank) {
        MessageReplacement level = MessageReplacement.replace("%level%", String.valueOf(rank.getLevel()));
        return getMsgComponentList(p, "gui.ranks.info", level);
    }
}
