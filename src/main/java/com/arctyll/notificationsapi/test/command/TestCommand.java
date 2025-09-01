package com.arctyll.notificationsapi.test.command;

import com.arctyll.notificationsapi.NotificationsAPI;
import com.arctyll.notificationsapi.Position;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class TestCommand extends CommandBase {

    private static final ResourceLocation API_ICON = new ResourceLocation("notificationsapi", "textures/icon.png");
    private static List<ScheduledNotification> scheduledNotifications = new ArrayList<>();
    private static boolean isRegistered = false;

    private static class ScheduledNotification {
        final Runnable task;
        final int delay;
        int ticksRemaining;

        ScheduledNotification(Runnable task, int delay) {
            this.task = task;
            this.delay = delay;
            this.ticksRemaining = delay;
        }
    }

    @Override
    public String getCommandName() {
        return "testnotif";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/testnotif <basic|icon|positions|colors|long>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (!isRegistered) {
            MinecraftForge.EVENT_BUS.register(this);
            isRegistered = true;
        }

        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }

        String testType = args[0].toLowerCase();

        switch (testType) {
            case "basic":
                testBasicNotifications();
                sender.addChatMessage(new ChatComponentText("Testing basic notifications..."));
                break;

            case "icon":
                testIconNotifications();
                sender.addChatMessage(new ChatComponentText("Testing icon notifications..."));
                break;

            case "positions":
                testPositionNotifications();
                sender.addChatMessage(new ChatComponentText("Testing position notifications..."));
                break;

            case "colors":
                testColorNotifications();
                sender.addChatMessage(new ChatComponentText("Testing color notifications..."));
                break;

            case "long":
                testLongNotifications();
                sender.addChatMessage(new ChatComponentText("Testing long text notifications..."));
                break;

            default:
                sender.addChatMessage(new ChatComponentText("Unknown test type. Available: basic, icon, positions, colors, long"));
                break;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (int i = scheduledNotifications.size() - 1; i >= 0; i--) {
            ScheduledNotification scheduled = scheduledNotifications.get(i);
            scheduled.ticksRemaining--;

            if (scheduled.ticksRemaining <= 0) {
                scheduled.task.run();
                scheduledNotifications.remove(i);
            }
        }
    }

    private static void scheduleNotification(Runnable task, int delayTicks) {
        scheduledNotifications.add(new ScheduledNotification(task, delayTicks));
    }

    private void testBasicNotifications() {
        NotificationsAPI.send("Basic Test", "This is a simple notification without an icon.");

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Another Basic", "Testing multiple notifications stacking.");
				}
			}, 10);
    }

    private void testIconNotifications() {
        NotificationsAPI.send("With Icon", "This notification includes the API icon!", API_ICON);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Icon Test 2", "Another notification with icon for comparison.", API_ICON);
				}
			}, 10);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Mixed Test", "This one has no icon for comparison.");
				}
			}, 20);
    }

    private void testPositionNotifications() {
        NotificationsAPI.send("Top Left", "Notification in top-left corner", 250, 3000, Position.TOP_LEFT, API_ICON);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Top Right", "Notification in top-right corner", 250, 3000, Position.TOP_RIGHT, API_ICON);
				}
			}, 4);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Bottom Left", "Notification in bottom-left corner", 250, 3000, Position.BOTTOM_LEFT, API_ICON);
				}
			}, 8);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Bottom Right", "Notification in bottom-right corner", 250, 3000, Position.BOTTOM_RIGHT, API_ICON);
				}
			}, 12);
    }

    private void testColorNotifications() {
        NotificationsAPI.send("Success", "Operation completed successfully!", 300, 4000, Position.TOP_RIGHT,
							  0xFF2D5A2D, 0xFF90EE90, 0xFFE0FFE0, API_ICON);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Warning", "This is a warning message.", 300, 4000, Position.TOP_RIGHT,
										  0xFF5A4A2D, 0xFFFFD700, 0xFFFFF8DC, API_ICON);
				}
			}, 10);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Error", "Something went wrong!", 300, 4000, Position.TOP_RIGHT,
										  0xFF5A2D2D, 0xFFFF6B6B, 0xFFFFE0E0, API_ICON);
				}
			}, 20);
    }

    private void testLongNotifications() {
        NotificationsAPI.send("Long Text Test",
							  "This is a very long notification message that should wrap to multiple lines. " +
							  "It demonstrates how the notification system handles longer content with proper text wrapping. " +
							  "The icon should remain properly aligned with the title while the message flows below.",
							  400, 6000, Position.TOP_RIGHT, API_ICON);

        scheduleNotification(new Runnable() {
				@Override
				public void run() {
					NotificationsAPI.send("Short Title",
										  "Another long message to test stacking behavior with wrapped text. " +
										  "This should appear below the first notification and demonstrate proper spacing.",
										  350, 5000, Position.TOP_RIGHT, API_ICON);
				}
			}, 20);
    }
}
