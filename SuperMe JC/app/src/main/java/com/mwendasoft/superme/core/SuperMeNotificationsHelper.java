package com.mwendasoft.superme.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Calendar;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.events.EventReminderReceiver;

public class SuperMeNotificationsHelper {

    private static final String CHANNEL_ID = "superme_event_channel";

    // Create a notification channel (Android 8+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "SuperMe Events",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for SuperMe upcoming events");
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Show a notification immediately
    public static void showNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more) // Use your icon. Its not recommended to use a built in one.
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Schedule a notification 1 day before event and return success status
	public static boolean scheduleNotification(Context context, int eventId, String title, String message, String dateStr, String timeStr) {
		try {
			// Expecting dateStr: "yyyy-MM-dd", timeStr: "HHmm"
			String[] parts = dateStr.split("-");
			int year = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]) - 1; // Month is 0-based
			int day = Integer.parseInt(parts[2]);

			int hour = Integer.parseInt(timeStr.substring(0, 2));
			int minute = Integer.parseInt(timeStr.substring(2));

			Calendar eventCal = Calendar.getInstance();
			eventCal.set(year, month, day, hour, minute, 0);
			eventCal.set(Calendar.MILLISECOND, 0);

			// Subtract 1 day
			eventCal.add(Calendar.DAY_OF_MONTH, -1);

			long triggerMillis = eventCal.getTimeInMillis();

			// Skip if time is in the past
			if (triggerMillis < System.currentTimeMillis()) {
				return false;
			}

			Intent intent = new Intent(context, EventReminderReceiver.class);
			intent.putExtra("title", title);
			intent.putExtra("message", message);

			int flags = PendingIntent.FLAG_UPDATE_CURRENT;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				flags |= PendingIntent.FLAG_IMMUTABLE;
			}

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
				context,
				eventId,
				intent,
				flags
			);

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (alarmManager != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
				} else {
					alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
				}
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
