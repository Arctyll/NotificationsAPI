package com.arctyll.notificationsapi;

import com.arctyll.notificationsapi.util.RenderUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

@Mod(modid = "notificationsapi", name = "NotificationsAPI", version = "1.0", acceptedMinecraftVersions = "[1.8.9]")
public class NotificationsAPI {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        RenderUtils.initNanoVG();
        loadFontFromAssets("assets/notificationsapi/fonts/Outfit.ttf");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void loadFontFromAssets(String path) {
        try {
			try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
				if (in == null) throw new IOException("Font not found: " + path);
				ByteBuffer buffer = ByteBuffer.allocateDirect(in.available());
				Channels.newChannel(in).read(buffer);
				buffer.flip();
				RenderUtils.loadDefaultFont(buffer);
			}
		} catch (Exception e) {
            System.err.println("Failed to load font: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            NotificationManager.render();
        }
    }
	
    public static void send(String title, String message) {
        send(title, message, 250, 4000, Position.TOP_RIGHT, 0xCC222222, 0xFFFFFFFF, 0xFFDDDDDD);
    }

    public static void send(String title, String message, int maxWidth) {
        send(title, message, maxWidth, 4000, Position.TOP_RIGHT, 0xCC222222, 0xFFFFFFFF, 0xFFDDDDDD);
    }

    public static void send(String title, String message, int maxWidth, long duration) {
        send(title, message, maxWidth, duration, Position.TOP_RIGHT, 0xCC222222, 0xFFFFFFFF, 0xFFDDDDDD);
    }

    public static void send(String title, String message, int maxWidth, long duration, Position position) {
        send(title, message, maxWidth, duration, position, 0xCC222222, 0xFFFFFFFF, 0xFFDDDDDD);
    }

    public static void send(String title, String message, int maxWidth, long duration, Position position, int bgColor, int titleColor, int msgColor) {
        if (title == null || message == null || duration <= 0) return;

        Notification n = new Notification(
            title,
            message,
            Math.max(150, maxWidth),
            duration,
            position != null ? position : Position.TOP_RIGHT,
            bgColor,
            titleColor,
            msgColor
        );

        NotificationManager.addNotification(n);
    }
}
