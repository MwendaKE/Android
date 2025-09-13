package com.mwendasoft.superme.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.mwendasoft.superme.helpers.SuperMeNotificationsHelper;
import com.mwendasoft.superme.tasks.*;
import com.mwendasoft.superme.events.*;

public class ReminderReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        String itemType = intent.getStringExtra("item_type"); // "task" or "event"
        long itemId = intent.getLongExtra("item_id", -1);
        String reminderText = intent.getStringExtra("reminder_text");

        if (itemId == -1) return;

			String title = "";
			String notificationTitle = "";

		switch (itemType) {
			case "task":
				title = getTaskTitle(context, itemId);
				notificationTitle = "SuperMe Tasks Reminder";
				break;
				
			case "event":
				title = getEventTitle(context, itemId);
				notificationTitle = "SuperMe Events Reminder";
                break;
		}
		
		if (!title.isEmpty()) {
			new SuperMeNotificationsHelper(context).showNotification(
			    itemType, 
				notificationTitle,
                reminderText + title,
                generateNotificationId(itemType, itemId)
            );
        }
    }

    private String getTaskTitle(Context context, long taskId) {
		TasksDBHelper dbHelper = null;
		Cursor cursor = null;
		String title = "";
		try {
			dbHelper = new TasksDBHelper(context);
			cursor = dbHelper.getTaskById((int)taskId);
			if (cursor != null && cursor.moveToFirst()) {
				title = cursor.getString(1); // Column 1 = title
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
		return title;
	}

	private String getEventTitle(Context context, long eventId) {
		EventsDBHelper dbHelper = null;
		Cursor cursor = null;
		String title = "";
		try {
			dbHelper = new EventsDBHelper(context);
			cursor = dbHelper.getEventById((int)eventId);
			if (cursor != null && cursor.moveToFirst()) {
				title = cursor.getString(1); // Column 1 = title
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
		return title;
	}

	private int generateNotificationId(String itemType, long itemId) {
		if ("task".equals(itemType)) {
			return 1000 + (int) itemId;
		} else {
			return 2000 + (int) itemId;
		}
	}
}
