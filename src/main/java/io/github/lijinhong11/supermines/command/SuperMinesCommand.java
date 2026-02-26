package io.github.lijinhong11.supermines.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.CuboidArea;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.NumberUtils;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.gui.GuiManager;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.NullUtils;
import io.github.lijinhong11.supermines.utils.selection.AreaSelection;
import io.github.lijinhong11.supermines.utils.selection.SelectionValidator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SuperMinesCommand {
    private static final Map<UUID, AreaSelection> selectionMap = new ConcurrentHashMap<>();

    public static void handlePos(Player player, boolean isPos1, Location forcedLoc) {
        Location loc = forcedLoc != null ? forcedLoc : player.getLocation();

        var mineManager = SuperMines.getInstance().getMineManager();
        if (mineManager.getMine(loc) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos.in-mine");
            return;
        }

        UUID uuid = player.getUniqueId();
        AreaSelection sel = selectionMap.get(uuid);

        if (!SelectionValidator.validateAll(player, sel, loc)) {
            return;
        }

        if (sel == null) {
            sel = new AreaSelection(null, null);
        }

        sel = isPos1 ? new AreaSelection(loc, sel.pos2()) : new AreaSelection(sel.pos1(), loc);

        selectionMap.put(uuid, sel);

        String parsed = SuperMines.getInstance().getLanguageManager().getParsedBlockLocation(player, loc);
        MessageReplacement pos = MessageReplacement.replace("%pos%", parsed);

        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(player, isPos1 ? "command.pos.set.pos1" : "command.pos.set.pos2", pos);
    }

    public void register() {
        new CommandAPICommand("supermines")
                .withAliases("sm", "mine", "mines")
                .executes((CommandExecutor) (sender, args) -> showHelp(sender))
                .withSubcommand(new CommandAPICommand("treasures")
                        .withPermission(Constants.Permission.TREASURES)
                        .executes((sender, args) -> {
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessages(sender, "command.help.treasures");
                        })
                        .withSubcommands(
                                new CommandAPICommand("create")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(new StringArgument("id"), new DoubleArgument("chance", 1, 100))
                                        .withOptionalArguments(new DisplayNameArgument())
                                        .executesPlayer((player, args) -> {
                                            String id = args.getByClassOrDefault("id", String.class, "");
                                            Optional<Component> displayName =
                                                    args.getOptionalByClass("displayName", Component.class);
                                            double chance = args.getByClassOrDefault("chance", double.class, 0d);
                                            if (!id.matches(Constants.Texts.ID_PATTERN)) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.invalid-id");
                                                return;
                                            }

                                            Treasure tr = SuperMines.getInstance().getTreasureManager().getTreasure(id);
                                            if (tr != null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.treasures.create.exists");
                                                return;
                                            }

                                            ItemStack is = player.getInventory().getItemInMainHand();
                                            if (is.getType().isAir()) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.treasures.no-item-in-hand");
                                                return;
                                            }

                                            Treasure treasure = new Treasure(
                                                    id, displayName.orElse(Component.text(id)), is, chance);
                                            SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .addTreasure(treasure);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            player,
                                                            "command.treasures.create.success",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("list")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .executes((sender, args) -> {
                                            list(
                                                    sender,
                                                    SuperMines.getInstance()
                                                            .getTreasureManager()
                                                            .getAllTreasures()
                                                            .toArray(new Treasure[0]));
                                        }),
                                new CommandAPICommand("remove")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(new StringArgument("id"))
                                        .executesPlayer((player, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.treasures-not-exists");
                                                return;
                                            }

                                            SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .removeTreasure(treasure.getId());
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            player,
                                                            "command.treasures.remove.success",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("setDisplayName")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new DisplayNameArgument())
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            Component displayName = (Component) args.get("displayName");
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }

                                            treasure.setDisplayName(displayName);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.set-display-name",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("setChance")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new DoubleArgument("chance", 1, 100))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            double chance = args.getByClassOrDefault("chance", double.class, 0d);
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }

                                            treasure.setChance(chance);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.set-chance",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("setItem")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(new StringArgument("id")
                                                .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                                        .executesPlayer((player, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.treasure-not-exists");
                                                return;
                                            }

                                            ItemStack itemStack =
                                                    player.getInventory().getItemInMainHand();
                                            if (itemStack.getType().isAir()) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.treasures.item-not-found");
                                                return;
                                            }

                                            treasure.setItemStack(itemStack);

                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            player,
                                                            "command.treasures.set-item",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("addMatch")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new BlockArgument("block"))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            PackedBlock block = (PackedBlock) args.get("block");

                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }

                                            if (block == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.invalid-block");
                                                return;
                                            }

                                            if (treasure.getMatchedBlocks().contains(block)) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.treasures.matched_blocks.exists");
                                                return;
                                            }

                                            treasure.addMatchedBlock(block);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.matched_blocks.add_success",
                                                            MessageReplacement.replace("%block%", block.getId()),
                                                            MessageReplacement.replace("%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("removeMatch")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new BlockArgument("block"))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            PackedBlock block = (PackedBlock) args.get("block");

                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }

                                            if (block == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.invalid-block");
                                                return;
                                            }

                                            if (!treasure.getMatchedBlocks().contains(block)) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                sender, "command.treasures.matched_blocks.not_exists");
                                                return;
                                            }

                                            treasure.removeMatchedBlock(block);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.matched_blocks.remove_success",
                                                            MessageReplacement.replace("%block%", block.getId()),
                                                            MessageReplacement.replace("%treasure%", treasure.getId()));
                                        }),
                                new CommandAPICommand("addCommand")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new GreedyStringArgument("command"))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            String command = NullUtils.tryString((String) args.get("command"));
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }
                                            if (command.isEmpty()) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.treasures.command-empty");
                                                return;
                                            }
                                            treasure.addConsoleCommand(command);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.add-command.success",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("removeCommand")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(
                                                                ArgumentSuggestions.strings(getTreasuresList())),
                                                new GreedyStringArgument("command"))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            String command = NullUtils.tryString((String) args.get("command"));
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }
                                            List<String> commands = treasure.getConsoleCommands();
                                            if (commands == null || !commands.remove(command)) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                sender, "command.treasures.remove-command.not-found");
                                                return;
                                            }
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.remove-command.success",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("listCommands")
                                        .withPermission(Constants.Permission.TREASURES)
                                        .withArguments(new StringArgument("id")
                                                .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            Treasure treasure = SuperMines.getInstance()
                                                    .getTreasureManager()
                                                    .getTreasure(id);
                                            if (treasure == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(sender, "command.treasure-not-exists");
                                                return;
                                            }
                                            List<String> commands = treasure.getConsoleCommands();
                                            if (commands == null || commands.isEmpty()) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                sender,
                                                                "command.treasures.list-commands.empty",
                                                                MessageReplacement.replace(
                                                                        "%treasure%", treasure.getRawDisplayName()));
                                                return;
                                            }
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.treasures.list-commands.head",
                                                            MessageReplacement.replace(
                                                                    "%treasure%", treasure.getRawDisplayName()));
                                            for (int i = 0; i < commands.size(); i++) {
                                                int index = i + 1;
                                                String cmd = commands.get(i);
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                sender,
                                                                "command.treasures.list-commands.line",
                                                                MessageReplacement.replace(
                                                                        "%index%", String.valueOf(index)),
                                                                MessageReplacement.replace("%command%", cmd));
                                            }
                                        })))

                // Rank
                .withSubcommand(new CommandAPICommand("ranks")
                        .withPermission(Constants.Permission.RANKS)
                        .executes((sender, args) -> {
                            SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.ranks");
                        })
                        .withSubcommands(
                                new CommandAPICommand("list")
                                        .withPermission(Constants.Permission.RANKS)
                                        .executes((sender, args) -> {
                                            list(
                                                    sender,
                                                    SuperMines.getInstance()
                                                            .getRankManager()
                                                            .getAllRanks()
                                                            .toArray(new Rank[0]));
                                        }),
                                new CommandAPICommand("create")
                                        .withPermission(Constants.Permission.RANKS)
                                        .withArguments(
                                                new StringArgument("rankId"),
                                                new DisplayNameArgument(),
                                                new IntegerArgument("level", 1, Integer.MAX_VALUE))
                                        .executesPlayer((player, args) -> {
                                            String id = NullUtils.tryString((String) args.get("rankId"));
                                            Component displayName = args.getByClassOrDefault(
                                                    "displayName", Component.class, Component.text(id));
                                            int level = args.getByClassOrDefault("level", int.class, 0);

                                            if (!id.matches(Constants.Texts.ID_PATTERN)) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.invalid-id");
                                                return;
                                            }

                                            if (SuperMines.getInstance()
                                                            .getRankManager()
                                                            .getRank(id)
                                                    != null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.ranks.create.exists");
                                                return;
                                            }

                                            Rank rank = new Rank(level, id, displayName);
                                            SuperMines.getInstance()
                                                    .getRankManager()
                                                    .addRank(rank);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessages(
                                                            player,
                                                            "command.ranks.create.success",
                                                            MessageReplacement.replace("%rank%", rank.getId()));
                                        }),
                                new CommandAPICommand("remove")
                                        .withPermission(Constants.Permission.RANKS)
                                        .withArguments(new StringArgument("rankId")
                                                .includeSuggestions(ArgumentSuggestions.strings(getRankList())))
                                        .executesPlayer((player, args) -> {
                                            String id = NullUtils.tryString((String) args.get("rankId"));
                                            if (SuperMines.getInstance()
                                                            .getRankManager()
                                                            .getRank(id)
                                                    == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessages(player, "command.rank-not-exists");
                                                return;
                                            }

                                            SuperMines.getInstance()
                                                    .getRankManager()
                                                    .removeRank(id);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessages(
                                                            player,
                                                            "command.ranks.remove.success",
                                                            MessageReplacement.replace("%rank%", id));
                                        }),
                                new CommandAPICommand("setLevel")
                                        .withPermission(Constants.Permission.RANKS)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())),
                                                new IntegerArgument("level", 1, Integer.MAX_VALUE))
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            int level = args.getByClassOrDefault("level", int.class, 0);
                                            Rank rank = SuperMines.getInstance()
                                                    .getRankManager()
                                                    .getRank(id);
                                            if (rank == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.rank-not-exists");
                                                return;
                                            }

                                            if (level < 1) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.ranks.level-less-than-1");
                                                return;
                                            }

                                            rank.setLevel(level);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.ranks.set-level",
                                                            MessageReplacement.replace(
                                                                    "%rank%", rank.getRawDisplayName()),
                                                            MessageReplacement.replace(
                                                                    "%level%", String.valueOf(level)));
                                        }),
                                new CommandAPICommand("setDisplayName")
                                        .withPermission(Constants.Permission.RANKS)
                                        .withArguments(
                                                new StringArgument("id")
                                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())),
                                                new DisplayNameArgument())
                                        .executes((sender, args) -> {
                                            String id = NullUtils.tryString((String) args.get("id"));
                                            Component displayName = (Component) args.get("displayName");

                                            Rank rank = SuperMines.getInstance()
                                                    .getRankManager()
                                                    .getRank(id);
                                            if (rank == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.rank-not-exists");
                                                return;
                                            }

                                            rank.setDisplayName(displayName);
                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.ranks.set-display-name",
                                                            MessageReplacement.replace("%rank%", rank.getId()),
                                                            MessageReplacement.replace(
                                                                    "%displayName%", rank.getRawDisplayName()));
                                        }),
                                new CommandAPICommand("giveRank")
                                        .withPermission(Constants.Permission.RANKS)
                                        .withArguments(
                                                new EntitySelectorArgument.OnePlayer("player"),
                                                new StringArgument("rankId")
                                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())))
                                        .withOptionalArguments(new BooleanArgument("notify"))
                                        .executes((sender, args) -> {
                                            Player player = (Player) args.get("player");
                                            boolean notify = (boolean) args.getOrDefault("notify", false);
                                            String id = (String) args.get("rankId");

                                            Rank rank = SuperMines.getInstance()
                                                    .getRankManager()
                                                    .getRank(id);
                                            if (rank == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.rank-not-exists");
                                                return;
                                            }

                                            PlayerData data = SuperMines.getInstance()
                                                    .getPlayerDataManager()
                                                    .getOrCreatePlayerData(player.getUniqueId());
                                            data.addRank(rank);

                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.ranks.give-rank",
                                                            MessageReplacement.replace("%player%", player.getName()),
                                                            MessageReplacement.replace(
                                                                    "%rank%", rank.getRawDisplayName()));

                                            if (notify) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                player,
                                                                "command.ranks.give-rank-notify",
                                                                MessageReplacement.replace(
                                                                        "%rank%", rank.getRawDisplayName()));
                                            }
                                        }),
                                new CommandAPICommand("takeRank")
                                        .withArguments(
                                                new EntitySelectorArgument.OnePlayer("player"),
                                                new StringArgument("rankId")
                                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())))
                                        .withOptionalArguments(new BooleanArgument("notify"))
                                        .executes((sender, args) -> {
                                            Player player = (Player) args.get("player");
                                            boolean notify = (boolean) args.getOrDefault("notify", false);
                                            String id = (String) args.get("rankId");

                                            Rank rank = SuperMines.getInstance()
                                                    .getRankManager()
                                                    .getRank(id);
                                            if (rank == null) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(sender, "command.rank-not-exists");
                                                return;
                                            }

                                            PlayerData data = SuperMines.getInstance()
                                                    .getPlayerDataManager()
                                                    .getOrCreatePlayerData(player.getUniqueId());
                                            data.removeRank(rank);

                                            SuperMines.getInstance()
                                                    .getLanguageManager()
                                                    .sendMessage(
                                                            sender,
                                                            "command.ranks.take-rank",
                                                            MessageReplacement.replace("%player%", player.getName()),
                                                            MessageReplacement.replace(
                                                                    "%rank%", rank.getRawDisplayName()));

                                            if (notify) {
                                                SuperMines.getInstance()
                                                        .getLanguageManager()
                                                        .sendMessage(
                                                                player,
                                                                "command.ranks.take-rank-notify",
                                                                MessageReplacement.replace(
                                                                        "%rank%", rank.getRawDisplayName()));
                                            }
                                        })))
                // Other
                .withSubcommand(new CommandAPICommand("pos1")
                        .withPermission(Constants.Permission.POS_SET)
                        .executesPlayer((PlayerCommandExecutor) (player, args) -> handlePos(player, true, null))
                        .withOptionalArguments(new LocationArgument("loc"))
                        .executesPlayer((player, args) -> {
                            Location loc = (Location) args.get("loc");
                            handlePos(player, true, loc);
                        }))
                .withSubcommand(new CommandAPICommand("pos2")
                        .withPermission(Constants.Permission.POS_SET)
                        .executesPlayer((PlayerCommandExecutor) (player, args) -> handlePos(player, false, null))
                        .withOptionalArguments(new LocationArgument("loc"))
                        .executesPlayer((player, args) -> {
                            Location loc = (Location) args.get("loc");
                            handlePos(player, false, loc);
                        }))

                // Mines
                .withSubcommand(new CommandAPICommand("create")
                        .withPermission(Constants.Permission.CREATE)
                        .withArguments(new StringArgument("id"))
                        .withOptionalArguments(new DisplayNameArgument())
                        .executesPlayer((player, args) -> {
                            String id = NullUtils.tryString((String) args.get("id"));
                            Component displayName = args.getOptionalByClass("displayName", Component.class)
                                    .orElse(Component.text(id));
                            createMine(player, id, displayName);
                        }))
                .withSubcommand(new CommandAPICommand("redefine")
                        .withPermission(Constants.Permission.REDEFINE)
                        .withArguments(
                                new StringArgument("id").includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                        .executesPlayer((PlayerCommandExecutor)
                                (player, args) -> redefineMine(player, (String) args.get("id"))))
                .withSubcommand(new CommandAPICommand("remove")
                        .withPermission(Constants.Permission.REMOVE)
                        .withArguments(new StringArgument("mineId")
                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                        .executes((CommandExecutor) (sender, args) -> removeMine(sender, (String) args.get("mineId"))))
                .withSubcommand(new CommandAPICommand("reset")
                        .withPermission(Constants.Permission.RESET)
                        .withArguments(
                                new StringArgument("id").includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                        .executes((CommandExecutor) (sender, args) -> resetMine(sender, (String) args.get("id"))))
                .withSubcommand(new CommandAPICommand("list")
                        .withPermission(Constants.Permission.LIST)
                        .executes((sender, args) -> {
                            list(
                                    sender,
                                    SuperMines.getInstance()
                                            .getMineManager()
                                            .getAllMines()
                                            .toArray(new Mine[0]));
                        }))
                .withSubcommand(new CommandAPICommand("gui")
                        .withPermission(Constants.Permission.GUI)
                        .executesPlayer((player, args) -> {
                            GuiManager.openGeneral(player);
                        }))
                .withSubcommand(new CommandAPICommand("addTreasure")
                        .withPermission(Constants.Permission.TREASURES)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new StringArgument("treasureId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                        .executes((sender, args) -> {
                            String mineId = args.getByClassOrDefault("mineId", String.class, "");
                            String treasureId = (String) args.get("treasureId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            Treasure treasure = SuperMines.getInstance()
                                    .getTreasureManager()
                                    .getTreasure(treasureId);
                            if (treasure == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.treasure-not-exists");
                                return;
                            }

                            mine.addTreasure(treasure);
                        }))
                .withSubcommand(new CommandAPICommand("removeTreasure")
                        .withPermission(Constants.Permission.TREASURES)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new StringArgument("treasureId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getTreasuresList())))
                        .executes((sender, args) -> {
                            String mineId = args.getByClassOrDefault("mineId", String.class, "");
                            String treasureId = args.getByClassOrDefault("treasureId", String.class, "");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            Treasure treasure = SuperMines.getInstance()
                                    .getTreasureManager()
                                    .getTreasure(treasureId);
                            if (treasure == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.treasure-not-exists");
                                return;
                            }

                            mine.removeTreasure(treasure);
                        }))
                .withSubcommand(new CommandAPICommand("setRequiredLevel")
                        .withPermission(Constants.Permission.SET_REQUIRED_LEVEL)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new IntegerArgument("level", 1, Integer.MAX_VALUE))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            int level = (int) args.get("level");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            mine.setRequiredRankLevel(level);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.set-required-level",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%level%", String.valueOf(level)));
                        }))
                .withSubcommand(new CommandAPICommand("setBlockGenerate")
                        .withPermission(Constants.Permission.BLOCK_GENERATE)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new DoubleArgument("chance", 1, 100),
                                new BlockArgument("block")
                        )
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            double chance = (double) args.get("chance");
                            PackedBlock block = (PackedBlock) args.get("block");

                            if (block == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.invalid-block");
                                return;
                            }

                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            double rest = mine.calculateRestChance();
                            double wasSet = mine.getBlockSpawnEntries().getOrDefault(block, 0d);
                            if (wasSet > 0) {
                                if (chance > wasSet && (chance - wasSet > rest)) {
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(sender, "command.block-generate.chance-too-high");
                                    return;
                                }
                            } else {
                                if (chance > rest) {
                                    SuperMines.getInstance()
                                            .getLanguageManager()
                                            .sendMessage(sender, "command.block-generate.chance-too-high");
                                    return;
                                }
                            }

                            mine.addBlockSpawnEntry(block, chance);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.block-generate.success",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%block%", block.getId()),
                                            MessageReplacement.replace("%chance%", String.valueOf(chance)));
                        }))
                .withSubcommand(new CommandAPICommand("removeBlockGenerate")
                        .withPermission(Constants.Permission.BLOCK_GENERATE)
                        .withArguments(new StringArgument("mineId"), new BlockArgument("block"))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            PackedBlock block = (PackedBlock) args.get("block");

                            if (block == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.invalid-block");
                                return;
                            }

                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            mine.removeBlockSpawnEntry(block);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.block-generate.removed",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
                        }))
                .withSubcommand(new CommandAPICommand("setDisplayName")
                        .withPermission(Constants.Permission.SET_DISPLAY_NAME)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new DisplayNameArgument())
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            mine.setDisplayName(
                                    Objects.requireNonNull(args.getByClass("displayName", Component.class)));
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.set-display-name",
                                            MessageReplacement.replace("%mine%", mine.getId()),
                                            MessageReplacement.replace("%displayName%", mine.getRawDisplayName()));
                        }))
                .withSubcommand(new CommandAPICommand("setDisplayIcon")
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new EnumArgument<>("icon", Material.class))
                        .withPermission(Constants.Permission.SET_DISPLAY_ICON)
                        .executes((executor, args) -> {
                            String mineId = (String) args.get("mineId");
                            Material icon = (Material) args.get("icon");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(executor, "command.mine-not-exists");
                                return;
                            }

                            if (icon == null || icon.isAir()) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(executor, "command.invalid-material");
                                return;
                            }

                            mine.setDisplayIcon(icon);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            executor,
                                            "command.set-display-icon",
                                            MessageReplacement.replace("%mine%", mine.getId()),
                                            MessageReplacement.replace("%icon%", icon.toString()));
                        }))
                .withSubcommand(new CommandAPICommand("addAllowedRank")
                        .withPermission(Constants.Permission.RANKS)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new StringArgument("rankId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            Rank rank =
                                    SuperMines.getInstance().getRankManager().getRank((String) args.get("rankId"));
                            if (rank == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.rank-not-exists");
                                return;
                            }

                            mine.addAllowedRankId(rank.getId());
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.ranks.allowed",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%rank%", rank.getRawDisplayName()));
                        }))
                .withSubcommand(new CommandAPICommand("removeAllowedRank")
                        .withPermission(Constants.Permission.RANKS)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new StringArgument("rankId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getRankList())))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            Rank rank =
                                    SuperMines.getInstance().getRankManager().getRank((String) args.get("rankId"));
                            if (rank == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.rank-not-exists");
                                return;
                            }

                            mine.removeAllowedRankId(rank.getId());
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.ranks.disallowed",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace("%rank%", rank.getRawDisplayName()));
                        }))
                .withSubcommand(new CommandAPICommand("addResetWarning")
                        .withPermission(Constants.Permission.RESET_WARNINGS)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new IntegerArgument("restSeconds", 1, Integer.MAX_VALUE))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            int restSeconds = (int) args.get("restSeconds");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            if (restSeconds >= mine.getRegenerateSeconds()) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.resetwarning.time-too-long");
                                return;
                            }

                            mine.getWarningSeconds().add(restSeconds);
                            SuperMines.getInstance().getTaskMaker().startMineWarningTask(mine, restSeconds);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            "command.resetwarning.started",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                            MessageReplacement.replace(
                                                    "%seconds%", NumberUtils.formatSeconds(sender, restSeconds)));
                        }))
                .withSubcommand(new CommandAPICommand("removeResetWarning")
                        .withPermission(Constants.Permission.RESET_WARNINGS)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new IntegerArgument("restSeconds", 1, Integer.MAX_VALUE))
                        .executes((sender, args) -> {
                            String mineId = (String) args.get("mineId");
                            int restSeconds = (int) args.get("restSeconds");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(mineId);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            if (restSeconds >= mine.getRegenerateSeconds()) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.resetwarning.time-too-long");
                                return;
                            }

                            mine.getWarningSeconds().remove(restSeconds);
                            SuperMines.getInstance().getTaskMaker().cancelMineWarningTask(mine, restSeconds);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(sender, "command.resetwarning.removed");
                        }))
                .withSubcommand(new CommandAPICommand("setResetTime")
                        .withPermission(Constants.Permission.SET_RESET_TIME)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new IntegerArgument("resetTime", 1, Integer.MAX_VALUE))
                        .executes((sender, args) -> {
                            String id = (String) args.get("mineId");
                            int resetTime = args.getByClassOrDefault("resetTime", int.class, 0);
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(id);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            mine.setRegenerateSeconds(resetTime);
                            if (resetTime <= 0) {
                                SuperMines.getInstance().getTaskMaker().cancelMineResetTask(mine);
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(
                                                sender,
                                                "command.reset.stop-reset",
                                                MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
                            } else {
                                SuperMines.getInstance().getTaskMaker().restartMineResetTask(mine);
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(
                                                sender,
                                                "command.reset.time-set",
                                                MessageReplacement.replace("%mine%", mine.getRawDisplayName()),
                                                MessageReplacement.replace(
                                                        "%time%", NumberUtils.formatSeconds(sender, resetTime)));
                            }
                        }))
                .withSubcommand(new CommandAPICommand("settp")
                        .withAliases("setteleport")
                        .withPermission(Constants.Permission.SET_TELEPORT)
                        .withArguments(new StringArgument("mineId")
                                .includeSuggestions(ArgumentSuggestions.strings(getMineList())))
                        .executesPlayer((p, args) -> {
                            String id = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(id);
                            if (mine == null) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.mine-not-exists");
                                return;
                            }

                            Location loc = p.getLocation();
                            mine.setTeleportLocation(loc);
                            String parsed = SuperMines.getInstance().getLanguageManager().getParsedBlockLocation(p, loc);
                            MessageReplacement pos = MessageReplacement.replace("%pos%", parsed);
                            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.teleport.set", pos);
                        }))
                .withSubcommand(new CommandAPICommand("tp")
                        .withAliases("teleport")
                        .withPermission(Constants.Permission.TELEPORT)
                        .withArguments(new StringArgument("mineId"))
                        .withOptionalArguments(new EntitySelectorArgument.OnePlayer("player"))
                        .executes((sender, args) -> {
                            Optional<Player> player = args.getOptionalByClass("player", Player.class);
                            if (player.isEmpty()) {
                                return;
                            }

                            String id = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(id);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            Location loc = mine.getTeleportLocation();
                            if (loc == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.teleport.no-loc");
                                return;
                            }

                            player.get().teleportAsync(loc);
                        })
                        .executesPlayer((p, args) -> {
                            String id = (String) args.get("mineId");
                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(id);
                            if (mine == null) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.mine-not-exists");
                                return;
                            }

                            Location loc = mine.getTeleportLocation();
                            if (loc == null) {
                                SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.teleport.no-loc");
                                return;
                            }

                            p.teleportAsync(loc);
                        }))
                .withSubcommand(new CommandAPICommand("setOnlyFillAir")
                        .withPermission(Constants.Permission.SET_ONLY_FILL_AIR)
                        .withArguments(
                                new StringArgument("mineId")
                                        .includeSuggestions(ArgumentSuggestions.strings(getMineList())),
                                new BooleanArgument("onlyFillAir"))
                        .executes((sender, args) -> {
                            String id = (String) args.get("mineId");
                            boolean b = args.getByClassOrDefault("onlyFillAir", boolean.class, false);

                            Mine mine =
                                    SuperMines.getInstance().getMineManager().getMine(id);
                            if (mine == null) {
                                SuperMines.getInstance()
                                        .getLanguageManager()
                                        .sendMessage(sender, "command.mine-not-exists");
                                return;
                            }

                            mine.setOnlyFillAirWhenRegenerate(b);
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(
                                            sender,
                                            b ? "command.fillair.enabled" : "command.fillair.disabled",
                                            MessageReplacement.replace("%mine%", mine.getRawDisplayName()));
                        }))
                .withSubcommand(new CommandAPICommand("wand")
                        .withPermission(Constants.Permission.POS_SET)
                        .executesPlayer((player, args) -> {
                            PlayerInventory inv = player.getInventory();
                            if (inv.firstEmpty() != -1) {
                                inv.addItem(Constants.Items.WAND.apply(player));
                            } else {
                                inv.setItemInMainHand(Constants.Items.WAND.apply(player));
                            }
                        }))
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(Constants.Permission.RELOAD)
                        .executes((sender, args) -> {
                            SuperMines.getInstance()
                                    .getLanguageManager()
                                    .sendMessage(sender, "command.reload.safe-tip");

                            SuperMines.getInstance().reloadConfig();
                            SuperMines.getInstance().getLanguageManager().reload();

                            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.reload.success");
                        }))
                .register();
    }

    private void showHelp(CommandSender sender) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender, "command.help.general");
    }

    private CuboidArea getSelectedArea(Player player, String id, boolean checkExists) {
        if (checkExists && SuperMines.getInstance().getMineManager().getMine(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.exists");
            return null;
        }

        if (!id.matches(Constants.Texts.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.invalid-id");
            return null;
        }

        AreaSelection sel = selectionMap.get(player.getUniqueId());
        if (sel == null || sel.pos1() == null || sel.pos2() == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.selection-not-finished");
            return null;
        }

        if (sel.isAnyMineIn()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.pos-in-mine");
            return null;
        }

        return sel.toCuboidArea();
    }

    private void createMine(Player player, String id, Component displayName) {
        CuboidArea ca = getSelectedArea(player, id, true);
        if (ca == null) return;
        Component name = displayName == null ? Component.text(id) : displayName;
        Mine mine = new Mine(id, name, player.getWorld(), ca, new HashMap<>(), 0, false);
        SuperMines.getInstance().getMineManager().addMine(mine);
        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.create.success");
    }

    private void redefineMine(Player player, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.mine-not-exists");
            return;
        }
        CuboidArea ca = getSelectedArea(player, id, false);
        if (ca == null) return;
        mine.setArea(ca);
        SuperMines.getInstance().getLanguageManager().sendMessage(player, "command.redefine.success");
    }

    private void removeMine(CommandSender sender, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
            return;
        }

        SuperMines.getInstance().getTaskMaker().cancelMineResetTask(mine);
        SuperMines.getInstance().getMineManager().removeMine(id);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.remove.success");
    }

    private void resetMine(CommandSender sender, String id) {
        Mine mine = SuperMines.getInstance().getMineManager().getMine(id);
        if (mine == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.mine-not-exists");
            return;
        }

        SuperMines.getInstance().getTaskMaker().runMineResetTaskNow(mine);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender, "command.reset.success");
    }

    private <T extends Identified> void list(CommandSender sender, T[] array) {
        Component head =
                SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.head.mine");
        String color = SuperMines.getInstance().getLanguageManager().getMsg(sender, "command.list.color");
        Component sep = SuperMines.getInstance().getLanguageManager().getMsgComponent(sender, "command.list.separator");
        Component msg = head;
        for (int i = 0; i < array.length; i++) {
            T t = array[i];
            msg = msg.append(ComponentUtils.deserialize(color + t.getRawDisplayName() + "(" + t.getId() + ")"));
            if (i < array.length - 1) msg = msg.append(sep);
        }
        sender.sendMessage(msg);
    }

    private Set<String> getMineList() {
        return SuperMines.getInstance().getMineManager().getAllMineIds();
    }

    private Set<String> getTreasuresList() {
        return SuperMines.getInstance().getTreasureManager().getAllTreasureIds();
    }

    private Set<String> getRankList() {
        return SuperMines.getInstance().getRankManager().getAllRankIds();
    }
}
