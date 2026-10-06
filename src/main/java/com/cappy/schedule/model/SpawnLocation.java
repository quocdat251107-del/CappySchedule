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
        if (world == null) {
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().equalsIgnoreCase(worldName)) {
                    world = w;
                    break;
                }
            }
        }

        if (world == null) {
            try {
                world = Bukkit.createWorld(new org.bukkit.WorldCreator(worldName));
            } catch (Exception ignored) {
            }
        }

        if (world == null) {
            Bukkit.getLogger().warning("[CappySchedule] World '" + worldName + "' could not be found or loaded!");
            return null;
        }

        int chunkX = ((int) x) >> 4;
        int chunkZ = ((int) z) >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.loadChunk(chunkX, chunkZ, true);
        }

        Location base = new Location(world, x, y, z, yaw, pitch);
        return LocationUtil.getComputedLocation(base, radius, safeSpawn);
    }
}

