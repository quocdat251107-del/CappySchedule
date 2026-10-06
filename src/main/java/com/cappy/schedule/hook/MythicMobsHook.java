package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobSchedule;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class MythicMobsHook implements MobHook {

    private final boolean available;

    public MythicMobsHook() {
        this.available = Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }

    @Override
    public boolean isAvailable() {
        return available && Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
    }

    @Override
    public Entity spawnMob(MobSchedule schedule, Location location) {
        if (!isAvailable() || location == null) {
            return null;
        }

        try {
            MythicBukkit mythic = MythicBukkit.inst();
            if (mythic == null) return null;

            MythicMob mob = mythic.getMobManager().getMythicMob(schedule.getMobId()).orElse(null);
            if (mob == null) {
                Bukkit.getLogger().warning("[CappySchedule] MythicMob '" + schedule.getMobId() + "' not found in MythicMobs database!");
                return null;
            }

            ActiveMob activeMob = mob.spawn(BukkitAdapter.adapt(location), Math.max(1, schedule.getLevel()));
            if (activeMob != null && activeMob.getEntity() != null) {
                return Bukkit.getEntity(activeMob.getEntity().getUniqueId());
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[CappySchedule] Error spawning MythicMob '" + schedule.getMobId() + "': " + t.getMessage());
            // Fallback command execution
            try {
                String cmd = String.format("mm mobs spawn %s 1 %s,%f,%f,%f,%f,%f",
                        schedule.getMobId(),
                        location.getWorld().getName(),
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch()
                );
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } catch (Exception ex) {
                Bukkit.getLogger().warning("[CappySchedule] Fallback spawn command failed: " + ex.getMessage());
            }
        }

        return null;
    }
}

