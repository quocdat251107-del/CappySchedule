package com.cappy.schedule.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MobSchedule {
    private final String id;
    private final boolean enabled;
    private final String displayName;
    private final MobProvider provider;
    private final String mobId;
    private final double level;
    private final List<LocalTime> times;
    private final Set<DayOfWeek> days;
    private final Long intervalSeconds;
    private final List<SpawnLocation> locations;
    private final boolean preventStacking;
    private final long despawnAfterSeconds;
    private final List<WarningConfig> warnings;
    private final SpawnAction onSpawn;
    private final KillAction onKill;
    private final DespawnAction onDespawn;

    public MobSchedule(String id, boolean enabled, String displayName, MobProvider provider,
                       String mobId, double level, List<LocalTime> times, Set<DayOfWeek> days,
                       Long intervalSeconds, List<SpawnLocation> locations, boolean preventStacking,
                       long despawnAfterSeconds, List<WarningConfig> warnings,
                       SpawnAction onSpawn, KillAction onKill, DespawnAction onDespawn) {
        this.id = id;
        this.enabled = enabled;
        this.displayName = displayName;
        this.provider = provider;
        this.mobId = mobId;
        this.level = level;
        this.times = times != null ? times : Collections.emptyList();
        this.days = days != null ? days : Collections.emptySet();
        this.intervalSeconds = intervalSeconds;
        this.locations = locations != null ? locations : Collections.emptyList();
        this.preventStacking = preventStacking;
        this.despawnAfterSeconds = despawnAfterSeconds;
        this.warnings = warnings != null ? warnings : Collections.emptyList();
        this.onSpawn = onSpawn;
        this.onKill = onKill;
        this.onDespawn = onDespawn;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MobProvider getProvider() {
        return provider;
    }

    public String getMobId() {
        return mobId;
    }

    public double getLevel() {
        return level;
    }

    public List<LocalTime> getTimes() {
        return times;
    }

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public Long getIntervalSeconds() {
        return intervalSeconds;
    }

    public List<SpawnLocation> getLocations() {
        return locations;
    }

    public SpawnLocation getRandomLocation() {
        if (locations.isEmpty()) return null;
        int index = ThreadLocalRandom.current().nextInt(locations.size());
        return locations.get(index);
    }

    public boolean isPreventStacking() {
        return preventStacking;
    }

    public long getDespawnAfterSeconds() {
        return despawnAfterSeconds;
    }

    public List<WarningConfig> getWarnings() {
        return warnings;
    }

    public SpawnAction getOnSpawn() {
        return onSpawn;
    }

    public KillAction getOnKill() {
        return onKill;
    }

    public DespawnAction getOnDespawn() {
        return onDespawn;
    }
}

