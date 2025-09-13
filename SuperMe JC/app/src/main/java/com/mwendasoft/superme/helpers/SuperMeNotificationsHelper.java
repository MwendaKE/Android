package com.mwendasoft.superme.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.mwendasoft.superme.R;

public class SuperMeNotificationsHelper {
    private static final String CHANNEL_TASKS = "tasks_reminders";
    private static final String CHANNEL_EVENTS = "events_reminders";
    private final Context context;
    private final NotificationManager manager;

    public SuperMeNotificationsHelper(Context context) {
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createAllChannels();
    }

    private void createAllChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Task Channel (Default Importance)
            NotificationChannel tasksChannel = new NotificationChannel(
                CHANNEL_TASKS,
                context.getString(R.string.channel_tasks),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            tasksChannel.setDescription(context.getString(R.string.channel_tasks_desc));

            // Event Channel (High Importance)
            NotificationChannel eventsChannel = new NotificationChannel(
                CHANNEL_EVENTS,
                context.getString(R.string.channel_events),
                NotificationManager.IMPORTANCE_HIGH
            );
            eventsChannel.setDescription(context.getString(R.string.channel_events_desc));
            eventsChannel.setVibrationPattern(new long[]{0, 500, 250, 500});

            manager.createNotificationChannel(tasksChannel);
            manager.createNotificationChannel(eventsChannel);
        }
    }

    public void showNotification(String section, String title, String message, int itemId) {
        String channelId = getChannelIdForSection(section);
        int notificationId = generateNotificationId(section, itemId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getIconForSection(section))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(getPriorityForSection(section))
            .setGroup("group_" + section)
            .setAutoCancel(true);

        manager.notify(notificationId, builder.build());
    }

    public int generateNotificationId(String section, int itemId) {
        return (section.equals("tasks") ? 1000 : 2000) + itemId;
    }

    private String getChannelIdForSection(String section) {
        return section.equals("events") ? CHANNEL_EVENTS : CHANNEL_TASKS;
    }

    private int getIconForSection(String section) {
        return section.equals("events") ? R.drawable.ic_event_notification : R.drawable.ic_task_notification;
    }

    private int getPriorityForSection(String section) {
        return section.equals("events") ? 
            NotificationCompat.PRIORITY_HIGH : 
            NotificationCompat.PRIORITY_DEFAULT;
    }
}
