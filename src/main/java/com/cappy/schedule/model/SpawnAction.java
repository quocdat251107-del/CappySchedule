package com.cappy.schedule.model;

import java.util.List;

public class SpawnAction {
    private final boolean broadcastChat;
    private final String chatMessage;
    private final String title;
    private final String subtitle;
    private final String actionbar;
    private final String sound;
    private final boolean spawnFireworks;
    private final List<String> commands;

    public SpawnAction(boolean broadcastChat, String chatMessage, String title, String subtitle,
                       String actionbar, String sound, boolean spawnFireworks, List<String> commands) {
        this.broadcastChat = broadcastChat;
        this.chatMessage = chatMessage;
        this.title = title;
        this.subtitle = subtitle;
        this.actionbar = actionbar;
        this.sound = sound;
        this.spawnFireworks = spawnFireworks;
        this.commands = commands;
    }

    public boolean isBroadcastChat() {
        return broadcastChat;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getActionbar() {
        return actionbar;
    }

    public String getSound() {
        return sound;
    }

    public boolean isSpawnFireworks() {
        return spawnFireworks;
    }

    public List<String> getCommands() {
        return commands;
    }
}

