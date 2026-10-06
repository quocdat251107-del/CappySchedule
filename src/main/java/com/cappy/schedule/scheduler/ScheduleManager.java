package com.cappy.schedule.scheduler;

import com.cappy.schedule.CappySchedulePlugin;
import com.cappy.schedule.model.*;
import com.cappy.schedule.util.ColorUtil;
import com.cappy.schedule.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleManager {

    private final CappySchedulePlugin plugin;
    private BukkitTask mainTask;

    // Track active mob instances per schedule ID: scheduleId -> List<ActiveMobInstance>
    private final Map<String, List<ActiveMobInstance>> activeMobs = new ConcurrentHashMap<>();

    // Track cached next run: scheduleId -> ZonedDateTime
    private final Map<String, ZonedDateTime> nextRunCache = new ConcurrentHashMap<>();

    // Track interval last run timestamp: scheduleId -> EpochMilli
    private final Map<String, Long> intervalLastRun = new ConcurrentHashMap<>();

    // Track fired warnings for next scheduled run: scheduleId -> Set of warning seconds already fired for the current target run
    private final Map<String, Set<Long>> firedWarnings = new ConcurrentHashMap<>();
    private long tickCounter = 0;

    public ScheduleManager(CappySchedulePlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        long interval = plugin.getPluginConfig().getTickInterval();
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
    }

    public void stop() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        firedWarnings.clear();
        nextRunCache.clear();
    }

    public void cleanupAllMobs() {
        for (List<ActiveMobInstance> instances : activeMobs.values()) {
            for (ActiveMobInstance instance : instances) {
                instance.remove();
            }
        }
        activeMobs.clear();
    }

    private void tick() {
        tickCounter++;
        ZoneId zone = plugin.getPluginConfig().getTimezone();
        ZonedDateTime now = ZonedDateTime.now(zone);
        long nowEpochSec = now.toEpochSecond();

        for (MobSchedule schedule : plugin.getPluginConfig().getSchedules().values()) {
            if (!schedule.isEnabled()) continue;

            // Use cached next run if valid, otherwise compute and cache
            ZonedDateTime nextRun = nextRunCache.get(schedule.getId());
            if (nextRun == null || now.isAfter(nextRun)) {
                nextRun = calculateNextRun(schedule, now);
                if (nextRun != null) {
                    nextRunCache.put(schedule.getId(), nextRun);
                    firedWarnings.put(schedule.getId(), ConcurrentHashMap.newKeySet());
                } else {
                    nextRunCache.remove(schedule.getId());
                    continue;
                }
            }

            long nextRunEpochSec = nextRun.toEpochSecond();
            long secondsUntilRun = nextRunEpochSec - nowEpochSec;

            // Check Warnings
            Set<Long> fired = firedWarnings.computeIfAbsent(schedule.getId(), k -> ConcurrentHashMap.newKeySet());
            for (WarningConfig warning : schedule.getWarnings()) {
                long warnSec = warning.getSecondsBefore();
                if (!fired.contains(warnSec) && secondsUntilRun <= warnSec && secondsUntilRun > (warnSec - 3)) {
                    fired.add(warnSec);
                    fireWarning(schedule, warning, secondsUntilRun);
                }
            }

            // Check Trigger / Spawn (within 1 second window)
            if (secondsUntilRun <= 0 && secondsUntilRun >= -2) {
                // If it's interval based, update interval tracker
                if (schedule.getIntervalSeconds() != null) {
                    intervalLastRun.put(schedule.getId(), System.currentTimeMillis());
                }
                // Invalidate cache so next cycle is calculated
                nextRunCache.remove(schedule.getId());
                fired.clear();
                triggerSchedule(schedule, false);
            }
        }

        // Clean dead mobs every 10 ticks (not every tick) to save CPU
        if (tickCounter % 10 == 0) {
            cleanDeadMobs();
        }
    }

    private final java.util.concurrent.atomic.AtomicBoolean worldGuardBypassing = new java.util.concurrent.atomic.AtomicBoolean(false);

    public boolean isWorldGuardBypassing() {
        return worldGuardBypassing.get();
    }

    public void setWorldGuardBypassing(boolean bypassing) {
        this.worldGuardBypassing.set(bypassing);
    }

    public boolean triggerSchedule(MobSchedule schedule, boolean force) {
        if (schedule == null || (!schedule.isEnabled() && !force)) {
            return false;
        }

        // Stacking check
        if (schedule.isPreventStacking() && !force) {
            List<ActiveMobInstance> list = activeMobs.get(schedule.getId());
            if (list != null && list.stream().anyMatch(ActiveMobInstance::isAlive)) {
                plugin.getLogger().info("[CappySchedule] Skipped spawning '" + schedule.getId() + "' because previous instance is still alive.");
                return false;
            }
        }

        // Pick location
        SpawnLocation spawnLocDef = schedule.getRandomLocation();
        Location loc = null;
        if (spawnLocDef != null) {
            loc = spawnLocDef.toBukkitLocation();
        }

        if (loc == null && schedule.getProvider() != MobProvider.COMMAND) {
            plugin.getLogger().warning("[CappySchedule] Could not find valid spawn location for schedule: " + schedule.getId());
            return false;
        }

        // Spawn mob through hook with WorldGuard bypass
        boolean bypassWG = schedule.isIgnoreWorldGuard() || plugin.getPluginConfig().isIgnoreWorldGuard();
        if (bypassWG) {
            setWorldGuardBypassing(true);
        }

        Entity spawnedEntity = null;
        try {
            spawnedEntity = plugin.getHookManager().spawn(schedule, loc);
        } finally {
            if (bypassWG) {
                setWorldGuardBypassing(false);
            }
        }

        ActiveMobInstance instance = null;
        if (spawnedEntity != null) {
            instance = new ActiveMobInstance(schedule.getId(), spawnedEntity.getUniqueId(), loc);
            activeMobs.computeIfAbsent(schedule.getId(), k -> new ArrayList<>()).add(instance);
        }

        // Handle auto-despawn timer
        if (instance != null && schedule.getDespawnAfterSeconds() > 0) {
            final ActiveMobInstance finalInst = instance;
            BukkitTask despawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (finalInst.isAlive()) {
                    finalInst.remove();
                    handleMobDespawn(schedule, finalInst);
                }
            }, schedule.getDespawnAfterSeconds() * 20L);
            instance.setDespawnTask(despawnTask);
        }

        // Trigger on_spawn actions
        handleSpawnActions(schedule, loc);

        return true;
    }

    private void handleSpawnActions(MobSchedule schedule, Location loc) {
        SpawnAction action = schedule.getOnSpawn();
        if (action == null) return;

        Map<String, String> placeholders = buildPlaceholders(schedule, loc, null, 0);
        String prefix = plugin.getPluginConfig().getPrefix();

        // Fireworks
        if (action.isSpawnFireworks() && loc != null && loc.getWorld() != null) {
            try {
                Firework fw = loc.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withColor(Color.RED, Color.ORANGE, Color.YELLOW)
                        .withFade(Color.PURPLE)
                        .withTrail()
                        .build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            } catch (Exception ignored) {}
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (action.isBroadcastChat() && action.getChatMessage() != null) {
                player.sendMessage(applyPlaceholders(action.getChatMessage(), placeholders, prefix));
            }
            if (action.getTitle() != null || action.getSubtitle() != null) {
                String title = action.getTitle() != null ? applyPlaceholders(action.getTitle(), placeholders, prefix) : "";
                String sub = action.getSubtitle() != null ? applyPlaceholders(action.getSubtitle(), placeholders, prefix) : "";
                player.sendTitle(title, sub, 10, 60, 20);
            }
            if (action.getActionbar() != null) {
                try {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(applyPlaceholders(action.getActionbar(), placeholders, prefix)));
                } catch (Throwable ignored) {}
            }
            if (action.getSound() != null) {
                SoundUtil.playSound(player, action.getSound());
            }
        }

        // Commands
        if (action.getCommands() != null) {
            for (String cmd : action.getCommands()) {
                String formatted = applyPlaceholders(cmd, placeholders, "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            }
        }
    }

    public void handleMobKill(MobSchedule schedule, Entity killedEntity, Player killer) {
        if (schedule == null) return;

        // Remove from tracking
        List<ActiveMobInstance> list = activeMobs.get(schedule.getId());
        if (list != null) {
            list.removeIf(inst -> inst.getEntityUniqueId().equals(killedEntity.getUniqueId()));
        }

        KillAction action = schedule.getOnKill();
        if (action == null) return;

        Location loc = killedEntity.getLocation();
        Map<String, String> placeholders = buildPlaceholders(schedule, loc, killer, 0);
        String prefix = plugin.getPluginConfig().getPrefix();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (action.isBroadcastChat() && action.getChatMessage() != null) {
                player.sendMessage(applyPlaceholders(action.getChatMessage(), placeholders, prefix));
            }
            if (action.getTitle() != null || action.getSubtitle() != null) {
                String title = action.getTitle() != null ? applyPlaceholders(action.getTitle(), placeholders, prefix) : "";
                String sub = action.getSubtitle() != null ? applyPlaceholders(action.getSubtitle(), placeholders, prefix) : "";
                player.sendTitle(title, sub, 10, 60, 20);
            }
            if (action.getActionbar() != null) {
                try {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(applyPlaceholders(action.getActionbar(), placeholders, prefix)));
                } catch (Throwable ignored) {}
            }
            if (action.getSound() != null) {
                SoundUtil.playSound(player, action.getSound());
            }
        }

        // Execute reward commands
        if (action.getCommands() != null) {
            for (String cmd : action.getCommands()) {
                String formatted = applyPlaceholders(cmd, placeholders, "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            }
        }
    }

    public void handleMobDespawn(MobSchedule schedule, ActiveMobInstance instance) {
        if (schedule == null) return;

        List<ActiveMobInstance> list = activeMobs.get(schedule.getId());
        if (list != null) {
            list.remove(instance);
        }

        DespawnAction action = schedule.getOnDespawn();
        if (action == null) return;

        Location loc = instance.getSpawnLocation();
        Map<String, String> placeholders = buildPlaceholders(schedule, loc, null, 0);
        String prefix = plugin.getPluginConfig().getPrefix();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (action.isBroadcastChat() && action.getChatMessage() != null) {
                player.sendMessage(applyPlaceholders(action.getChatMessage(), placeholders, prefix));
            }
            if (action.getTitle() != null || action.getSubtitle() != null) {
                String title = action.getTitle() != null ? applyPlaceholders(action.getTitle(), placeholders, prefix) : "";
                String sub = action.getSubtitle() != null ? applyPlaceholders(action.getSubtitle(), placeholders, prefix) : "";
                player.sendTitle(title, sub, 10, 40, 10);
            }
            if (action.getSound() != null) {
                SoundUtil.playSound(player, action.getSound());
            }
        }

        if (action.getCommands() != null) {
            for (String cmd : action.getCommands()) {
                String formatted = applyPlaceholders(cmd, placeholders, "");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
            }
        }
    }

    private void fireWarning(MobSchedule schedule, WarningConfig warning, long secondsUntilRun) {
        SpawnLocation locDef = schedule.getRandomLocation();
        Location loc = locDef != null ? new Location(Bukkit.getWorld(locDef.getWorldName()), locDef.getX(), locDef.getY(), locDef.getZ()) : null;
        Map<String, String> placeholders = buildPlaceholders(schedule, loc, null, secondsUntilRun);
        String prefix = plugin.getPluginConfig().getPrefix();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (warning.isBroadcastChat() && warning.getChatMessage() != null) {
                player.sendMessage(applyPlaceholders(warning.getChatMessage(), placeholders, prefix));
            }
            if (warning.getTitle() != null || warning.getSubtitle() != null) {
                String title = warning.getTitle() != null ? applyPlaceholders(warning.getTitle(), placeholders, prefix) : "";
                String sub = warning.getSubtitle() != null ? applyPlaceholders(warning.getSubtitle(), placeholders, prefix) : "";
                player.sendTitle(title, sub, 10, 50, 15);
            }
            if (warning.getActionbar() != null) {
                try {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText(applyPlaceholders(warning.getActionbar(), placeholders, prefix)));
                } catch (Throwable ignored) {}
            }
            if (warning.getSound() != null) {
                SoundUtil.playSound(player, warning.getSound());
            }
        }
    }

    public ZonedDateTime calculateNextRun(MobSchedule schedule, ZonedDateTime fromTime) {
        if (schedule == null) return null;

        // 1. Interval-based schedule
        if (schedule.getIntervalSeconds() != null && schedule.getIntervalSeconds() > 0) {
            Long lastRun = intervalLastRun.get(schedule.getId());
            if (lastRun == null) {
                // Initialize
                intervalLastRun.put(schedule.getId(), fromTime.toInstant().toEpochMilli());
                return fromTime.plusSeconds(schedule.getIntervalSeconds());
            }
            Instant lastInstant = Instant.ofEpochMilli(lastRun);
            ZonedDateTime lastZdt = ZonedDateTime.ofInstant(lastInstant, fromTime.getZone());
            ZonedDateTime next = lastZdt.plusSeconds(schedule.getIntervalSeconds());
            if (next.isBefore(fromTime)) {
                return fromTime;
            }
            return next;
        }

        // 2. Fixed times & Days of week schedule
        if (schedule.getTimes().isEmpty()) {
            return null;
        }

        ZonedDateTime earliest = null;
        LocalDate today = fromTime.toLocalDate();

        // Search up to 8 days in the future to find next valid time & day
        for (int dayOffset = 0; dayOffset <= 8; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            DayOfWeek dow = date.getDayOfWeek();
            if (!schedule.getDays().contains(dow)) {
                continue;
            }

            for (LocalTime time : schedule.getTimes()) {
                ZonedDateTime candidate = ZonedDateTime.of(date, time, fromTime.getZone());
                // If today, candidate must be after or equal to fromTime minus 1 second
                if (candidate.isAfter(fromTime.minusSeconds(1))) {
                    if (earliest == null || candidate.isBefore(earliest)) {
                        earliest = candidate;
                    }
                }
            }
            if (earliest != null && !earliest.toLocalDate().isAfter(date)) {
                break;
            }
        }

        return earliest;
    }

    public MobSchedule getScheduleByEntity(Entity entity) {
        if (entity == null) return null;
        UUID uuid = entity.getUniqueId();
        for (Map.Entry<String, List<ActiveMobInstance>> entry : activeMobs.entrySet()) {
            for (ActiveMobInstance inst : entry.getValue()) {
                if (inst.getEntityUniqueId().equals(uuid)) {
                    return plugin.getPluginConfig().getSchedule(entry.getKey());
                }
            }
        }
        return null;
    }

    public int getAliveCount(String scheduleId) {
        List<ActiveMobInstance> list = activeMobs.get(scheduleId.toLowerCase());
        if (list == null) return 0;
        return (int) list.stream().filter(ActiveMobInstance::isAlive).count();
    }

    private void cleanDeadMobs() {
        for (List<ActiveMobInstance> list : activeMobs.values()) {
            list.removeIf(inst -> !inst.isAlive());
        }
    }

    private Map<String, String> buildPlaceholders(MobSchedule schedule, Location loc, Player player, long secondsLeft) {
        Map<String, String> map = new HashMap<>();
        map.put("mob_name", schedule.getDisplayName());
        map.put("mob_id", schedule.getMobId());
        map.put("schedule_id", schedule.getId());
        map.put("provider", schedule.getProvider().name());
        map.put("time_left", TimeParser.formatDuration(Math.max(0, secondsLeft)));
        map.put("seconds_left", String.valueOf(Math.max(0, secondsLeft)));

        if (loc != null) {
            map.put("world", loc.getWorld() != null ? loc.getWorld().getName() : "world");
            map.put("x", String.format("%.1f", loc.getX()));
            map.put("y", String.format("%.1f", loc.getY()));
            map.put("z", String.format("%.1f", loc.getZ()));
            map.put("location", String.format("%s (%.1f, %.1f, %.1f)",
                    loc.getWorld() != null ? loc.getWorld().getName() : "world",
                    loc.getX(), loc.getY(), loc.getZ()));
        } else {
            map.put("world", "world");
            map.put("x", "0");
            map.put("y", "0");
            map.put("z", "0");
            map.put("location", "Unknown");
        }

        if (player != null) {
            map.put("killer", player.getName());
            map.put("player", player.getName());
        } else {
            map.put("killer", "Không rõ");
            map.put("player", "");
        }

        return map;
    }

    private String applyPlaceholders(String text, Map<String, String> placeholders, String prefix) {
        if (text == null) return "";
        text = text.replace("{prefix}", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ColorUtil.colorize(text);
    }
}

