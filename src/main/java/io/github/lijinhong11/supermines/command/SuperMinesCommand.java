package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.utils.Constants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("supermines")
@Alias({"sm", "mine", "mines"})
public class SuperMinesCommand {
    @Default
    public void showHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help");
    }

    @Subcommand("create")
    @Permission(Constants.Permission.CREATE)
    public void createMine(Player player, @AStringArgument String id) {

    }

    @Subcommand("redefine")
    @Permission(Constants.Permission.REDEFINE)
    public void redefineMine(Player player, @AStringArgument String id) {

    }

    @Subcommand("remove")
    @Permission(Constants.Permission.REMOVE)
    public void removeMine(Player player, @AStringArgument String id) {

    }
}
