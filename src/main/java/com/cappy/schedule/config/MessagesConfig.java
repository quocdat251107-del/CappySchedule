package com.cappy.schedule.config;

import com.cappy.schedule.CappySchedulePlugin;
import com.cappy.schedule.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesConfig {

    private final CappySchedulePlugin plugin;
    private FileConfiguration messages;
    private File file;

    public MessagesConfig(CappySchedulePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getRaw(String key) {
        return messages.getString(key, key);
    }

    public String get(String key, Map<String, String> placeholders) {
        String msg = getRaw(key);
        String prefix = plugin.getPluginConfig().getPrefix();
        msg = msg.replace("{prefix}", prefix);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
            }
        }

        return ColorUtil.colorize(msg);
    }

    public String get(String key) {
        return get(key, null);
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String msg = get(key, placeholders);
        if (msg != null && !msg.trim().isEmpty()) {
            sender.sendMessage(msg);
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, null);
    }
}

