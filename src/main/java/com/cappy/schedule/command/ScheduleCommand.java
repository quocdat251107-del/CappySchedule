package com.cappy.schedule.command;

import com.cappy.schedule.CappySchedulePlugin;
import com.cappy.schedule.model.MobSchedule;
import com.cappy.schedule.model.SpawnLocation;
import com.cappy.schedule.scheduler.TimeParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.time.ZonedDateTime;
import java.util.*;

public class ScheduleCommand implements CommandExecutor, TabCompleter {

    private final CappySchedulePlugin plugin;

    public ScheduleCommand(CappySchedulePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "time":
                handleTime(sender);
                return true;

            case "next":
                handleNext(sender);
                return true;

            case "list":
                if (!sender.hasPermission("cappyschedule.admin.list") && !sender.hasPermission("cappyschedule.admin")) {
                    plugin.getMessagesConfig().send(sender, "no-permission");
                    return true;
                }
                handleList(sender);
                return true;

            case "trigger":
                if (!sender.hasPermission("cappyschedule.admin.trigger") && !sender.hasPermission("cappyschedule.admin")) {
                    plugin.getMessagesConfig().send(sender, "no-permission");
                    return true;
                }
                handleTrigger(sender, args);
                return true;

            case "info":
                handleInfo(sender, args);
                return true;

            case "reload":
                if (!sender.hasPermission("cappyschedule.admin.reload") && !sender.hasPermission("cappyschedule.admin")) {
                    plugin.getMessagesConfig().send(sender, "no-permission");
                    return true;
                }
                handleReload(sender);
                return true;

            default:
                plugin.getMessagesConfig().send(sender, "invalid-command");
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getMessagesConfig().get("help-header"));
        sender.sendMessage(plugin.getMessagesConfig().get("help-next"));
        sender.sendMessage(plugin.getMessagesConfig().get("help-time"));
        sender.sendMessage("§e/cs info <id> §7- Xem thông tin chi tiết lịch trình");
        if (sender.hasPermission("cappyschedule.admin")) {
            sender.sendMessage(plugin.getMessagesConfig().get("help-list"));
            sender.sendMessage(plugin.getMessagesConfig().get("help-trigger"));
            sender.sendMessage(plugin.getMessagesConfig().get("help-reload"));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getPluginConfig().getPrefix() + "§cSử dụng: /cs info <id>");
            return;
        }

        String id = args[1];
        MobSchedule schedule = plugin.getPluginConfig().getSchedule(id);
        if (schedule == null) {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            plugin.getMessagesConfig().send(sender, "schedule-not-found", map);
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(plugin.getPluginConfig().getTimezone());
        ZonedDateTime next = plugin.getScheduleManager().calculateNextRun(schedule, now);
        String nextStr = next != null ? next.format(plugin.getPluginConfig().getDateTimeFormatter()) : "N/A";
        String timeLeft = next != null ? TimeParser.formatDuration(Math.max(0, next.toEpochSecond() - now.toEpochSecond())) : "N/A";
        int alive = plugin.getScheduleManager().getAliveCount(schedule.getId());

        sender.sendMessage("§6====== [ §eThông Tin: " + schedule.getDisplayName() + " §6] ======");
        sender.sendMessage("§eID: §f" + schedule.getId());
        sender.sendMessage("§eTrạng thái: " + (schedule.isEnabled() ? "§a[BẬT]" : "§c[TẮT]"));
        sender.sendMessage("§eNhà cung cấp: §b" + schedule.getProvider().name() + " §7(Mob ID: §f" + schedule.getMobId() + "§7, Level: §f" + schedule.getLevel() + "§7)");
        sender.sendMessage("§eLần xuất hiện kế tiếp: §a" + nextStr + " §7(còn §e" + timeLeft + "§7)");
        sender.sendMessage("§eĐang còn sống trong server: " + (alive > 0 ? "§c" + alive + " con" : "§70"));
        if (!schedule.getLocations().isEmpty()) {
            SpawnLocation loc = schedule.getLocations().get(0);
            sender.sendMessage("§eToạ độ triệu hồi: §b" + loc.getWorldName() + " §7(x: §f" + loc.getX() + "§7, y: §f" + loc.getY() + "§7, z: §f" + loc.getZ() + "§7)");
        }
    }

    private void handleTime(CommandSender sender) {
        ZonedDateTime now = ZonedDateTime.now(plugin.getPluginConfig().getTimezone());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("timezone", plugin.getPluginConfig().getTimezone().getId());
        placeholders.put("time", now.format(plugin.getPluginConfig().getTimeFormatter()));
        placeholders.put("date", now.format(plugin.getPluginConfig().getDateFormatter()));
        plugin.getMessagesConfig().send(sender, "time-info", placeholders);
    }

    private void handleNext(CommandSender sender) {
        ZonedDateTime now = ZonedDateTime.now(plugin.getPluginConfig().getTimezone());
        List<Map.Entry<MobSchedule, ZonedDateTime>> upcoming = new ArrayList<>();

        for (MobSchedule schedule : plugin.getPluginConfig().getSchedules().values()) {
            if (!schedule.isEnabled()) continue;
            ZonedDateTime next = plugin.getScheduleManager().calculateNextRun(schedule, now);
            if (next != null) {
                upcoming.add(Map.entry(schedule, next));
            }
        }

        if (upcoming.isEmpty()) {
            plugin.getMessagesConfig().send(sender, "next-empty");
            return;
        }

        upcoming.sort(Comparator.comparing(Map.Entry::getValue));

        sender.sendMessage(plugin.getMessagesConfig().get("next-header"));
        for (Map.Entry<MobSchedule, ZonedDateTime> entry : upcoming) {
            MobSchedule schedule = entry.getKey();
            ZonedDateTime time = entry.getValue();
            long diffSec = time.toEpochSecond() - now.toEpochSecond();

            String worldName = "Unknown";
            if (!schedule.getLocations().isEmpty()) {
                worldName = schedule.getLocations().get(0).getWorldName();
            }

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", schedule.getDisplayName());
            placeholders.put("time", time.format(plugin.getPluginConfig().getDateTimeFormatter()));
            placeholders.put("time_left", TimeParser.formatDuration(Math.max(0, diffSec)));
            placeholders.put("world", worldName);

            sender.sendMessage(plugin.getMessagesConfig().get("next-item", placeholders));
        }
    }

    private void handleList(CommandSender sender) {
        Collection<MobSchedule> schedules = plugin.getPluginConfig().getSchedules().values();
        ZonedDateTime now = ZonedDateTime.now(plugin.getPluginConfig().getTimezone());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("count", String.valueOf(schedules.size()));
        sender.sendMessage(plugin.getMessagesConfig().get("list-header", headerMap));

        for (MobSchedule s : schedules) {
            ZonedDateTime next = plugin.getScheduleManager().calculateNextRun(s, now);
            String nextStr = next != null ? next.format(plugin.getPluginConfig().getTimeFormatter()) : "N/A";
            int alive = plugin.getScheduleManager().getAliveCount(s.getId());

            String statusStr = s.isEnabled() ? plugin.getMessagesConfig().get("status-enabled") : plugin.getMessagesConfig().get("status-disabled");
            String aliveStr = alive > 0 ? plugin.getMessagesConfig().get("alive-yes").replace("{count}", String.valueOf(alive)) : plugin.getMessagesConfig().get("alive-no");

            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("id", s.getId());
            itemMap.put("name", s.getDisplayName());
            itemMap.put("provider", s.getProvider().name());
            itemMap.put("status", statusStr);
            itemMap.put("next_time", nextStr);
            itemMap.put("alive", aliveStr);

            sender.sendMessage(plugin.getMessagesConfig().get("list-item", itemMap));
        }
    }

    private void handleTrigger(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getPluginConfig().getPrefix() + "§cSử dụng: /cs trigger <id>");
            return;
        }

        String id = args[1];
        MobSchedule schedule = plugin.getPluginConfig().getSchedule(id);
        if (schedule == null) {
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            plugin.getMessagesConfig().send(sender, "schedule-not-found", map);
            return;
        }

        boolean success = plugin.getScheduleManager().triggerSchedule(schedule, true);
        if (success) {
            Map<String, String> map = new HashMap<>();
            map.put("name", schedule.getDisplayName());
            map.put("id", schedule.getId());
            plugin.getMessagesConfig().send(sender, "schedule-triggered", map);
        } else {
            sender.sendMessage(plugin.getPluginConfig().getPrefix() + "§cKhông thể kích hoạt lịch trình!");
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        Map<String, String> map = new HashMap<>();
        map.put("count", String.valueOf(plugin.getPluginConfig().getSchedules().size()));
        plugin.getMessagesConfig().send(sender, "reloaded", map);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("help", "time", "next", "info"));
            if (sender.hasPermission("cappyschedule.admin.list") || sender.hasPermission("cappyschedule.admin")) {
                subs.add("list");
            }
            if (sender.hasPermission("cappyschedule.admin.trigger") || sender.hasPermission("cappyschedule.admin")) {
                subs.add("trigger");
            }
            if (sender.hasPermission("cappyschedule.admin.reload") || sender.hasPermission("cappyschedule.admin")) {
                subs.add("reload");
            }
            StringUtil.copyPartialMatches(args[0], subs, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("trigger") || sub.equals("info")) {
                List<String> ids = new ArrayList<>(plugin.getPluginConfig().getSchedules().keySet());
                StringUtil.copyPartialMatches(args[1], ids, completions);
                Collections.sort(completions);
                return completions;
            }
        }

        return completions;
    }
}

