package com.cappy.schedule;

import com.cappy.schedule.command.ScheduleCommand;
import com.cappy.schedule.config.MessagesConfig;
import com.cappy.schedule.config.PluginConfig;
import com.cappy.schedule.hook.HookManager;
import com.cappy.schedule.listener.MobDeathListener;
import com.cappy.schedule.scheduler.ScheduleManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CappySchedulePlugin extends JavaPlugin {

    private static CappySchedulePlugin instance;

    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;
    private HookManager hookManager;
    private ScheduleManager scheduleManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load configs
        this.pluginConfig = new PluginConfig(this);
        this.messagesConfig = new MessagesConfig(this);

        // Initialize hooks and scheduler
        this.hookManager = new HookManager();
        this.scheduleManager = new ScheduleManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new MobDeathListener(this), this);

        // Register command
        PluginCommand cmd = getCommand("cappyschedule");
        if (cmd != null) {
            ScheduleCommand executor = new ScheduleCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Start scheduler loop
        scheduleManager.start();

        getLogger().info("=================================================");
        getLogger().info(" CappySchedule v" + getDescription().getVersion() + " has been ENABLED!");
        getLogger().info(" Configured Timezone: " + pluginConfig.getTimezone().getId());
        getLogger().info(" Active Schedules: " + pluginConfig.getSchedules().size());
        getLogger().info("=================================================");
    }

    @Override
    public void onDisable() {
        if (scheduleManager != null) {
            scheduleManager.stop();
        }

        getLogger().info("CappySchedule has been DISABLED!");
    }

    public void reload() {
        if (scheduleManager != null) {
            scheduleManager.stop();
        }
        pluginConfig.load();
        messagesConfig.load();
        if (scheduleManager != null) {
            scheduleManager.start();
        }
    }

    public static CappySchedulePlugin getInstance() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }
}

