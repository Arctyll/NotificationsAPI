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
        int padding = 20;
        int cornerRadius = 10;
        int fontSizeTitle = 22;
        int fontSizeMessage = 16;
        int iconSize = 40;
        int iconGap = 12;

        for (Position position : Position.values()) {
            List<Notification> list = getNotificationsAt(position);

            for (int i = 0; i < list.size(); i++) {
                Notification n = list.get(i);
                if (n == null) continue;

                float progress = TimeUtils.getProgress(n.getStartTime(), n.getDuration());
                if (progress >= 1f) continue;

                float alpha = TimeUtils.getAlpha(n.getStartTime(), n.getDuration(), 200);
                float ease = TimeUtils.easeInOut(progress);

                boolean hasIcon = n.getIcon() != null;
                float maxWidth = Math.max(n.getMaxWidth(), 150);

                float textAreaWidth = maxWidth - 2 * padding;
                if (hasIcon) {
                    textAreaWidth -= (iconSize + iconGap);
                }

                float[] messageBounds = RenderUtils.measureWrappedText(n.getMessage(), textAreaWidth, fontSizeMessage);
                float messageHeight = messageBounds[3] - messageBounds[1];

                float headerHeight = hasIcon ? Math.max(iconSize, fontSizeTitle) : fontSizeTitle;
                float height = padding * 2 + headerHeight + 10 + messageHeight; // 10px gap between title and message

                float slideOffset = 20 * (1 - ease);
                float offsetX, offsetY;

                switch (n.getPosition() != null ? n.getPosition() : Position.TOP_RIGHT) {
                    case TOP_LEFT:
                        offsetX = 10 + slideOffset;
                        offsetY = 10 + getOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth, iconSize, iconGap);
                        break;
                    case TOP_RIGHT:
                        offsetX = scaledWidth - maxWidth - 10 - slideOffset;
                        offsetY = 10 + getOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth, iconSize, iconGap);
                        break;
                    case BOTTOM_LEFT:
                        offsetX = 10 + slideOffset;
                        offsetY = scaledHeight - getBottomOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth, iconSize, iconGap);
                        break;
                    case BOTTOM_RIGHT:
                    default:
                        offsetX = scaledWidth - maxWidth - 10 - slideOffset;
                        offsetY = scaledHeight - getBottomOffsetY(i, list, spacing, padding, fontSizeTitle, fontSizeMessage, maxWidth, iconSize, iconGap);
                        break;
                }

                int bg = applyAlpha(n.getBackgroundColor(), alpha);
                int tc = applyAlpha(n.getTitleColor(), alpha);
                int mc = applyAlpha(n.getMessageColor(), alpha);

                RenderUtils.drawRoundedRect(offsetX, offsetY, maxWidth, height, cornerRadius, bg);

                float contentX = offsetX + padding;
                float contentY = offsetY + padding;

                if (hasIcon) {
                    int iconColor = applyAlpha(0xFFCCCCCC, alpha); // Light gray icon color to match HTML
                    RenderUtils.drawIcon(n.getIcon(), contentX, contentY, iconSize, iconColor);

                    float titleX = contentX + iconSize + iconGap;
                    float titleY = contentY + (iconSize - fontSizeTitle) / 2f; // Center title vertically with icon
                    RenderUtils.drawText(titleX, titleY, n.getTitle(), fontSizeTitle, tc);

                    float messageY = contentY + headerHeight + 10; // 10px gap
                    RenderUtils.drawWrappedText(contentX, messageY, n.getMessage(), textAreaWidth, fontSizeMessage, mc);
                } else {
                    RenderUtils.drawText(contentX, contentY, n.getTitle(), fontSizeTitle, tc);

                    float messageY = contentY + fontSizeTitle + 10; // 10px gap
                    RenderUtils.drawWrappedText(contentX, messageY, n.getMessage(), textAreaWidth, fontSizeMessage, mc);
                }
            }
        }

        RenderUtils.endFrame();
        removeExpired();
    }

    private static float getOffsetY(int index, List<Notification> list, int spacing, int padding, int fontSizeTitle, int fontSizeMessage, float maxWidth, int iconSize, int iconGap) {
        float y = 0;
        for (int i = 0; i < index; i++) {
            Notification n = list.get(i);
            boolean hasIcon = n.getIcon() != null;

            float textAreaWidth = maxWidth - 2 * padding;
            if (hasIcon) {
                textAreaWidth -= (iconSize + iconGap);
            }

            float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), textAreaWidth, fontSizeMessage);
            float messageHeight = bounds[3] - bounds[1];

            float headerHeight = hasIcon ? Math.max(iconSize, fontSizeTitle) : fontSizeTitle;
            float height = padding * 2 + headerHeight + 10 + messageHeight;
            y += height + spacing;
        }
        return y;
    }

    private static float getBottomOffsetY(int index, List<Notification> list, int spacing, int padding, int fontSizeTitle, int fontSizeMessage, float maxWidth, int iconSize, int iconGap) {
        float y = 0;
        for (int i = list.size() - 1; i > index; i--) {
            Notification n = list.get(i);
            boolean hasIcon = n.getIcon() != null;

            float textAreaWidth = maxWidth - 2 * padding;
            if (hasIcon) {
                textAreaWidth -= (iconSize + iconGap);
            }

            float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), textAreaWidth, fontSizeMessage);
            float messageHeight = bounds[3] - bounds[1];

            float headerHeight = hasIcon ? Math.max(iconSize, fontSizeTitle) : fontSizeTitle;
            float height = padding * 2 + headerHeight + 10 + messageHeight;
            y += height + spacing;
        }

        Notification n = list.get(index);
        boolean hasIcon = n.getIcon() != null;

        float textAreaWidth = maxWidth - 2 * padding;
        if (hasIcon) {
            textAreaWidth -= (iconSize + iconGap);
        }

        float[] bounds = RenderUtils.measureWrappedText(n.getMessage(), textAreaWidth, fontSizeMessage);
        float messageHeight = bounds[3] - bounds[1];

        float headerHeight = hasIcon ? Math.max(iconSize, fontSizeTitle) : fontSizeTitle;
        float height = padding * 2 + headerHeight + 10 + messageHeight;
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
