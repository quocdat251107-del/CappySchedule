package com.cappy.schedule.config;

import com.cappy.schedule.CappySchedulePlugin;
import com.cappy.schedule.model.*;
import com.cappy.schedule.scheduler.TimeParser;
import com.cappy.schedule.util.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PluginConfig {

    private final CappySchedulePlugin plugin;

    private ZoneId timezone;
    private DateTimeFormatter timeFormatter;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private long tickInterval;
    private String prefix;
    private final Map<String, MobSchedule> schedules = new LinkedHashMap<>();

    public PluginConfig(CappySchedulePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Timezone
        String tzStr = config.getString("timezone", "Asia/Ho_Chi_Minh");
        try {
            this.timezone = ZoneId.of(tzStr);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid timezone '" + tzStr + "'. Falling back to system default timezone!");
            this.timezone = ZoneId.systemDefault();
        }

        // Formats
        String timePattern = config.getString("time-format", "HH:mm:ss");
        String datePattern = config.getString("date-format", "yyyy-MM-dd");
        String dateTimePattern = config.getString("datetime-format", "yyyy-MM-dd HH:mm:ss");

        this.timeFormatter = DateTimeFormatter.ofPattern(timePattern);
        this.dateFormatter = DateTimeFormatter.ofPattern(datePattern);
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);

        this.tickInterval = config.getLong("scheduler-tick-interval", 20L);
        this.prefix = ColorUtil.colorize(config.getString("prefix", "&6[CappySchedule]&r "));

        // Load schedules
        schedules.clear();
        ConfigurationSection sec = config.getConfigurationSection("schedules");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection s = sec.getConfigurationSection(key);
                if (s == null) continue;

                boolean enabled = s.getBoolean("enabled", true);
                String displayName = s.getString("display_name", key);
                MobProvider provider = MobProvider.fromString(s.getString("provider", "MYTHICMOBS"));
                String mobId = s.getString("mob_id", key);
                double level = s.getDouble("level", 1.0);

                // Times
                List<String> rawTimes = s.getStringList("times");
                List<LocalTime> times = new ArrayList<>();
                for (String t : rawTimes) {
                    LocalTime lt = TimeParser.parseTime(t);
                    if (lt != null) {
                        times.add(lt);
                    }
                }

                // Days
                List<String> rawDays = s.getStringList("days");
                Set<DayOfWeek> days = TimeParser.parseDays(rawDays);

                // Interval
                String rawInterval = s.getString("interval");
                Long intervalSeconds = TimeParser.parseIntervalSeconds(rawInterval);

                // Locations
                List<SpawnLocation> locations = new ArrayList<>();
                List<Map<?, ?>> locList = s.getMapList("locations");
                for (Map<?, ?> map : locList) {
                    String world = Objects.toString(map.get("world"), "world");
                    double x = toDouble(map.get("x"), 0.0);
                    double y = toDouble(map.get("y"), 64.0);
                    double z = toDouble(map.get("z"), 0.0);
                    float yaw = (float) toDouble(map.get("yaw"), 0.0);
                    float pitch = (float) toDouble(map.get("pitch"), 0.0);
                    double radius = toDouble(map.get("radius"), 0.0);
                    boolean safeSpawn = toBoolean(map.get("safe_spawn"), true);

                    locations.add(new SpawnLocation(world, x, y, z, yaw, pitch, radius, safeSpawn));
                }

                boolean preventStacking = s.getBoolean("prevent_stacking", true);
                long despawnAfterSeconds = s.getLong("despawn_after_seconds", 0L);

                // Warnings
                List<WarningConfig> warnings = new ArrayList<>();
                List<Map<?, ?>> rawWarnings = s.getMapList("warnings");
                for (Map<?, ?> wMap : rawWarnings) {
                    String timeBeforeStr = Objects.toString(wMap.get("time_before"), "1m");
                    long secondsBefore = TimeParser.parseDurationSeconds(timeBeforeStr);
                    boolean bChat = toBoolean(wMap.get("broadcast_chat"), true);
                    String chatMsg = Objects.toString(wMap.get("chat_message"), null);
                    String title = Objects.toString(wMap.get("title"), null);
                    String subtitle = Objects.toString(wMap.get("subtitle"), null);
                    String actionbar = Objects.toString(wMap.get("actionbar"), null);
                    String sound = Objects.toString(wMap.get("sound"), null);

                    warnings.add(new WarningConfig(secondsBefore, bChat, chatMsg, title, subtitle, actionbar, sound));
                }

                // On Spawn Action
                ConfigurationSection spawnSec = s.getConfigurationSection("on_spawn");
                SpawnAction onSpawn = null;
                if (spawnSec != null) {
                    boolean bChat = spawnSec.getBoolean("broadcast_chat", true);
                    String chatMsg = spawnSec.getString("chat_message");
                    String title = spawnSec.getString("title");
                    String subtitle = spawnSec.getString("subtitle");
                    String actionbar = spawnSec.getString("actionbar");
                    String sound = spawnSec.getString("sound");
                    boolean fireworks = spawnSec.getBoolean("spawn_fireworks", false);
                    List<String> commands = spawnSec.getStringList("commands");
                    onSpawn = new SpawnAction(bChat, chatMsg, title, subtitle, actionbar, sound, fireworks, commands);
                }

                // On Kill Action
                ConfigurationSection killSec = s.getConfigurationSection("on_kill");
                KillAction onKill = null;
                if (killSec != null) {
                    boolean bChat = killSec.getBoolean("broadcast_chat", true);
                    String chatMsg = killSec.getString("chat_message");
                    String title = killSec.getString("title");
                    String subtitle = killSec.getString("subtitle");
                    String actionbar = killSec.getString("actionbar");
                    String sound = killSec.getString("sound");
                    List<String> commands = killSec.getStringList("commands");
                    onKill = new KillAction(bChat, chatMsg, title, subtitle, actionbar, sound, commands);
                }

                // On Despawn Action
                ConfigurationSection despawnSec = s.getConfigurationSection("on_despawn");
                DespawnAction onDespawn = null;
                if (despawnSec != null) {
                    boolean bChat = despawnSec.getBoolean("broadcast_chat", true);
                    String chatMsg = despawnSec.getString("chat_message");
                    String title = despawnSec.getString("title");
                    String subtitle = despawnSec.getString("subtitle");
                    String actionbar = despawnSec.getString("actionbar");
                    String sound = despawnSec.getString("sound");
                    List<String> commands = despawnSec.getStringList("commands");
                    onDespawn = new DespawnAction(bChat, chatMsg, title, subtitle, actionbar, sound, commands);
                }

                MobSchedule schedule = new MobSchedule(
                        key, enabled, displayName, provider, mobId, level,
                        times, days, intervalSeconds, locations, preventStacking,
                        despawnAfterSeconds, warnings, onSpawn, onKill, onDespawn
                );

                schedules.put(key.toLowerCase(), schedule);
            }
        }
    }

    private double toDouble(Object obj, double def) {
        if (obj instanceof Number num) return num.doubleValue();
        if (obj instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private boolean toBoolean(Object obj, boolean def) {
        if (obj instanceof Boolean b) return b;
        if (obj instanceof String str) return Boolean.parseBoolean(str);
        return def;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public long getTickInterval() {
        return tickInterval;
    }

    public String getPrefix() {
        return prefix;
    }

    public Map<String, MobSchedule> getSchedules() {
        return Collections.unmodifiableMap(schedules);
    }

    public MobSchedule getSchedule(String id) {
        if (id == null) return null;
        return schedules.get(id.toLowerCase());
    }
}

