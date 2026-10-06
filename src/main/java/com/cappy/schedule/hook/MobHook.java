package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobSchedule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface MobHook {
    /**
     * Attempts to spawn the mob defined in schedule at location.
     * Returns the spawned Entity if successful, or null if custom/non-entity.
     */
    Entity spawnMob(MobSchedule schedule, Location location);

    /**
     * Checks whether the underlying provider plugin is available.
     */
    boolean isAvailable();
}

