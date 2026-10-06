package com.cappy.schedule.listener;

import com.cappy.schedule.CappySchedulePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class SpawnProtectionBypassListener implements Listener {

    private final CappySchedulePlugin plugin;

    public SpawnProtectionBypassListener(CappySchedulePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() && plugin.getScheduleManager().isWorldGuardBypassing()) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.isCancelled() && plugin.getScheduleManager().isWorldGuardBypassing()) {
            event.setCancelled(false);
        }
    }
}

