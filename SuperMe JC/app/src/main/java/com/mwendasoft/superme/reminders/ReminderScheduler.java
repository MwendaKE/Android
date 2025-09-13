package com.mwendasoft.superme.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {
    private static final String TAG = "ReminderScheduler";

    // Reminder types
    public static final String TYPE_TASK = "task";
    public static final String TYPE_EVENT = "event";

    // Schedule both reminders (1 day and 2 hours before)
    public static void scheduleReminders(Context context, String itemType, long itemId, long dueTime) {
        long oneDayBefore = dueTime - (24 * 60 * 60 * 1000);
        long twoHoursBefore = dueTime - (2 * 60 * 60 * 1000);

        scheduleNotification(context, itemType, itemId, oneDayBefore, getReminderText(itemType, "1 day"));
        scheduleNotification(context, itemType, itemId, twoHoursBefore, getReminderText(itemType, "2 hours"));
    }

    private static String getReminderText(String itemType, String timeLeft) {
        switch (itemType) {
            case TYPE_TASK: return timeLeft + " left to complete: ";
            case TYPE_EVENT: return timeLeft + " until: ";
            default: return timeLeft + " left for: ";
        }
    }

    private static void scheduleNotification(Context context, String itemType, long itemId, 
											 long triggerTime, String reminderText) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        // Include all necessary data
        intent.putExtra("item_type", itemType);
        intent.putExtra("item_id", itemId);
        intent.putExtra("reminder_text", reminderText);

        // Generate unique request code
        int requestCode = generateRequestCode(itemType, itemId, triggerTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);

        // Debug logging
        String timeStr = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
			.format(new Date(triggerTime));
        Log.d(TAG, String.format(Locale.US, 
								 "Scheduled %s reminder for ID %d at %s", 
								 itemType, itemId, timeStr));
    }

    // Cancel both reminders
    public static void cancelReminders(Context context, String itemType, long itemId, long dueTime) {
        long oneDayBefore = dueTime - (24 * 60 * 60 * 1000);
        long twoHoursBefore = dueTime - (2 * 60 * 60 * 1000);

        cancelNotification(context, itemType, itemId, oneDayBefore);
        cancelNotification(context, itemType, itemId, twoHoursBefore);
    }

    private static void cancelNotification(Context context, String itemType, 
										   long itemId, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        int requestCode = generateRequestCode(itemType, itemId, triggerTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, String.format(Locale.US, 
								 "Cancelled %s reminder for ID %d", itemType, itemId));
    }

    // Generates unique request codes for each reminder
    private static int generateRequestCode(String itemType, long itemId, long triggerTime) {
        int typePrefix = itemType.equals(TYPE_TASK) ? 100000 : 200000;
        return typePrefix + (int)(itemId % 10000) + (int)(triggerTime % 10000);
    }
}
