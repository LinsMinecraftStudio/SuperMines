package io.github.lijinhong11.supermines.integrates;

import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MiniPlaceholderExtension {
    public static void register() {
        Expansion expansion = Expansion.builder("supermines")
                .audiencePlaceholder("rank", (a, args, ctx) -> {
                    if (args.hasNext()) {
                        String playerName = args.pop().value();
                        OfflinePlayer p2 = Bukkit.getOfflinePlayer(playerName);
                        PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p2.getUniqueId());
                        return Tag.selfClosingInserting(data.getRank().getDisplayName());
                    } else {
                        OfflinePlayer p = (Player) a;
                        PlayerData data = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
                        return Tag.selfClosingInserting(data.getRank().getDisplayName());
                    }
                })
                .build();

        expansion.register();
    }
}
