package com.cappy.schedule.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ThreadLocalRandom;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static Location getComputedLocation(Location baseLocation, double radius, boolean safeSpawn) {
        if (baseLocation == null || baseLocation.getWorld() == null) {
            return null;
        }

        World world = baseLocation.getWorld();
        double targetX = baseLocation.getX();
        double targetY = baseLocation.getY();
        double targetZ = baseLocation.getZ();

        if (radius > 0) {
            double angle = ThreadLocalRandom.current().nextDouble() * 2 * Math.PI;
            double distance = ThreadLocalRandom.current().nextDouble() * radius;
            targetX += Math.cos(angle) * distance;
            targetZ += Math.sin(angle) * distance;
        }

        Location target = new Location(world, targetX, targetY, targetZ, baseLocation.getYaw(), baseLocation.getPitch());

        if (safeSpawn) {
            return findSafeLocation(target);
        }

        return target;
    }

    public static Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return loc;

        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ)) {
            world.loadChunk(chunkX, chunkZ, true);
        }

        int x = loc.getBlockX();
        int initialY = loc.getBlockY();
        int z = loc.getBlockZ();

        // Search down up to 20 blocks or up 10 blocks for solid ground
        for (int y = initialY; y >= Math.max(world.getMinHeight(), initialY - 30); y--) {
            Block block = world.getBlockAt(x, y, z);
            Block above1 = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);

            if (block.getType().isSolid() && !block.isLiquid() 
                    && !above1.getType().isSolid() && !above2.getType().isSolid()
                    && !above1.isLiquid() && !above2.isLiquid()) {
                return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
            }
        }

        // If not found downwards, check upwards
        for (int y = initialY + 1; y <= Math.min(world.getMaxHeight() - 2, initialY + 20); y++) {
            Block block = world.getBlockAt(x, y, z);
            Block above1 = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);

            if (block.getType().isSolid() && !block.isLiquid() 
                    && !above1.getType().isSolid() && !above2.getType().isSolid()
                    && !above1.isLiquid() && !above2.isLiquid()) {
                return new Location(world, loc.getX(), y + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
            }
        }

        return loc;
    }
}

