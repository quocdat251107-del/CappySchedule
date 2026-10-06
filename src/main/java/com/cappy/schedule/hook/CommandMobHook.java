package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobSchedule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CommandMobHook implements MobHook {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Entity spawnMob(MobSchedule schedule, Location location) {
        // Handled directly via schedule.getOnSpawn().getCommands() or schedule.getMobId()
        if (schedule.getMobId() != null && !schedule.getMobId().trim().isEmpty()) {
            String cmd = schedule.getMobId();
            if (location != null) {
                cmd = cmd.replace("{world}", location.getWorld() != null ? location.getWorld().getName() : "")
                         .replace("{x}", String.format("%.2f", location.getX()))
                         .replace("{y}", String.format("%.2f", location.getY()))
                         .replace("{z}", String.format("%.2f", location.getZ()));
            }
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[CappySchedule] Error executing command mob hook: " + e.getMessage());
            }
        }
        return null;
    }
}

