package com.arctyll.notificationsapi;

import net.minecraft.util.ResourceLocation;

public class Notification {
    private final String title;
    private final String message;
    private final int maxWidth;
    private final long duration;
    private final long startTime;
    private final Position position;
    private final int backgroundColor;
    private final int titleColor;
    private final int messageColor;
    private final ResourceLocation icon;

    public Notification(
        String title,
        String message,
        int maxWidth,
        long duration,
        Position position,
        int backgroundColor,
        int titleColor,
        int messageColor,
        ResourceLocation icon
    ) {
        this.title = title;
        this.message = message;
        this.maxWidth = maxWidth;
        this.duration = duration;
        this.position = position;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.messageColor = messageColor;
        this.icon = icon;
        this.startTime = System.currentTimeMillis();
    }

    public Notification(
        String title,
        String message,
        int maxWidth,
        long duration,
        Position position,
        int backgroundColor,
        int titleColor,
        int messageColor
    ) {
        this(title, message, maxWidth, duration, position, backgroundColor, titleColor, messageColor, null);
    }

    public Notification(
        String title,
        String message,
        long duration,
        Position position
    ) {
        this(title, message, 300, duration, position, 0xFF2A2A2E, 0xFFFFFFFF, 0xFFDDDDDD, null);
    }

    public Notification(
        String title,
        String message,
        long duration,
        Position position,
        ResourceLocation icon
    ) {
        this(title, message, 300, duration, position, 0xFF2A2A2E, 0xFFFFFFFF, 0xFFDDDDDD, icon);
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public int getMaxWidth() { return maxWidth; }
    public long getDuration() { return duration; }
    public long getStartTime() { return startTime; }
    public Position getPosition() { return position; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getTitleColor() { return titleColor; }
    public int getMessageColor() { return messageColor; }
    public ResourceLocation getIcon() { return icon; }
    public boolean hasIcon() { return icon != null; }
}
