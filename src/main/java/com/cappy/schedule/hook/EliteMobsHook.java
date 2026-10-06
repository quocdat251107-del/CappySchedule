package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobSchedule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EliteMobsHook implements MobHook {

    private final boolean available;

    public EliteMobsHook() {
        this.available = Bukkit.getPluginManager().getPlugin("EliteMobs") != null;
    }

    @Override
    public boolean isAvailable() {
        return available && Bukkit.getPluginManager().isPluginEnabled("EliteMobs");
    }

    @Override
    public Entity spawnMob(MobSchedule schedule, Location location) {
        if (!isAvailable() || location == null) {
            return null;
        }

        try {
            // EliteMobs command: /em spawnmob <filename/id> <world> <x> <y> <z> [level]
            int level = (int) Math.max(1, schedule.getLevel());
            String cmd = String.format("em spawnmob %s %s %f %f %f %d",
                    schedule.getMobId(),
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    level
            );
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[CappySchedule] Error spawning EliteMob: " + t.getMessage());
        }

        return null;
    }
}

