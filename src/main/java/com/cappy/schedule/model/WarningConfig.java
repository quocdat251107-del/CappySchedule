package com.cappy.schedule.model;

public class WarningConfig {
    private final long secondsBefore;
    private final boolean broadcastChat;
    private final String chatMessage;
    private final String title;
    private final String subtitle;
    private final String actionbar;
    private final String sound;

    public WarningConfig(long secondsBefore, boolean broadcastChat, String chatMessage,
                         String title, String subtitle, String actionbar, String sound) {
        this.secondsBefore = secondsBefore;
        this.broadcastChat = broadcastChat;
        this.chatMessage = chatMessage;
        this.title = title;
        this.subtitle = subtitle;
        this.actionbar = actionbar;
        this.sound = sound;
    }

    public long getSecondsBefore() {
        return secondsBefore;
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
}

