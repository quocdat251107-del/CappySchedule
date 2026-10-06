package com.cappy.schedule.listener;

import com.cappy.schedule.CappySchedulePlugin;
import com.cappy.schedule.model.MobSchedule;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class MobDeathListener implements Listener {

    private final CappySchedulePlugin plugin;

    public MobDeathListener(CappySchedulePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        MobSchedule schedule = plugin.getScheduleManager().getScheduleByEntity(entity);
        if (schedule != null) {
            Player killer = event.getEntity().getKiller();
            plugin.getScheduleManager().handleMobKill(schedule, entity, killer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (event.getEntity() == null) return;
        UUID entityUuid = event.getEntity().getUniqueId();
        if (entityUuid == null) return;

        Entity entity = Bukkit.getEntity(entityUuid);
        if (entity == null) return;

        MobSchedule schedule = plugin.getScheduleManager().getScheduleByEntity(entity);
        if (schedule != null) {
            Player killer = null;
            if (event.getKiller() != null && event.getKiller().getUniqueId() != null) {
                killer = Bukkit.getPlayer(event.getKiller().getUniqueId());
            }
            plugin.getScheduleManager().handleMobKill(schedule, entity, killer);
        }
    }
}


