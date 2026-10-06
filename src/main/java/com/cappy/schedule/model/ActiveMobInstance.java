package com.cappy.schedule.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ActiveMobInstance {
    private final String scheduleId;
    private final UUID entityUniqueId;
    private final Location spawnLocation;
    private final long spawnTimeMillis;
    private BukkitTask despawnTask;

    public ActiveMobInstance(String scheduleId, UUID entityUniqueId, Location spawnLocation) {
        this.scheduleId = scheduleId;
        this.entityUniqueId = entityUniqueId;
        this.spawnLocation = spawnLocation;
        this.spawnTimeMillis = System.currentTimeMillis();
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public UUID getEntityUniqueId() {
        return entityUniqueId;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public long getSpawnTimeMillis() {
        return spawnTimeMillis;
    }

    public BukkitTask getDespawnTask() {
        return despawnTask;
    }

    public void setDespawnTask(BukkitTask despawnTask) {
        this.despawnTask = despawnTask;
    }

    public boolean isAlive() {
        if (entityUniqueId == null) return false;
        Entity entity = Bukkit.getEntity(entityUniqueId);
        return entity != null && !entity.isDead() && entity.isValid();
    }

    public void remove() {
        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }
        if (entityUniqueId != null) {
            Entity entity = Bukkit.getEntity(entityUniqueId);
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
    }
}

