package com.cappy.schedule.hook;

import com.cappy.schedule.model.MobProvider;
import com.cappy.schedule.model.MobSchedule;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.EnumMap;
import java.util.Map;

public class HookManager {

    private final Map<MobProvider, MobHook> hooks = new EnumMap<>(MobProvider.class);

    public HookManager() {
        hooks.put(MobProvider.MYTHICMOBS, new MythicMobsHook());
        hooks.put(MobProvider.ELITEMOBS, new EliteMobsHook());
        hooks.put(MobProvider.VANILLA, new VanillaMobHook());
        hooks.put(MobProvider.COMMAND, new CommandMobHook());
    }

    public Entity spawn(MobSchedule schedule, Location location) {
        MobHook hook = hooks.get(schedule.getProvider());
        if (hook != null) {
            return hook.spawnMob(schedule, location);
        }
        return null;
    }

    public boolean isProviderAvailable(MobProvider provider) {
        MobHook hook = hooks.get(provider);
        return hook != null && hook.isAvailable();
    }
}

