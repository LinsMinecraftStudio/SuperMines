package io.github.lijinhong11.supermines.message;

import java.io.File;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.base.Preconditions;
import io.github.lijinhong11.supermines.api.data.Rank;
import io.github.lijinhong11.supermines.api.mine.Mine;
import io.github.lijinhong11.supermines.api.mine.Treasure;
import io.github.lijinhong11.supermines.utils.ComponentUtils;
import io.github.lijinhong11.supermines.utils.ConfigFileUtil;
import io.github.lijinhong11.supermines.utils.Constants;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class to manage language files and messages.
 * You can create a new instance of this class by passing a reference to your plugin.
 */
public final class LanguageManager {
    private final Plugin plugin;
    private final String defaultLanguage;

    private final Map<String, YamlConfiguration> configurations = new HashMap<>();

    private boolean detectPlayerLocale;
    private YamlConfiguration defaultConfiguration;

    public LanguageManager(Plugin plugin) {
        this(plugin, "en-US");
    }

    public LanguageManager(Plugin plugin, String defaultLanguage) {
        this.plugin = plugin;
        this.defaultLanguage = defaultLanguage;

        loadLanguages();
    }

    private void loadLanguages() {
        detectPlayerLocale = plugin.getConfig().getBoolean("detect-player-locale", true);

        defaultConfiguration =
                YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "language/en-US.yml"));

        File pluginFolder = plugin.getDataFolder();

        URL fileURL = Objects.requireNonNull(plugin.getClass().getClassLoader().getResource("language/"));
        String jarPath = fileURL.toString().substring(0, fileURL.toString().indexOf("!/") + 2);

        try {
            URL jar = URI.create(jarPath).toURL();
            JarURLConnection jarCon = (JarURLConnection) jar.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                String name = entry.getName();
                if (name.startsWith("language/") && !entry.isDirectory()) {
                    String realName = name.replaceAll("language/", "");
                    try (InputStream stream = plugin.getClass().getClassLoader().getResourceAsStream(name)) {
                        File destinationFile = new File(pluginFolder, "language/" + realName);

                        if (!destinationFile.exists() && stream != null) {
                            plugin.saveResource("language/" + realName, false);
                        }

                        ConfigFileUtil.completeLangFile(plugin, "language/" + realName);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        File[] languageFiles = new File(pluginFolder, "language").listFiles();
        if (languageFiles != null) {
            for (File languageFile : languageFiles) {
                String language = convertToRightLangCode(languageFile.getName().replaceAll(".yml", ""));
                configurations.put(language, YamlConfiguration.loadConfiguration(languageFile));
            }
        }
    }

    private String convertToRightLangCode(String lang) {
        if (lang == null || lang.isBlank()) return "en-US";
        String[] split = lang.split("-");
        if (split.length == 1) {
            String[] split2 = lang.split("_");
            if (split2.length == 1) return lang;
            return lang.replace(split2[1], split2[1].toUpperCase());
        }
        return lang.replace(split[1], split[1].toUpperCase());
    }

    public List<Component> getMineInfo(@NotNull Player p, @NotNull Mine mine) {
        Preconditions.checkNotNull(mine, "mine cannot be null");
        MessageReplacement world = MessageReplacement.replace("%world%", mine.getWorld().getName());
        MessageReplacement regenerateSeconds = MessageReplacement.replace("%regenerate_seconds%", String.valueOf(mine.getRegenerateSeconds()));
        MessageReplacement pos1 = MessageReplacement.replace("%pos1%", mine.getArea().pos1().toString());
        MessageReplacement pos2 = MessageReplacement.replace("%pos2%", mine.getArea().pos2().toString());
        return getMsgComponentList(p, "gui.mines.info", world, regenerateSeconds, pos1, pos2);
    }

    public List<Component> getTreasureInfo(@NotNull Player p, @NotNull Treasure treasure) {
        MessageReplacement chance = MessageReplacement.replace("%chance%", String.valueOf(treasure.getChance()));
        MessageReplacement matchedMaterials = MessageReplacement.replace("%matched_materials%", String.valueOf(treasure.getMatchedMaterials().size()));
        return getMsgComponentList(p, "gui.treasures.info", chance, matchedMaterials);
    }

    public List<Component> getRankInfo(@NotNull Player p, @NotNull Rank rank) {
        MessageReplacement level = MessageReplacement.replace("%level%", String.valueOf(rank.getLevel()));
        return getMsgComponentList(p, "gui.ranks.info", level);
    }

    public void sendMessage(CommandSender commandSender, String key, MessageReplacement... args) {
        commandSender.sendMessage(parseToComponent(getMsg(commandSender, key, args)));
    }

    public void sendMessages(CommandSender commandSender, String key, MessageReplacement... args) {
        for (String msg : getMsgList(commandSender, key, args)) {
            commandSender.sendMessage(parseToComponent(msg));
        }
    }

    public void consoleMessage(String key, MessageReplacement... args) {
        Bukkit.getConsoleSender().sendMessage(parseToComponent(getMsg(null, key, args)));
    }

    public Component getMsgComponent(@Nullable CommandSender commandSender, String key, MessageReplacement... args) {
        return parseToComponent(getMsg(commandSender, key, args));
    }

    public Component getMsgComponentByLanguage(@Nullable String lang, String key, MessageReplacement... args) {
        return parseToComponent(getMsgByLanguage(lang, key, args));
    }

    public List<Component> getMsgComponentList(
            @Nullable CommandSender commandSender, String key, MessageReplacement... args) {
        return parseToComponentList(getMsgList(commandSender, key, args));
    }

    public List<Component> getMsgComponentListByLanguage(
            @Nullable String lang, String key, MessageReplacement... args) {
        return parseToComponentList(getMsgListByLanguage(lang, key, args));
    }

    public String getMsg(@Nullable CommandSender commandSender, String key, MessageReplacement... args) {
        String msg = getConfiguration(commandSender).getString(key);
        if (msg == null) {
            return key;
        }

        for (MessageReplacement arg : args) {
            msg = arg.parse(msg);
        }

        return msg;
    }

    public List<String> getMsgList(@Nullable CommandSender commandSender, String key, MessageReplacement... args) {
        List<String> msgList = getConfiguration(commandSender).getStringList(key);
        for (MessageReplacement arg : args) {
            msgList.replaceAll(arg::parse);
        }

        return msgList;
    }

    public String getMsgByLanguage(@Nullable String lang, String key, MessageReplacement... args) {
        String msg = getConfiguration(lang).getString(key);
        if (msg == null) {
            return key;
        }

        for (MessageReplacement arg : args) {
            msg = arg.parse(msg);
        }

        return msg;
    }

    public List<String> getMsgListByLanguage(@Nullable String lang, String key, MessageReplacement... args) {
        List<String> msgList = getConfiguration(lang).getStringList(key);
        for (MessageReplacement arg : args) {
            msgList.replaceAll(arg::parse);
        }

        return msgList;
    }

    public void reload() {
        loadLanguages();
    }

    public static Component parseToComponent(String msg) {
        return Constants.StringsAndComponents.RESET.append(ComponentUtils.deserialize(msg));
    }

    public static List<Component> parseToComponentList(List<String> msgList) {
        return msgList.stream().map(LanguageManager::parseToComponent).toList();
    }

    private Configuration getConfiguration(CommandSender p) {
        if (!detectPlayerLocale || !(p instanceof Player pl)) {
            String lang = plugin.getConfig().getString("language", defaultLanguage);
            return configurations.getOrDefault(lang, defaultConfiguration);
        }

        return configurations.getOrDefault(pl.locale().toLanguageTag(), defaultConfiguration);
    }

    private Configuration getConfiguration(String lang) {
        return configurations.getOrDefault(Objects.requireNonNullElse(lang, defaultLanguage), defaultConfiguration);
    }
}