package com.cappy.schedule.scheduler;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {

    private static final DateTimeFormatter TIME_FORMAT_SHORT = DateTimeFormatter.ofPattern("H:m");
    private static final DateTimeFormatter TIME_FORMAT_SHORT_SS = DateTimeFormatter.ofPattern("H:m:s");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(d|h|m|s)?", Pattern.CASE_INSENSITIVE);

    private TimeParser() {
    }

    public static LocalTime parseTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        str = str.trim();
        if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
            str = str.substring(1, str.length() - 1).trim();
        }

        try {
            String[] parts = str.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0].trim());
                int minute = Integer.parseInt(parts[1].trim());
                int second = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : 0;
                return LocalTime.of(hour, minute, second);
            }
        } catch (Exception ignored) {
        }

        try {
            return LocalTime.parse(str);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Parses duration string like "10m", "30s", "2h", "1d" into seconds.
     */
    public static long parseDurationSeconds(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        str = str.trim();

        Matcher matcher = DURATION_PATTERN.matcher(str);
        long totalSeconds = 0;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            long val = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            if (unit == null) unit = "s";

            switch (unit.toLowerCase()) {
                case "d":
                    totalSeconds += val * 86400;
                    break;
                case "h":
                    totalSeconds += val * 3600;
                    break;
                case "m":
                    totalSeconds += val * 60;
                    break;
                case "s":
                default:
                    totalSeconds += val;
                    break;
            }
        }

        return found ? totalSeconds : 0;
    }

    /**
     * Parses "every: 3h" or "every: 30m" into interval seconds.
     */
    public static Long parseIntervalSeconds(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        str = str.trim();
        if (str.toLowerCase().startsWith("every:")) {
            str = str.substring(6).trim();
        }
        long sec = parseDurationSeconds(str);
        return sec > 0 ? sec : null;
    }

    public static Set<DayOfWeek> parseDays(List<String> rawDays) {
        Set<DayOfWeek> days = new HashSet<>();
        if (rawDays == null || rawDays.isEmpty()) {
            return EnumSet.allOf(DayOfWeek.class);
        }

        for (String raw : rawDays) {
            if (raw == null) continue;
            String upper = raw.trim().toUpperCase();
            if (upper.equals("ALL") || upper.equals("*")) {
                return EnumSet.allOf(DayOfWeek.class);
            }
            if (upper.equals("WEEKEND")) {
                days.add(DayOfWeek.SATURDAY);
                days.add(DayOfWeek.SUNDAY);
                continue;
            }
            if (upper.equals("WEEKDAY") || upper.equals("WEEKDAYS")) {
                days.add(DayOfWeek.MONDAY);
                days.add(DayOfWeek.TUESDAY);
                days.add(DayOfWeek.WEDNESDAY);
                days.add(DayOfWeek.THURSDAY);
                days.add(DayOfWeek.FRIDAY);
                continue;
            }
            try {
                days.add(DayOfWeek.valueOf(upper));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return days.isEmpty() ? EnumSet.allOf(DayOfWeek.class) : days;
    }

    public static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSec = seconds % 60;
        if (minutes < 60) {
            return remainingSec > 0 ? String.format("%dm %ds", minutes, remainingSec) : String.format("%dm", minutes);
        }
        long hours = minutes / 60;
        long remainingMin = minutes % 60;
        return remainingMin > 0 ? String.format("%dh %dm", hours, remainingMin) : String.format("%dh", hours);
    }
}

