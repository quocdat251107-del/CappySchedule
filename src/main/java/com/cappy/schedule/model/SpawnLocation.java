package com.cappy.schedule.model;

import com.cappy.schedule.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnLocation {
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final double radius;
    private final boolean safeSpawn;

    public SpawnLocation(String worldName, double x, double y, double z, float yaw, float pitch, double radius, boolean safeSpawn) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.radius = radius;
        this.safeSpawn = safeSpawn;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isSafeSpawn() {
        return safeSpawn;
    }

    public Location toBukkitLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        Location base = new Location(world, x, y, z, yaw, pitch);
        return LocationUtil.getComputedLocation(base, radius, safeSpawn);
    }
}

