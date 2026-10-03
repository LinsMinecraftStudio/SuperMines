package io.github.lijinhong11.supermines.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.lijinhong11.mittellib.hook.ContentProviders;
import io.github.lijinhong11.mittellib.iface.block.PackedBlock;
import io.github.lijinhong11.mittellib.math.AreaOfBlocks;
import io.github.lijinhong11.mittellib.math.BlockPos;
import io.github.lijinhong11.mittellib.message.MessageReplacement;
import io.github.lijinhong11.mittellib.utils.StringUtils;
import io.github.lijinhong11.mittellib.utils.components.ComponentUtils;
import io.github.lijinhong11.mittellib.utils.random.WeightedRandomMap;
import io.github.lijinhong11.supermines.SuperMines;
import io.github.lijinhong11.supermines.api.data.PlayerData;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.iface.Identified;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.api.regen.RegenPoint;
import io.github.lijinhong11.supermines.gui.GuiManager;
import io.github.lijinhong11.supermines.listeners.BlockListener;
import io.github.lijinhong11.supermines.utils.Constants;
import io.github.lijinhong11.supermines.utils.selection.AreaSelection;
import io.github.lijinhong11.supermines.utils.selection.SelectionValidator;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class SuperMinesCommand {
    private static final Map<UUID, AreaSelection> selectionMap = new ConcurrentHashMap<>();
    private static final Set<UUID> sphereModePlayers = ConcurrentHashMap.newKeySet();

    private SuperMinesCommand() {}

    public static void register(SuperMines plugin) {
        LiteralCommandNode<CommandSourceStack> root = Commands.literal("supermines")
                .executes(SuperMinesCommand::help)
                .then(Commands.literal("pos1")
                        .requires(playerPermission(Constants.Permission.POS_SET))
                        .executes(c -> pos(c, true, null))
                        .then(Commands.argument("loc", ArgumentTypes.finePosition())
                                .executes(c -> pos(c, true, location(c)))))
                .then(Commands.literal("pos2")
                        .requires(playerPermission(Constants.Permission.POS_SET))
                        .executes(c -> pos(c, false, null))
                        .then(Commands.argument("loc", ArgumentTypes.finePosition())
                                .executes(c -> pos(c, false, location(c)))))
                .then(Commands.literal("sphere")
                        .requires(playerPermission(Constants.Permission.POS_SET))
                        .executes(c -> toggleSphere(player(c)))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(c -> sphere(player(c), IntegerArgumentType.getInteger(c, "radius")))))
                .then(Commands.literal("auto-pickup")
                        .requires(playerPermission(null))
                        .executes(SuperMinesCommand::autoPickup))
                .then(Commands.literal("treasures")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.TREASURES))
                        .executes(c -> helpGroup(c, "treasures"))
                        .then(Commands.literal("list").executes(c -> {
                            list(
                                    sender(c),
                                    SuperMines.getInstance()
                                            .getTreasureManager()
                                            .getAllTreasures()
                                            .toArray(new Treasure[0]));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("copy")
                                .then(Commands.argument("sourceId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("newId", StringArgumentType.word())
                                                .executes(SuperMinesCommand::treasureCopy))))
                        .then(Commands.literal("create")
                                .requires(playerPermission(null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument(
                                                        "weight", DoubleArgumentType.doubleArg(Constants.WEIGHT_MIN))
                                                .executes(c -> treasureCreate(c, false))
                                                .then(Commands.argument(
                                                                "displayName", StringArgumentType.greedyString())
                                                        .executes(c -> treasureCreate(c, true))))))
                        .then(Commands.literal("remove")
                                .requires(playerPermission(null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(SuperMinesCommand::treasureRemove)))
                        .then(Commands.literal("setDisplayName")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                                .executes(SuperMinesCommand::treasureDisplayName))))
                        .then(Commands.literal("setWeight")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument(
                                                        "weight", DoubleArgumentType.doubleArg(Constants.WEIGHT_MIN))
                                                .executes(SuperMinesCommand::treasureWeight))))
                        .then(Commands.literal("setItem")
                                .requires(playerPermission(null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .executes(SuperMinesCommand::treasureItem)))
                        .then(Commands.literal("addMatch")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::treasureAddMatch))))
                        .then(Commands.literal("removeMatch")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::treasureRemoveMatch))))
                        .then(Commands.literal("addCommand")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .executes(SuperMinesCommand::treasureAddCommand))))
                        .then(Commands.literal("removeCommand")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .then(Commands.argument("command", StringArgumentType.greedyString())
                                                .executes(SuperMinesCommand::treasureRemoveCommand))))
                        .then(Commands.literal("listCommands")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .executes(SuperMinesCommand::treasureListCommands))))
                .then(Commands.literal("ranks")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RANKS))
                        .executes(c -> helpGroup(c, "ranks"))
                        .then(Commands.literal("list").executes(c -> {
                            list(
                                    sender(c),
                                    SuperMines.getInstance()
                                            .getRankManager()
                                            .getAllRanks()
                                            .toArray(new Rank[0]));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("copy")
                                .then(Commands.argument("sourceId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .then(Commands.argument("newId", StringArgumentType.word())
                                                .executes(SuperMinesCommand::rankCopy))))
                        .then(Commands.literal("create")
                                .requires(playerPermission(null))
                                .then(Commands.argument("rankId", StringArgumentType.word())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(c -> rankCreate(c, false))
                                                .then(Commands.argument(
                                                                "displayName", StringArgumentType.greedyString())
                                                        .executes(c -> rankCreate(c, true))))))
                        .then(Commands.literal("remove")
                                .requires(playerPermission(null))
                                .then(Commands.argument("rankId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .executes(SuperMinesCommand::rankRemove)))
                        .then(Commands.literal("setLevel")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(SuperMinesCommand::rankLevel))))
                        .then(Commands.literal("setDisplayName")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                                .executes(SuperMinesCommand::rankDisplayName))))
                        .then(Commands.literal("giveRank")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("rankId", StringArgumentType.word())
                                                .suggests((c, b) -> suggest(b, getRankList()))
                                                .executes(c -> rankChange(c, true, false))
                                                .then(Commands.argument("notify", BoolArgumentType.bool())
                                                        .executes(c -> rankChange(c, true, true))))))
                        .then(Commands.literal("takeRank")
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .then(Commands.argument("rankId", StringArgumentType.word())
                                                .suggests((c, b) -> suggest(b, getRankList()))
                                                .executes(c -> rankChange(c, false, false))
                                                .then(Commands.argument("notify", BoolArgumentType.bool())
                                                        .executes(c -> rankChange(c, false, true)))))))
                .then(Commands.literal("regenpoints")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.REGEN_POINTS))
                        .executes(c -> helpGroup(c, "regenpoints"))
                        .then(Commands.literal("list").executes(c -> {
                            list(
                                    sender(c),
                                    SuperMines.getInstance()
                                            .getRegenPointManager()
                                            .getAllRegenPoints()
                                            .toArray(new RegenPoint[0]));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(Commands.literal("create")
                                .requires(playerPermission(null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::pointCreate))))
                        .then(Commands.literal("remove")
                                .requires(playerPermission(null))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .executes(SuperMinesCommand::pointRemove)))
                        .then(Commands.literal("setRespawnSeconds")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                .executes(SuperMinesCommand::pointSeconds))))
                        .then(Commands.literal("setBlock")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::pointBlock))))
                        .then(Commands.literal("addBlock")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("block", StringArgumentType.word())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .then(Commands.argument(
                                                                "weight",
                                                                DoubleArgumentType.doubleArg(Constants.WEIGHT_MIN))
                                                        .executes(SuperMinesCommand::pointAddBlock)))))
                        .then(Commands.literal("removeBlock")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::pointRemoveBlock))))
                        .then(Commands.literal("addReward")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("treasure", StringArgumentType.word())
                                                .suggests((c, b) -> suggest(b, getTreasuresList()))
                                                .then(Commands.argument(
                                                                "chance",
                                                                DoubleArgumentType.doubleArg(Constants.WEIGHT_MIN, 100))
                                                        .executes(SuperMinesCommand::pointAddReward)))))
                        .then(Commands.literal("removeReward")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("treasure", StringArgumentType.word())
                                                .suggests((c, b) -> suggest(b, getTreasuresList()))
                                                .executes(SuperMinesCommand::pointRemoveReward))))
                        .then(Commands.literal("respawnNow")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .executes(SuperMinesCommand::pointRespawn)))
                        .then(Commands.literal("setDisplayName")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                                .executes(SuperMinesCommand::pointName))))
                        .then(Commands.literal("tp")
                                .requires(playerPermission(Constants.Permission.TELEPORT))
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRegenPointsList()))
                                        .executes(SuperMinesCommand::pointTeleport))))
                .then(Commands.literal("create")
                        .requires(playerPermission(Constants.Permission.CREATE))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(c -> create(c, false))
                                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                        .executes(c -> create(c, true)))))
                .then(Commands.literal("redefine")
                        .requires(playerPermission(Constants.Permission.REDEFINE))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::redefine)))
                .then(Commands.literal("remove")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.REMOVE))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::remove)))
                .then(Commands.literal("reset")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RESET))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::reset)))
                .then(Commands.literal("info")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.LIST))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::info)))
                .then(Commands.literal("list")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.LIST))
                        .executes(SuperMinesCommand::listMines))
                .then(Commands.literal("gui")
                        .requires(playerPermission(Constants.Permission.GUI))
                        .executes(c -> {
                            GuiManager.openMain(player(c));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("bindTreasure")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.TREASURES))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("treasureId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .executes(SuperMinesCommand::bindTreasure))))
                .then(Commands.literal("unbindTreasure")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.TREASURES))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("treasureId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getTreasuresList()))
                                        .executes(SuperMinesCommand::unbindTreasure))))
                .then(Commands.literal("setRequiredLevel")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.SET_REQUIRED_LEVEL))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(SuperMinesCommand::setRequiredLevel))))
                .then(Commands.literal("setBlockGenerate")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.BLOCK_GENERATE))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("weight", DoubleArgumentType.doubleArg(Constants.WEIGHT_MIN))
                                        .then(Commands.argument("block", StringArgumentType.greedyString())
                                                .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                                .executes(SuperMinesCommand::setBlockGenerate)))))
                .then(Commands.literal("removeBlockGenerate")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.BLOCK_GENERATE))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .then(Commands.argument("block", StringArgumentType.greedyString())
                                        .suggests((c, b) -> suggest(b, ContentProviders.getBlockSuggestions()))
                                        .executes(SuperMinesCommand::removeBlockGenerate))))
                .then(Commands.literal("setDisplayName")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.SET_DISPLAY_NAME))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                        .executes(SuperMinesCommand::setDisplayName))))
                .then(Commands.literal("setDisplayIcon")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.SET_DISPLAY_ICON))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("icon", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(
                                                b,
                                                Arrays.stream(Material.values())
                                                        .map(Material::toString)
                                                        .toList()))
                                        .executes(SuperMinesCommand::setDisplayIcon))))
                .then(Commands.literal("addAllowedRank")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RANKS))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("rankId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .executes(SuperMinesCommand::addAllowedRank))))
                .then(Commands.literal("removeAllowedRank")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RANKS))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("rankId", StringArgumentType.word())
                                        .suggests((c, b) -> suggest(b, getRankList()))
                                        .executes(SuperMinesCommand::removeAllowedRank))))
                .then(Commands.literal("addResetWarning")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RESET_WARNINGS))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("restSeconds", IntegerArgumentType.integer(1))
                                        .executes(SuperMinesCommand::addResetWarning))))
                .then(Commands.literal("removeResetWarning")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RESET_WARNINGS))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("restSeconds", IntegerArgumentType.integer(1))
                                        .executes(SuperMinesCommand::removeResetWarning))))
                .then(Commands.literal("setResetTime")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.SET_RESET_TIME))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("resetTime", IntegerArgumentType.integer(0))
                                        .executes(SuperMinesCommand::setResetTime))))
                .then(Commands.literal("settp")
                        .requires(playerPermission(Constants.Permission.SET_TELEPORT))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::setTeleport)))
                .then(Commands.literal("setteleport")
                        .requires(playerPermission(Constants.Permission.SET_TELEPORT))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .executes(SuperMinesCommand::setTeleport)))
                .then(Commands.literal("tp")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.TELEPORT))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .executes(SuperMinesCommand::teleportSelf)
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(SuperMinesCommand::teleportOther))))
                .then(Commands.literal("teleport")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.TELEPORT))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .executes(SuperMinesCommand::teleportSelf)
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(SuperMinesCommand::teleportOther))))
                .then(Commands.literal("setOnlyFillAir")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.SET_ONLY_FILL_AIR))
                        .then(Commands.argument("mineId", StringArgumentType.word())
                                .suggests((c, b) -> suggest(b, getMineList()))
                                .then(Commands.argument("onlyFillAir", BoolArgumentType.bool())
                                        .executes(SuperMinesCommand::setOnlyFillAir))))
                .then(Commands.literal("wand")
                        .requires(playerPermission(Constants.Permission.POS_SET))
                        .executes(SuperMinesCommand::wand))
                .then(Commands.literal("about").executes(SuperMinesCommand::about))
                .then(Commands.literal("reload")
                        .requires(s -> s.getSender().hasPermission(Constants.Permission.RELOAD))
                        .executes(SuperMinesCommand::reload))
                .build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar()
                .register(root, plugin.getDescription().getDescription(), List.of("sm", "mine", "mines")));
    }

    private static CommandSender sender(CommandContext<CommandSourceStack> c) {
        return c.getSource().getSender();
    }

    private static Player player(CommandContext<CommandSourceStack> c) {
        return (Player) sender(c);
    }

    private static Predicate<CommandSourceStack> playerPermission(String permission) {
        return source -> source.getSender() instanceof Player
                && (permission == null || source.getSender().hasPermission(permission));
    }

    private static int help(CommandContext<CommandSourceStack> c) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.help.general");
        return Command.SINGLE_SUCCESS;
    }

    private static int helpGroup(CommandContext<CommandSourceStack> c, String group) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.help." + group);
        return Command.SINGLE_SUCCESS;
    }

    public static void handlePos(Player p, boolean pos1, Location forced) {
        Location loc = forced == null ? p.getLocation() : forced;
        if (SuperMines.getInstance().getMineManager().getMine(loc) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.pos.in-mine");
            return;
        }
        UUID id = p.getUniqueId();
        AreaSelection old = selectionMap.get(id);
        if (!SelectionValidator.validateAll(p, old, loc)) return;
        boolean sphere = sphereModePlayers.contains(id);
        if (old == null) old = new AreaSelection(null, null, sphere);
        selectionMap.put(
                id, pos1 ? new AreaSelection(loc, old.pos2(), sphere) : new AreaSelection(old.pos1(), loc, sphere));
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        pos1 ? "command.pos.set.pos1" : "command.pos.set.pos2",
                        MessageReplacement.replace(
                                "%pos%",
                                SuperMines.getInstance().getLanguageManager().getParsedBlockLocation(p, loc)));
    }

    private static int pos(CommandContext<CommandSourceStack> c, boolean pos1, Location loc) {
        handlePos(player(c), pos1, loc);
        return Command.SINGLE_SUCCESS;
    }

    private static Location location(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        var position = c.getArgument("loc", FinePositionResolver.class).resolve(c.getSource());
        Location source = c.getSource().getLocation();
        return new Location(source.getWorld(), position.x(), position.y(), position.z());
    }

    private static int autoPickup(CommandContext<CommandSourceStack> c) {
        Player p = player(c);
        if (!SuperMines.getInstance().getConfig().getBoolean("mine.auto-pickup.enabled", false)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.auto-pickup.unavailable");
            return Command.SINGLE_SUCCESS;
        }
        String permission = SuperMines.getInstance().getConfig().getString("mine.auto-pickup.permission", "");
        if (!permission.isEmpty() && !p.hasPermission(permission)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.auto-pickup.no-permission");
            return Command.SINGLE_SUCCESS;
        }
        BlockListener.togglePlayerAutoPickup(p);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        BlockListener.getPlayerAutoPickup(p)
                                ? "command.auto-pickup.enabled"
                                : "command.auto-pickup.disabled");
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleSphere(Player p) {
        UUID id = p.getUniqueId();
        boolean was = sphereModePlayers.remove(id);
        if (!was) {
            sphereModePlayers.add(id);
            selectionMap.remove(id);
        }
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(p, was ? "command.pos.sphere.disabled" : "command.pos.sphere.enabled");
        return Command.SINGLE_SUCCESS;
    }

    private static int sphere(Player p, int radius) {
        UUID id = p.getUniqueId();
        AreaSelection s = selectionMap.get(id);
        Location center = s != null && s.pos1() != null ? s.pos1() : p.getLocation();
        sphereModePlayers.add(id);
        selectionMap.put(id, new AreaSelection(center, center.clone().add(radius, 0, 0), true));
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "command.pos.sphere.radius-set",
                        MessageReplacement.replace("%radius%", String.valueOf(radius)),
                        MessageReplacement.replace(
                                "%center%",
                                SuperMines.getInstance().getLanguageManager().getParsedBlockLocation(p, center)));
        return Command.SINGLE_SUCCESS;
    }

    private static Treasure treasure(CommandContext<CommandSourceStack> c) {
        return SuperMines.getInstance().getTreasureManager().getTreasure(StringArgumentType.getString(c, "id"));
    }

    private static int treasureCreate(CommandContext<CommandSourceStack> c, boolean named) {
        Player p = player(c);
        String id = StringArgumentType.getString(c, "id");
        if (!id.matches(Constants.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessages(p, "command.invalid-id");
            return Command.SINGLE_SUCCESS;
        }
        if (treasure(c) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessages(p, "command.treasures.create.exists");
            return Command.SINGLE_SUCCESS;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) item = null;
        Component displayName = named
                ? ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName"))
                : ComponentUtils.text(id);
        Treasure t = new Treasure(id, displayName, item, DoubleArgumentType.getDouble(c, "weight"));
        SuperMines.getInstance().getTreasureManager().addTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "command.treasures.create.success",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureRemove(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) {
            SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.treasure-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        SuperMines.getInstance().getTreasureManager().removeTreasure(t.getId());
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.remove.success",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureCopy(CommandContext<CommandSourceStack> c) {
        Treasure source =
                SuperMines.getInstance().getTreasureManager().getTreasure(StringArgumentType.getString(c, "sourceId"));
        String newId = StringArgumentType.getString(c, "newId");
        if (source == null) return missingTreasure(c);
        if (!newId.matches(Constants.ID_PATTERN)
                || SuperMines.getInstance().getTreasureManager().getTreasure(newId) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-id");
            return Command.SINGLE_SUCCESS;
        }
        Treasure copy = source.copy(newId);
        SuperMines.getInstance().getTreasureManager().addTreasure(copy);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.create.copy.success",
                        MessageReplacement.replace("%treasure%", copy.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureDisplayName(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) return missingTreasure(c);
        t.setDisplayName(ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName")));
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.set-display-name",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureWeight(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) return missingTreasure(c);
        double weight = DoubleArgumentType.getDouble(c, "weight");
        t.setWeight(weight);
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.set-weight",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()),
                        MessageReplacement.replace("%weight%", String.valueOf(weight)));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureItem(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        Player p = player(c);
        if (t == null) return missingTreasure(c);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            SuperMines.getInstance().getLanguageManager().sendMessages(p, "command.treasures.no-item-in-hand");
            return Command.SINGLE_SUCCESS;
        }
        t.setItemStack(item);
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "command.treasures.set-item",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureAddMatch(CommandContext<CommandSourceStack> c) {
        return treasureBlock(c, true);
    }

    private static int treasureRemoveMatch(CommandContext<CommandSourceStack> c) {
        return treasureBlock(c, false);
    }

    private static int treasureBlock(CommandContext<CommandSourceStack> c, boolean add) {
        Treasure t = treasure(c);
        PackedBlock b = ContentProviders.getBlock(StringArgumentType.getString(c, "block"));
        if (t == null) return missingTreasure(c);
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        boolean contains = t.getMatchedBlocks().contains(b);
        if (add == contains) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            sender(c),
                            add
                                    ? "command.treasures.matched_blocks.exists"
                                    : "command.treasures.matched_blocks.not_exists");
            return Command.SINGLE_SUCCESS;
        }
        if (add) t.addMatchedBlock(b);
        else t.removeMatchedBlock(b);
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        add
                                ? "command.treasures.matched_blocks.add_success"
                                : "command.treasures.matched_blocks.remove_success",
                        MessageReplacement.replace("%block%", b.getId()),
                        MessageReplacement.replace("%treasure%", add ? t.getRawDisplayName() : t.getId()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureAddCommand(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) return missingTreasure(c);
        String command = StringArgumentType.getString(c, "command");
        if (command.isEmpty()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.treasures.command-empty");
            return Command.SINGLE_SUCCESS;
        }
        t.addConsoleCommand(command);
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.add-command.success",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureRemoveCommand(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) return missingTreasure(c);
        List<String> commands = t.getConsoleCommands();
        if (commands == null || !commands.remove(StringArgumentType.getString(c, "command"))) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(sender(c), "command.treasures.remove-command.not-found");
            return Command.SINGLE_SUCCESS;
        }
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.remove-command.success",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        SuperMines.getInstance().getTreasureManager().saveTreasure(t);
        return Command.SINGLE_SUCCESS;
    }

    private static int treasureListCommands(CommandContext<CommandSourceStack> c) {
        Treasure t = treasure(c);
        if (t == null) return missingTreasure(c);
        List<String> commands = t.getConsoleCommands();
        if (commands == null || commands.isEmpty()) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            sender(c),
                            "command.treasures.list-commands.empty",
                            MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
            return Command.SINGLE_SUCCESS;
        }
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.treasures.list-commands.head",
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        for (int i = 0; i < commands.size(); i++) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            sender(c),
                            "command.treasures.list-commands.line",
                            MessageReplacement.replace("%index%", String.valueOf(i + 1)),
                            MessageReplacement.replace("%command%", commands.get(i)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int missingTreasure(CommandContext<CommandSourceStack> c) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.treasure-not-exists");
        return Command.SINGLE_SUCCESS;
    }

    private static int rankCreate(CommandContext<CommandSourceStack> c, boolean named) {
        String id = StringArgumentType.getString(c, "rankId");
        if (!id.matches(Constants.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.invalid-id");
            return Command.SINGLE_SUCCESS;
        }
        if (SuperMines.getInstance().getRankManager().getRank(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.ranks.create.exists");
            return Command.SINGLE_SUCCESS;
        }
        Component displayName = named
                ? ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName"))
                : ComponentUtils.text(id);
        Rank r = new Rank(IntegerArgumentType.getInteger(c, "level"), id, displayName);
        SuperMines.getInstance().getRankManager().addRank(r);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessages(
                        sender(c), "command.ranks.create.success", MessageReplacement.replace("%rank%", r.getId()));
        return Command.SINGLE_SUCCESS;
    }

    private static int rankRemove(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "rankId");
        if (SuperMines.getInstance().getRankManager().getRank(id) == null) {
            SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        SuperMines.getInstance().getRankManager().removeRank(id);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessages(sender(c), "command.ranks.remove.success", MessageReplacement.replace("%rank%", id));
        return Command.SINGLE_SUCCESS;
    }

    private static int rankCopy(CommandContext<CommandSourceStack> c) {
        Rank source = SuperMines.getInstance().getRankManager().getRank(StringArgumentType.getString(c, "sourceId"));
        String newId = StringArgumentType.getString(c, "newId");
        if (source == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        if (!newId.matches(Constants.ID_PATTERN)
                || SuperMines.getInstance().getRankManager().getRank(newId) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-id");
            return Command.SINGLE_SUCCESS;
        }
        Rank copy = source.copy(newId);
        SuperMines.getInstance().getRankManager().addRank(copy);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.ranks.create.copy",
                        MessageReplacement.replace("%rank%", copy.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int rankLevel(CommandContext<CommandSourceStack> c) {
        Rank r = SuperMines.getInstance().getRankManager().getRank(StringArgumentType.getString(c, "id"));
        if (r == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        int level = IntegerArgumentType.getInteger(c, "level");
        if (level < 1) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.ranks.level-less-than-1");
            return Command.SINGLE_SUCCESS;
        }
        r.setLevel(level);
        SuperMines.getInstance().getRankManager().saveRank(r);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.ranks.set-level",
                        MessageReplacement.replace("%rank%", r.getRawDisplayName()),
                        MessageReplacement.replace("%level%", String.valueOf(level)));
        return Command.SINGLE_SUCCESS;
    }

    private static int rankDisplayName(CommandContext<CommandSourceStack> c) {
        Rank r = SuperMines.getInstance().getRankManager().getRank(StringArgumentType.getString(c, "id"));
        if (r == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        r.setDisplayName(ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName")));
        SuperMines.getInstance().getRankManager().saveRank(r);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.ranks.set-display-name",
                        MessageReplacement.replace("%rank%", r.getId()),
                        MessageReplacement.replace("%displayName%", r.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int rankChange(CommandContext<CommandSourceStack> c, boolean add, boolean hasNotify)
            throws CommandSyntaxException {
        Player p = selectedPlayer(c);
        Rank r = SuperMines.getInstance().getRankManager().getRank(StringArgumentType.getString(c, "rankId"));
        if (r == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        PlayerData d = SuperMines.getInstance().getPlayerDataManager().getOrCreatePlayerData(p.getUniqueId());
        if (add) d.addRank(r);
        else d.removeRank(r);
        SuperMines.getInstance().getPlayerDataManager().savePlayerData(d);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        add ? "command.ranks.give-rank" : "command.ranks.take-rank",
                        MessageReplacement.replace("%player%", p.getName()),
                        MessageReplacement.replace("%rank%", r.getRawDisplayName()));
        if (hasNotify && BoolArgumentType.getBool(c, "notify")) {
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            p,
                            add ? "command.ranks.give-rank-notify" : "command.ranks.take-rank-notify",
                            MessageReplacement.replace("%rank%", r.getRawDisplayName()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static RegenPoint point(CommandContext<CommandSourceStack> c) {
        return SuperMines.getInstance().getRegenPointManager().getRegenPoint(StringArgumentType.getString(c, "id"));
    }

    private static PackedBlock block(CommandContext<CommandSourceStack> c) {
        return ContentProviders.getBlock(StringArgumentType.getString(c, "block"));
    }

    private static int pointCreate(CommandContext<CommandSourceStack> c) {
        Player p = player(c);
        String id = StringArgumentType.getString(c, "id");
        PackedBlock b = block(c);
        if (!id.matches(Constants.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessages(p, "command.invalid-id");
            return Command.SINGLE_SUCCESS;
        }
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        if (SuperMines.getInstance().getRegenPointManager().getRegenPoint(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessages(p, "command.regenpoints.create.exists");
            return Command.SINGLE_SUCCESS;
        }
        var target = p.getTargetBlockExact(5);
        if (target == null || target.getType().isAir()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.regenpoints.no-target-block");
            return Command.SINGLE_SUCCESS;
        }
        Location l = target.getLocation();
        if (SuperMines.getInstance().getRegenPointManager().getRegenPoint(l) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.regenpoints.already-exists");
            return Command.SINGLE_SUCCESS;
        }
        RegenPoint rp = new RegenPoint(
                id,
                l.getWorld(),
                BlockPos.fromLocation(l),
                b,
                SuperMines.getInstance().getRegenPointManager().getDefaultRespawnSeconds());
        SuperMines.getInstance().getRegenPointManager().addRegenPoint(rp);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "command.regenpoints.create.success",
                        MessageReplacement.replace("%point%", rp.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int pointRemove(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        SuperMines.getInstance().getRegenPointManager().removeRegenPoint(p.getId());
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.regenpoints.remove.success",
                        MessageReplacement.replace("%point%", p.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int pointSeconds(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        int seconds = IntegerArgumentType.getInteger(c, "seconds");
        SuperMines.getInstance().getRegenPointManager().setRespawnSeconds(p, seconds);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.regenpoints.set-respawn-seconds",
                        MessageReplacement.replace("%point%", p.getRawDisplayName()),
                        MessageReplacement.replace("%seconds%", String.valueOf(seconds)));
        return Command.SINGLE_SUCCESS;
    }

    private static int pointBlock(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        PackedBlock b = block(c);
        if (p == null) return missingPoint(c);
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        p.setBlock(b);
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.regenpoints.set-block",
                        MessageReplacement.replace("%point%", p.getRawDisplayName()),
                        MessageReplacement.replace("%block%", b.getId()));
        return Command.SINGLE_SUCCESS;
    }

    private static int pointAddBlock(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        PackedBlock b = block(c);
        if (p == null) return missingPoint(c);
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        p.addBlock(b, DoubleArgumentType.getDouble(c, "weight"));
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.regenpoints.add-block.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int pointRemoveBlock(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        PackedBlock b = block(c);
        if (p == null) return missingPoint(c);
        if (b == null || !p.getBlocks().containsKey(b)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        if (p.getBlocks().size() <= 1) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.regenpoints.last-block");
            return Command.SINGLE_SUCCESS;
        }
        p.removeBlock(b);
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(sender(c), "command.regenpoints.remove-block.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int pointAddReward(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        String id = StringArgumentType.getString(c, "treasure");
        if (SuperMines.getInstance().getTreasureManager().getTreasure(id) == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.treasure-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        p.setRewardChance(id, DoubleArgumentType.getDouble(c, "chance"));
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.regenpoints.add-reward.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int pointRemoveReward(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        p.removeReward(StringArgumentType.getString(c, "treasure"));
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(sender(c), "command.regenpoints.remove-reward.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int pointRespawn(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        SuperMines.getInstance().getRegenPointManager().respawnNow(p);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.regenpoints.respawn-now.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int pointName(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        p.setDisplayName(ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName")));
        SuperMines.getInstance().getRegenPointManager().saveRegenPoint(p);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.regenpoints.set-display-name",
                        MessageReplacement.replace("%point%", p.getRawDisplayName()),
                        MessageReplacement.replace("%displayName%", p.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int pointTeleport(CommandContext<CommandSourceStack> c) {
        RegenPoint p = point(c);
        if (p == null) return missingPoint(c);
        Player player = player(c);
        player.teleportAsync(p.getLocation().clone().add(.5, 0, .5)).thenAccept(success -> SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        player,
                        success ? "command.regenpoints.teleport.success" : "command.regenpoints.teleport.failed",
                        MessageReplacement.replace("%point%", p.getRawDisplayName())));
        return Command.SINGLE_SUCCESS;
    }

    private static int missingPoint(CommandContext<CommandSourceStack> c) {
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.regenpoints.point-not-exists");
        return Command.SINGLE_SUCCESS;
    }

    private static AreaOfBlocks selected(Player p, String id, boolean check) {
        if (check && SuperMines.getInstance().getMineManager().getMine(id) != null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.create.exists");
            return null;
        }
        if (!id.matches(Constants.ID_PATTERN)) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.invalid-id");
            return null;
        }
        AreaSelection s = selectionMap.get(p.getUniqueId());
        if (s == null || s.pos1() == null || s.pos2() == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.create.selection-not-finished");
            return null;
        }
        if (s.isAnyMineIn()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.pos.in-mine");
            return null;
        }
        return s.toArea();
    }

    private static int create(CommandContext<CommandSourceStack> c, boolean named) {
        Player p = player(c);
        String id = StringArgumentType.getString(c, "id");
        AreaOfBlocks a = selected(p, id, true);
        if (a == null) return Command.SINGLE_SUCCESS;
        Mine mine = new Mine(
                id,
                named
                        ? ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName"))
                        : ComponentUtils.text(id),
                p.getWorld(),
                a,
                new WeightedRandomMap<>(),
                0,
                false);
        if (!SuperMines.getInstance().getMineManager().tryAddMine(mine)) return Command.SINGLE_SUCCESS;
        SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.create.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int redefine(CommandContext<CommandSourceStack> c) {
        Player p = player(c);
        Mine m = SuperMines.getInstance().getMineManager().getMine(StringArgumentType.getString(c, "id"));
        if (m == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.mine-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        AreaOfBlocks a = selected(p, StringArgumentType.getString(c, "id"), false);
        if (a == null) return Command.SINGLE_SUCCESS;
        m.setArea(a);
        SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.redefine.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int remove(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "mineId");
        if (SuperMines.getInstance().getMineManager().getMine(id) == null) return missingMine(c);
        if (SuperMines.getInstance().getMineManager().tryRemoveMine(id))
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.remove.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int reset(CommandContext<CommandSourceStack> c) {
        Mine m = SuperMines.getInstance().getMineManager().getMine(StringArgumentType.getString(c, "id"));
        if (m == null) return missingMine(c);
        SuperMines.getInstance().getTaskMaker().runMineResetTaskNow(m);
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.reset.success");
        return Command.SINGLE_SUCCESS;
    }

    private static int listMines(CommandContext<CommandSourceStack> c) {
        list(sender(c), SuperMines.getInstance().getMineManager().getAllMines().toArray(new Mine[0]));
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<CommandSourceStack> c) {
        Mine m = SuperMines.getInstance().getMineManager().getMine(StringArgumentType.getString(c, "id"));
        if (m == null) return missingMine(c);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessages(
                        sender(c),
                        "command.mine-info",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%world%", m.getWorld().getName()),
                        MessageReplacement.replace("%reset%", String.valueOf(m.getRegenerateSeconds())),
                        MessageReplacement.replace(
                                "%blocks%", String.valueOf(m.getArea().volume())),
                        MessageReplacement.replace(
                                "%generated%",
                                String.valueOf(m.getBlockSpawnEntries().size())),
                        MessageReplacement.replace(
                                "%treasures%", String.valueOf(m.getTreasures().size())),
                        MessageReplacement.replace("%broken%", String.valueOf(m.getBlocksBroken())));
        return Command.SINGLE_SUCCESS;
    }

    private static Mine mine(CommandContext<CommandSourceStack> c) {
        return SuperMines.getInstance().getMineManager().getMine(StringArgumentType.getString(c, "mineId"));
    }

    private static int missingMine(CommandContext<CommandSourceStack> c) {
        SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.mine-not-exists");
        return Command.SINGLE_SUCCESS;
    }

    private static int bindTreasure(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        Treasure t = SuperMines.getInstance()
                .getTreasureManager()
                .getTreasure(StringArgumentType.getString(c, "treasureId"));
        if (t == null) return missingTreasure(c);
        if (m.getTreasures().stream().anyMatch(existing -> existing.getId().equals(t.getId()))) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.add-treasure.exists");
            return Command.SINGLE_SUCCESS;
        }
        m.addTreasure(t);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.add-treasure.success",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int unbindTreasure(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        Treasure t = SuperMines.getInstance()
                .getTreasureManager()
                .getTreasure(StringArgumentType.getString(c, "treasureId"));
        if (t == null) return missingTreasure(c);
        if (m.getTreasures().stream().noneMatch(existing -> existing.getId().equals(t.getId()))) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.remove-treasure.not-bound");
            return Command.SINGLE_SUCCESS;
        }
        m.removeTreasure(t);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.remove-treasure.success",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%treasure%", t.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int setRequiredLevel(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        int level = IntegerArgumentType.getInteger(c, "level");
        m.setRequiredRankLevel(level);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.set-required-level",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%level%", String.valueOf(level)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setBlockGenerate(CommandContext<CommandSourceStack> c) {
        PackedBlock b = block(c);
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        double weight = DoubleArgumentType.getDouble(c, "weight");
        m.addBlockSpawnEntry(b, weight);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.block-generate.success",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%block%", b.getId()),
                        MessageReplacement.replace("%weight%", String.valueOf(weight)));
        return Command.SINGLE_SUCCESS;
    }

    private static int removeBlockGenerate(CommandContext<CommandSourceStack> c) {
        PackedBlock b = block(c);
        if (b == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-block");
            return Command.SINGLE_SUCCESS;
        }
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        m.removeBlockSpawnEntry(b);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.block-generate.removed",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDisplayName(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        m.setDisplayName(ComponentUtils.deserialize(StringArgumentType.getString(c, "displayName")));
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.set-display-name",
                        MessageReplacement.replace("%mine%", m.getId()),
                        MessageReplacement.replace("%displayName%", m.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int setDisplayIcon(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        Material icon;
        try {
            icon = Material.valueOf(StringArgumentType.getString(c, "icon").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            icon = null;
        }
        if (icon == null || icon.isAir()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.invalid-material");
            return Command.SINGLE_SUCCESS;
        }
        m.setDisplayIcon(icon);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        "command.set-display-icon",
                        MessageReplacement.replace("%mine%", m.getId()),
                        MessageReplacement.replace("%item%", icon.toString()));
        return Command.SINGLE_SUCCESS;
    }

    private static int addAllowedRank(CommandContext<CommandSourceStack> c) {
        return changeAllowedRank(c, true);
    }

    private static int removeAllowedRank(CommandContext<CommandSourceStack> c) {
        return changeAllowedRank(c, false);
    }

    private static int changeAllowedRank(CommandContext<CommandSourceStack> c, boolean add) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        Rank r = SuperMines.getInstance().getRankManager().getRank(StringArgumentType.getString(c, "rankId"));
        if (r == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.rank-not-exists");
            return Command.SINGLE_SUCCESS;
        }
        if (add) m.addAllowedRankId(r.getId());
        else m.removeAllowedRankId(r.getId());
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        add ? "command.ranks.allowed" : "command.ranks.disallowed",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%rank%", r.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int addResetWarning(CommandContext<CommandSourceStack> c) {
        return changeResetWarning(c, true);
    }

    private static int removeResetWarning(CommandContext<CommandSourceStack> c) {
        return changeResetWarning(c, false);
    }

    private static int changeResetWarning(CommandContext<CommandSourceStack> c, boolean add) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        int seconds = IntegerArgumentType.getInteger(c, "restSeconds");
        if (seconds >= m.getRegenerateSeconds()) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.resetwarning.time-too-long");
            return Command.SINGLE_SUCCESS;
        }
        if (add) m.getWarningSeconds().add(seconds);
        else m.getWarningSeconds().remove(seconds);
        SuperMines.getInstance().getMineManager().saveMine(m);
        if (add) SuperMines.getInstance().getTaskMaker().startMineWarningTask(m, seconds);
        else SuperMines.getInstance().getTaskMaker().cancelMineWarningTask(m, seconds);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        add ? "command.resetwarning.started" : "command.resetwarning.stopped",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%seconds%", StringUtils.formatCountdown(seconds)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setResetTime(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        int seconds = IntegerArgumentType.getInteger(c, "resetTime");
        m.setRegenerateSeconds(seconds);
        SuperMines.getInstance().getMineManager().saveMine(m);
        if (seconds <= 0) {
            SuperMines.getInstance().getTaskMaker().cancelMineResetTask(m);
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            sender(c),
                            "command.reset.stop-reset",
                            MessageReplacement.replace("%mine%", m.getRawDisplayName()));
        } else {
            SuperMines.getInstance().getTaskMaker().restartMineResetTask(m);
            SuperMines.getInstance()
                    .getLanguageManager()
                    .sendMessage(
                            sender(c),
                            "command.reset.time-set",
                            MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                            MessageReplacement.replace("%time%", StringUtils.formatCountdown(seconds)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setTeleport(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        Player p = player(c);
        Location location = p.getLocation();
        m.setTeleportLocation(location);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        "command.teleport.set",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace(
                                "%loc%",
                                SuperMines.getInstance().getLanguageManager().getParsedBlockLocation(p, location)));
        return Command.SINGLE_SUCCESS;
    }

    private static int teleportSelf(CommandContext<CommandSourceStack> c) {
        if (!(sender(c) instanceof Player p)) return Command.SINGLE_SUCCESS;
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        if (m.getTeleportLocation() == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(p, "command.teleport.no-loc");
            return Command.SINGLE_SUCCESS;
        }
        p.teleportAsync(m.getTeleportLocation()).thenAccept(success -> SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        p,
                        success ? "command.teleport.success" : "command.teleport.failed",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName())));
        return Command.SINGLE_SUCCESS;
    }

    private static int teleportOther(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        if (m.getTeleportLocation() == null) {
            SuperMines.getInstance().getLanguageManager().sendMessage(sender(c), "command.teleport.no-loc");
            return Command.SINGLE_SUCCESS;
        }
        Player p = selectedPlayer(c);
        p.teleportAsync(m.getTeleportLocation()).thenAccept(success -> SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        success ? "command.teleport.success-other" : "command.teleport.failed",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()),
                        MessageReplacement.replace("%player%", p.getName())));
        return Command.SINGLE_SUCCESS;
    }

    private static int setOnlyFillAir(CommandContext<CommandSourceStack> c) {
        Mine m = mine(c);
        if (m == null) return missingMine(c);
        boolean onlyFillAir = BoolArgumentType.getBool(c, "onlyFillAir");
        m.setOnlyFillAirWhenRegenerate(onlyFillAir);
        SuperMines.getInstance().getMineManager().saveMine(m);
        SuperMines.getInstance()
                .getLanguageManager()
                .sendMessage(
                        sender(c),
                        onlyFillAir ? "command.fillair.enabled" : "command.fillair.disabled",
                        MessageReplacement.replace("%mine%", m.getRawDisplayName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int wand(CommandContext<CommandSourceStack> c) {
        PlayerInventory i = player(c).getInventory();
        ItemStack w = Constants.Items.WAND.apply(player(c));
        if (i.firstEmpty() == -1) i.setItemInMainHand(w);
        else i.addItem(w);
        return Command.SINGLE_SUCCESS;
    }

    private static int about(CommandContext<CommandSourceStack> c) {
        SuperMines.getInstance().getLanguageManager().sendMessages(sender(c), "command.about");
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        SuperMines p = SuperMines.getInstance();
        p.getLanguageManager().sendMessage(sender(c), "command.reload.safe-tip");
        p.getTaskMaker().reload();
        p.getTreasureManager().reloadData();
        p.getRankManager().reloadData();
        p.getRegenPointManager().reloadData();
        p.getMineManager().reloadData();
        p.reloadConfig();
        p.getLanguageManager().reload();
        p.getTaskMaker().startup();
        p.getRegenPointManager().startup();
        p.getLanguageManager().sendMessage(sender(c), "command.reload.success");
        return Command.SINGLE_SUCCESS;
    }

    private static Player selectedPlayer(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        return c.getArgument("player", PlayerSelectorArgumentResolver.class)
                .resolve(c.getSource())
                .getFirst();
    }

    private static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Iterable<String> values) {
        String remaining = builder.getRemainingLowerCase();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) builder.suggest(value);
        }
        return builder.buildFuture();
    }

    private static Set<String> getMineList() {
        return SuperMines.getInstance().getMineManager().getAllMineIds();
    }

    private static Set<String> getTreasuresList() {
        return SuperMines.getInstance().getTreasureManager().getAllTreasureIds();
    }

    private static Set<String> getRankList() {
        return SuperMines.getInstance().getRankManager().getAllRankIds();
    }

    private static Set<String> getRegenPointsList() {
        return SuperMines.getInstance().getRegenPointManager().getAllRegenPointIds();
    }

    private static <T extends Identified> void list(CommandSender s, T[] a) {
        Component msg = SuperMines.getInstance().getLanguageManager().getMsgComponent(s, "command.list.head.mine");
        String color = SuperMines.getInstance().getLanguageManager().getMsg(s, "command.list.color");
        Component sep = SuperMines.getInstance().getLanguageManager().getMsgComponent(s, "command.list.separator");
        for (int i = 0; i < a.length; i++) {
            msg = msg.append(ComponentUtils.deserialize(color + a[i].getRawDisplayName() + "(" + a[i].getId() + ")"));
            if (i < a.length - 1) msg = msg.append(sep);
        }
        s.sendMessage(msg);
    }
}
