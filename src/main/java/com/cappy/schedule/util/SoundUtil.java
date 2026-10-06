package com.cappy.schedule.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {
    }

    /**
     * Plays sound from string format "SOUND_NAME:volume:pitch" or just "SOUND_NAME"
     */
    public static void playSound(Player player, String soundString) {
        if (soundString == null || soundString.trim().isEmpty() || player == null) {
            return;
        }

        try {
            String[] parts = soundString.split(":");
            String soundName = parts[0].trim().toUpperCase();
            float volume = 1.0f;
            float pitch = 1.0f;

            if (parts.length > 1) {
                volume = Float.parseFloat(parts[1].trim());
            }
            if (parts.length > 2) {
                pitch = Float.parseFloat(parts[2].trim());
            }

            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
            // Sound enum constant might not exist on older/newer versions
        }
    }

    public static void playSoundAtLocation(Location loc, String soundString) {
        if (soundString == null || soundString.trim().isEmpty() || loc == null || loc.getWorld() == null) {
            return;
        }

        try {
            String[] parts = soundString.split(":");
            String soundName = parts[0].trim().toUpperCase();
            float volume = 1.0f;
            float pitch = 1.0f;

            if (parts.length > 1) {
                volume = Float.parseFloat(parts[1].trim());
            }
            if (parts.length > 2) {
                pitch = Float.parseFloat(parts[2].trim());
            }

            Sound sound = Sound.valueOf(soundName);
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }
}

