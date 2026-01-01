package io.github.lijinhong11.supermines.utils.chat;

import org.bukkit.entity.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

public interface ChatInputHandler extends Predicate<String> {
    @ParametersAreNonnullByDefault
    void onChat(Player p, String msg);
}
