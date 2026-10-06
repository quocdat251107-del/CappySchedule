package com.cappy.schedule.model;

public enum MobProvider {
    MYTHICMOBS,
    ELITEMOBS,
    VANILLA,
    COMMAND;

    public static MobProvider fromString(String str) {
        if (str == null) return MYTHICMOBS;
        try {
            return MobProvider.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MYTHICMOBS;
        }
    }
}

