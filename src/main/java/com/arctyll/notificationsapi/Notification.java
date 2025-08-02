package com.arctyll.notificationsapi;

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
        this.title = title;
        this.message = message;
        this.maxWidth = maxWidth;
        this.duration = duration;
        this.position = position;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
        this.messageColor = messageColor;
        this.startTime = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public long getDuration() {
        return duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public Position getPosition() {
        return position;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getMessageColor() {
        return messageColor;
    }
}
