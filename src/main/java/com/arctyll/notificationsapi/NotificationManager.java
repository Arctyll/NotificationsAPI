package com.arctyll.notificationsapi;

import com.arctyll.notificationsapi.util.RenderUtils;
import com.arctyll.notificationsapi.util.TimeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;

public class NotificationManager {
    private static final List<Notification> notifications = new ArrayList<>();

    public static void addNotification(Notification notification) {
        if (notification == null) return;
        notifications.add(notification);
    }

    public static void render() {
		ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		int scaledWidth = res.getScaledWidth();
		int scaledHeight = res.getScaledHeight();
		
        RenderUtils.beginFrame(scaledWidth, scaledHeight);

        int spacing = 8;
        int padding = 10;
        int cornerRadius = 8;
        int fontSizeTitle = 18;
        int fontSizeMessage = 14;

        for (Position position : Position.values()) {
            List<Notification> list = getNotificationsAt(position);

            for (int i = 0; i < list.size(); i++) {
                Notification n = list.get(i);
                if (n == null) continue;

                float progress = TimeUtils.getProgress(n.getStartTime(), n.getDuration());
                if (progress >= 1f) continue;

                float alpha = TimeUtils.getAlpha(n.getStartTime(), n.getDuration(), 200);
                float ease = TimeUtils.easeInOut(progress);

                float maxWidth = Math.max(n.getMaxWidth(), 150);

                float[] messageBounds = RenderUtils.measureWrappedText(n.getMessage(), maxWidth - 2 * padding, fontSizeMessage);
                float messageHeight = messageBounds[3] - messageBounds[1];
                float height = padding * 2 + fontSizeTitle + 4 + messageHeight;

                float slideOffset = 20 * (1 - ease);
                float offsetX, offsetY;

                switch (n.getPosition() != null ? n.getPosition() : Position.TOP_RIGHT) {
                    case TOP_LEFT:
                        offsetX = 10 + slideOffset;
                        offsetY = 10 + getOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth);
                        break;
                    case TOP_RIGHT:
                        offsetX = scaledWidth - maxWidth - 10 - slideOffset;
                        offsetY = 10 + getOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth);
                        break;
                    case BOTTOM_LEFT:
                        offsetX = 10 + slideOffset;
                        offsetY = scaledHeight - getBottomOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth);
                        break;
                    case BOTTOM_RIGHT:
                    default:
                        offsetX = scaledWidth - maxWidth - 10 - slideOffset;
                        offsetY = scaledHeight - getBottomOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth);
                        break;
                }

                int bg = applyAlpha(n.getBackgroundColor(), alpha);
                int tc = applyAlpha(n.getTitleColor(), alpha);
                int mc = applyAlpha(n.getMessageColor(), alpha);

                RenderUtils.drawRoundedRect(offsetX, offsetY, maxWidth, height, cornerRadius, bg);
                RenderUtils.drawText(offsetX + padding, offsetY + padding, n.getTitle(), fontSizeTitle, tc);
                RenderUtils.drawWrappedText(offsetX + padding, offsetY + padding + fontSizeTitle + 4, n.getMessage(), maxWidth - 2 * padding, fontSizeMessage, mc);
            }
        }

        RenderUtils.endFrame();
        removeExpired();
    }

    private static float getOffsetY(int index, List<Notification> list, int spacing, int padding, int fontSizeTitle, int fontSizeMessage, float maxWidth) {
        float y = 0;
        for (int i = 0; i < index; i++) {
            Notification n = list.get(i);
            float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), maxWidth - 2 * padding, fontSizeMessage);
            float messageHeight = bounds[3] - bounds[1];
            float height = padding * 2 + fontSizeTitle + 4 + messageHeight;
            y += height + spacing;
        }
        return y;
    }

    private static float getBottomOffsetY(int index, List<Notification> list, int spacing, int padding, int fontSizeTitle, int fontSizeMessage, float maxWidth) {
        float y = 0;
        for (int i = list.size() - 1; i > index; i--) {
            Notification n = list.get(i);
            float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), maxWidth - 2 * padding, fontSizeMessage);
            float messageHeight = bounds[3] - bounds[1];
            float height = padding * 2 + fontSizeTitle + 4 + messageHeight;
            y += height + spacing;
        }
        Notification n = list.get(index);
        float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), maxWidth - 2 * padding, fontSizeMessage);
        float messageHeight = bounds[3] - bounds[1];
        float height = padding * 2 + fontSizeTitle + 4 + messageHeight;
        return y + height;
    }

    private static List<Notification> getNotificationsAt(Position position) {
        List<Notification> list = new ArrayList<>();
        for (Notification n : notifications) {
            if ((n.getPosition() != null ? n.getPosition() : Position.TOP_RIGHT) == position) {
                list.add(n);
            }
        }
        return list;
    }

    private static void removeExpired() {
        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            if (System.currentTimeMillis() - n.getStartTime() > n.getDuration()) {
                it.remove();
            }
        }
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
