package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobSchedule;
import com.cappy.schedule.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class VanillaMobHook implements MobHook {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Entity spawnMob(MobSchedule schedule, Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        try {
            EntityType type = EntityType.valueOf(schedule.getMobId().trim().toUpperCase());
            Entity entity = location.getWorld().spawnEntity(location, type);
            if (entity instanceof LivingEntity living) {
                if (schedule.getDisplayName() != null && !schedule.getDisplayName().isEmpty()) {
                    living.setCustomName(ColorUtil.colorize(schedule.getDisplayName()));
                    living.setCustomNameVisible(true);
                }
                living.setRemoveWhenFarAway(false);
            }
            return entity;
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[CappySchedule] Invalid vanilla EntityType: '" + schedule.getMobId() + "'");
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[CappySchedule] Error spawning vanilla mob: " + t.getMessage());
        }

        return null;
    }
}

