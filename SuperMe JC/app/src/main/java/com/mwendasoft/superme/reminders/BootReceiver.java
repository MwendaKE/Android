package com.mwendasoft.superme.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.mwendasoft.superme.tasks.TasksDBHelper;
import com.mwendasoft.superme.events.EventsDBHelper;
import com.mwendasoft.superme.reminders.ConvertTimeToMillis;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted - restoring reminders");
            restoreTaskReminders(context);
            restoreEventReminders(context);
        }
    }

    private void restoreTaskReminders(Context context) {
        TasksDBHelper dbHelper = new TasksDBHelper(context);
        try (Cursor cursor = dbHelper.getAllTasks()) {
            int count = 0;
            int errors = 0;

            while (cursor.moveToNext()) {
                long taskId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String dueDateTime = cursor.getString(cursor.getColumnIndexOrThrow("edate"));

                // Validate and convert time
                if (!ConvertTimeToMillis.isValidFormat(dueDateTime)) {
                    Log.w(TAG, "Invalid task date format for ID " + taskId + ": " + dueDateTime);
                    errors++;
                    continue;
                }

                long dueTimeMillis = ConvertTimeToMillis.convertToMillis(dueDateTime);

                // Only schedule future reminders
                if (dueTimeMillis > System.currentTimeMillis()) {
                    ReminderScheduler.scheduleReminders(
                        context,
                        ReminderScheduler.TYPE_TASK,
                        taskId,
                        dueTimeMillis
                    );
                    count++;
                }
            }
            Log.d(TAG, String.format("Restored %d task reminders (%d invalid formats)", count, errors));
        }
    }

    private void restoreEventReminders(Context context) {
        EventsDBHelper dbHelper = new EventsDBHelper(context);
        try (Cursor cursor = dbHelper.getAllEvents()) {
            int count = 0;
            int errors = 0;

            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String eventTime = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                String combinedDateTime = eventDate + " " + eventTime;

                // Validate and convert time
                if (!ConvertTimeToMillis.isValidFormat(combinedDateTime)) {
                    Log.w(TAG, "Invalid event date format for ID " + eventId + ": " + combinedDateTime);
                    errors++;
                    continue;
                }

                long eventTimeMillis = ConvertTimeToMillis.convertToMillis(combinedDateTime);

                // Only schedule future reminders
                if (eventTimeMillis > System.currentTimeMillis()) {
                    ReminderScheduler.scheduleReminders(
                        context,
                        ReminderScheduler.TYPE_EVENT,
                        eventId,
                        eventTimeMillis
                    );
                    count++;
                }
            }
            Log.d(TAG, String.format("Restored %d event reminders (%d invalid formats)", count, errors));
        }
    }
}
